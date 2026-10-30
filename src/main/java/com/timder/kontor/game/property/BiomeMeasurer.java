package com.timder.kontor.game.property;

import com.timder.kontor.CreateKontor;
import com.timder.kontor.config.PropertyConfig;
import com.timder.kontor.core.company.property.DimensionId;
import com.timder.kontor.core.company.property.measurement.BiomeMeasurement;
import com.timder.kontor.core.company.property.measurement.MeasuredFrequencies;
import com.timder.kontor.core.company.property.measurement.MeasurementGrid;
import com.timder.kontor.util.ComponentFormatting;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class BiomeMeasurer {

    private static final long SLOW_SAMPLE_NANOS = 1_000_000_000L;

    private record Job(ResourceKey<Level> level, BiomeMeasurement measurement, int ceilingSampleY) {}

    public enum StartResult {
        STARTED,
        ALREADY_RUNNING
    }

    private static final Map<DimensionId, Job> JOBS = new LinkedHashMap<>();

    public static StartResult start(ServerLevel level) {
        DimensionId dimensionId = BiomeSampling.dimensionId(level);
        if (JOBS.containsKey(dimensionId)) {
            return StartResult.ALREADY_RUNNING;
        }
        MeasurementGrid grid = PropertyConfig.toMeasurementGrid();
        JOBS.put(
                dimensionId,
                new Job(
                        level.dimension(),
                        new BiomeMeasurement(grid),
                        PropertyConfig.MEASUREMENT_CEILING_SAMPLE_Y.get()));
        return StartResult.STARTED;
    }

    public static boolean isRunning(DimensionId dimension) {
        return JOBS.containsKey(dimension);
    }

    public static boolean cancel(DimensionId dimension) {
        return JOBS.remove(dimension) != null;
    }

    public static Map<DimensionId, Double> progress() {
        Map<DimensionId, Double> progress = new LinkedHashMap<>();
        for (Map.Entry<DimensionId, Job> entry : JOBS.entrySet()) {
            progress.put(entry.getKey(), entry.getValue().measurement().progress());
        }
        return progress;
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (JOBS.isEmpty()) {
            return;
        }

        MinecraftServer server = event.getServer();
        int budget = PropertyConfig.MEASUREMENT_SAMPLES_PER_TICK.get();
        long deadline = System.nanoTime() + PropertyConfig.MEASUREMENT_MAX_MILLIS_PER_TICK.get() * 1_000_000L;

        Iterator<Map.Entry<DimensionId, Job>> jobs = JOBS.entrySet().iterator();
        while (jobs.hasNext()) {
            Map.Entry<DimensionId, Job> entry = jobs.next();
            DimensionId dimension = entry.getKey();
            Job job = entry.getValue();

            ServerLevel level = server.getLevel(job.level());
            if (level == null) {
                CreateKontor.LOGGER.warn(
                        "Stopping the biome measurement of {}: the dimension does not exist anymore.", dimension);
                jobs.remove();
                continue;
            }

            BiomeMeasurement measurement = job.measurement();
            try {
                while (budget > 0 && !measurement.isDone()) {
                    int x = measurement.nextX();
                    int z = measurement.nextZ();
                    long started = System.nanoTime();
                    measurement.record(BiomeSampling.biomeAt(level, x, z, job.ceilingSampleY()));
                    budget--;

                    long now = System.nanoTime();
                    if (now - started > SLOW_SAMPLE_NANOS) {
                        CreateKontor.LOGGER.warn(
                                "Sampling x={} z={} in {} took {} ms.", x, z, dimension, (now - started) / 1_000_000L);
                    }
                    if (now >= deadline) {
                        budget = 0;
                    }
                }
            } catch (RuntimeException e) {
                CreateKontor.LOGGER.error("Stopping the measurement of {} because an error occurred.", dimension, e);
                jobs.remove();
                continue;
            }

            if (measurement.isDone()) {
                MeasuredFrequencies result = measurement.result();
                BiomeFrequencySavedData.get(server).put(dimension, result);
                jobs.remove();
                CreateKontor.LOGGER.info(
                        "Biome measurement of {} is done: {} points, {} biomes sampled.",
                        dimension,
                        result.frequencies().total(),
                        result.frequencies().samples().size());
                server.getPlayerList()
                        .broadcastSystemMessage(
                                Component.translatable(
                                        "message.createkontor.measurement_done",
                                        ComponentFormatting.highlightStandard(dimension.toString())),
                                false);
            }
        }
    }

    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        BiomeFrequencySavedData data = BiomeFrequencySavedData.get(server);

        for (String name : PropertyConfig.MEASUREMENT_AUTO_DIMENSIONS.get()) {
            ResourceLocation location = ResourceLocation.tryParse(name);
            ServerLevel level =
                    location == null ? null : server.getLevel(ResourceKey.create(Registries.DIMENSION, location));
            if (level == null) {
                CreateKontor.LOGGER.warn("Not measuring \"{}\": dimension does not exist.", name);
                continue;
            }
            DimensionId dimension = BiomeSampling.dimensionId(level);
            if (data.get(dimension).isPresent()) {
                continue;
            }
            try {
                if (start(level) == StartResult.STARTED) {
                    CreateKontor.LOGGER.info(
                            "Measuring the biomes of {}. Server may lag, this is normal! Depending on your configuration, measurement might take a few minutes.",
                            dimension);
                }
            } catch (IllegalArgumentException e) {
                CreateKontor.LOGGER.error(
                        "Not measuring {}: the measurement settings are invalid: {}", dimension, e.getMessage());
            }
        }
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        JOBS.clear();
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (JOBS.isEmpty() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        for (Map.Entry<DimensionId, Job> entry : JOBS.entrySet()) {
            double progress = entry.getValue().measurement().progress();
            player.sendSystemMessage(Component.translatable(
                    "message.createkontor.measurement_running",
                    ComponentFormatting.highlightStandard(entry.getKey().toString()),
                    ComponentFormatting.highlightStandard(String.format(Locale.ROOT, "%.0f %%", progress * 100.0))));
        }
    }
}
