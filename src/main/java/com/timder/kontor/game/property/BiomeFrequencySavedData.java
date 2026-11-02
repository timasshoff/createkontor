package com.timder.kontor.game.property;

import com.timder.kontor.CreateKontor;
import com.timder.kontor.core.company.property.BiomeFrequencies;
import com.timder.kontor.core.company.property.BiomeId;
import com.timder.kontor.core.company.property.DimensionId;
import com.timder.kontor.core.company.property.measurement.MeasuredFrequencies;
import com.timder.kontor.core.company.property.measurement.MeasurementGrid;
import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class BiomeFrequencySavedData extends SavedData {

    private static final String NAME = "kontor_biome_frequencies";

    private final Map<DimensionId, MeasuredFrequencies> measured = new LinkedHashMap<>();

    public static BiomeFrequencySavedData get(MinecraftServer server) {
        SavedData.Factory<BiomeFrequencySavedData> factory =
                new SavedData.Factory<>(BiomeFrequencySavedData::new, (tag, registries) -> load(tag));
        return server.overworld().getDataStorage().computeIfAbsent(factory, NAME);
    }

    public Optional<MeasuredFrequencies> get(DimensionId dimension) {
        Objects.requireNonNull(dimension, "dimension must not be null.");
        return Optional.ofNullable(measured.get(dimension));
    }

    public Map<DimensionId, MeasuredFrequencies> all() {
        return Collections.unmodifiableMap(measured);
    }

    public Map<DimensionId, BiomeFrequencies> frequencies() {
        Map<DimensionId, BiomeFrequencies> result = new LinkedHashMap<>();
        for (Map.Entry<DimensionId, MeasuredFrequencies> entry : measured.entrySet()) {
            result.put(entry.getKey(), entry.getValue().frequencies());
        }
        return result;
    }

    public void put(DimensionId dimension, MeasuredFrequencies frequencies) {
        Objects.requireNonNull(dimension, "dimension must not be null.");
        Objects.requireNonNull(frequencies, "frequencies must not be null.");
        measured.put(dimension, frequencies);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag dimensions = new ListTag();
        for (Map.Entry<DimensionId, MeasuredFrequencies> entry : measured.entrySet()) {
            MeasurementGrid grid = entry.getValue().grid();
            CompoundTag dimensionTag = new CompoundTag();
            dimensionTag.putString("Dimension", entry.getKey().value());
            dimensionTag.putInt("CenterX", grid.centerX());
            dimensionTag.putInt("CenterZ", grid.centerZ());
            dimensionTag.putInt("Radius", grid.radius());
            dimensionTag.putInt("Step", grid.step());

            ListTag samples = new ListTag();
            for (Map.Entry<BiomeId, Long> sample :
                    entry.getValue().frequencies().samples().entrySet()) {
                CompoundTag sampleTag = new CompoundTag();
                sampleTag.putString("Biome", sample.getKey().value());
                sampleTag.putLong("Count", sample.getValue());
                samples.add(sampleTag);
            }
            dimensionTag.put("Samples", samples);
            dimensions.add(dimensionTag);
        }
        tag.put("Dimensions", dimensions);
        return tag;
    }

    private static BiomeFrequencySavedData load(CompoundTag tag) {
        BiomeFrequencySavedData data = new BiomeFrequencySavedData();
        for (Tag t : tag.getList("Dimensions", Tag.TAG_COMPOUND)) {
            CompoundTag dimensionTag = (CompoundTag) t;
            String name = dimensionTag.getString("Dimension");
            try {
                MeasurementGrid grid = new MeasurementGrid(
                        dimensionTag.getInt("CenterX"),
                        dimensionTag.getInt("CenterZ"),
                        dimensionTag.getInt("Radius"),
                        dimensionTag.getInt("Step"));
                Map<BiomeId, Long> samples = new LinkedHashMap<>();
                for (Tag s : dimensionTag.getList("Samples", Tag.TAG_COMPOUND)) {
                    CompoundTag sampleTag = (CompoundTag) s;
                    samples.put(new BiomeId(sampleTag.getString("Biome")), sampleTag.getLong("Count"));
                }
                data.measured.put(new DimensionId(name), new MeasuredFrequencies(grid, new BiomeFrequencies(samples)));
            } catch (IllegalArgumentException e) {
                CreateKontor.LOGGER.warn(
                        "Skipping the saved biome frequencies of \"{}\" because they are invalid: {}",
                        name,
                        e.getMessage());
            }
        }
        return data;
    }
}
