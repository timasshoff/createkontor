package com.timder.kontor.game.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.timder.kontor.config.PropertyConfig;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.property.BiomeFrequencies;
import com.timder.kontor.core.company.property.BiomeId;
import com.timder.kontor.core.company.property.DimensionId;
import com.timder.kontor.core.company.property.DimensionPricing;
import com.timder.kontor.core.company.property.PropertyPricingParams;
import com.timder.kontor.core.company.property.PropertyPricingRules;
import com.timder.kontor.core.company.property.measurement.MeasuredFrequencies;
import com.timder.kontor.core.company.property.measurement.MeasurementGrid;
import com.timder.kontor.core.company.property.measurement.MeasurementReport;
import com.timder.kontor.game.property.BiomeFrequencySavedData;
import com.timder.kontor.game.property.BiomeMeasurer;
import com.timder.kontor.game.property.BiomeSampling;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;

/*
The following code has been AI generated.
 */
public final class PropertyCommands {

    private static final double DEFAULT_EXPONENT = 0.5;
    private static final double DEFAULT_MIN_FACTOR = 0.2;
    private static final double DEFAULT_MAX_FACTOR = 50.0;

    private PropertyCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kontor")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("property")
                        .then(Commands.literal("measure")
                                .then(Commands.literal("start")
                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                .executes(PropertyCommands::measureStart)))
                                .then(Commands.literal("status").executes(PropertyCommands::measureStatus))
                                .then(Commands.literal("cancel")
                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                .executes(PropertyCommands::measureCancel))))
                        .then(Commands.literal("table")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .executes(PropertyCommands::table)))
                        .then(Commands.literal("pricepreview")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .then(Commands.argument("referenceBiome", ResourceLocationArgument.id())
                                                .then(pricePreviewPrice()))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> pricePreviewPrice() {
        ArgumentBuilder<CommandSourceStack, ?> maxFactor = Commands.argument(
                        "maxFactor", DoubleArgumentType.doubleArg(1.0))
                .executes(context -> pricePreview(
                        context,
                        DoubleArgumentType.getDouble(context, "exponent"),
                        DoubleArgumentType.getDouble(context, "minFactor"),
                        DoubleArgumentType.getDouble(context, "maxFactor")));
        ArgumentBuilder<CommandSourceStack, ?> minFactor = Commands.argument(
                        "minFactor", DoubleArgumentType.doubleArg(0.0001, 1.0))
                .then(maxFactor);
        ArgumentBuilder<CommandSourceStack, ?> exponent = Commands.argument(
                        "exponent", DoubleArgumentType.doubleArg(0.0))
                .executes(context -> pricePreview(
                        context,
                        DoubleArgumentType.getDouble(context, "exponent"),
                        DEFAULT_MIN_FACTOR,
                        DEFAULT_MAX_FACTOR))
                .then(minFactor);
        return Commands.argument("referencePrice", DoubleArgumentType.doubleArg(0.01))
                .executes(context -> pricePreview(context, DEFAULT_EXPONENT, DEFAULT_MIN_FACTOR, DEFAULT_MAX_FACTOR))
                .then(exponent);
    }

    private static int measureStart(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            ServerLevel level = DimensionArgument.getDimension(context, "dimension");
            MeasurementGrid grid = PropertyConfig.toMeasurementGrid();
            if (BiomeMeasurer.start(level) == BiomeMeasurer.StartResult.ALREADY_RUNNING) {
                source.sendFailure(Component.literal(
                        "A measurement of " + BiomeSampling.dimensionId(level) + " is already running."));
                return 0;
            }
            source.sendSuccess(
                    () -> Component.literal(String.format(
                            Locale.ROOT,
                            "Measuring %s: %d points (%d x %d), around x=%d z=%d, step %d blocks, %d points per tick. Roughly %d ticks.",
                            BiomeSampling.dimensionId(level),
                            grid.pointCount(),
                            grid.pointsPerSide(),
                            grid.pointsPerSide(),
                            grid.centerX(),
                            grid.centerZ(),
                            grid.step(),
                            PropertyConfig.MEASUREMENT_SAMPLES_PER_TICK.get(),
                            (grid.pointCount() + PropertyConfig.MEASUREMENT_SAMPLES_PER_TICK.get() - 1)
                                    / PropertyConfig.MEASUREMENT_SAMPLES_PER_TICK.get())),
                    true);
            return 1;
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("The measurement settings are invalid: " + e.getMessage()));
            return 0;
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            source.sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int measureStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        BiomeFrequencySavedData data = BiomeFrequencySavedData.get(source.getServer());
        MutableComponent text = Component.literal("=== Biome measurements ===");

        Map<DimensionId, Double> running = BiomeMeasurer.progress();
        if (running.isEmpty()) {
            text.append(Component.literal("\nRunning: none."));
        }
        for (Map.Entry<DimensionId, Double> entry : running.entrySet()) {
            text.append(Component.literal(
                    String.format(Locale.ROOT, "\nRunning: %s at %.1f %%", entry.getKey(), entry.getValue() * 100.0)));
        }

        if (data.all().isEmpty()) {
            text.append(Component.literal("\nSaved: none."));
        }
        for (Map.Entry<DimensionId, MeasuredFrequencies> entry : data.all().entrySet()) {
            MeasurementGrid grid = entry.getValue().grid();
            text.append(Component.literal(String.format(
                    Locale.ROOT,
                    "\nSaved: %s - %d points, %d biomes hit, around x=%d z=%d, radius %d, step %d",
                    entry.getKey(),
                    entry.getValue().frequencies().total(),
                    entry.getValue().frequencies().samples().size(),
                    grid.centerX(),
                    grid.centerZ(),
                    grid.radius(),
                    grid.step())));
        }
        source.sendSuccess(() -> text, false);
        return 1;
    }

    private static int measureCancel(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            DimensionId dimension = BiomeSampling.dimensionId(DimensionArgument.getDimension(context, "dimension"));
            if (!BiomeMeasurer.cancel(dimension)) {
                source.sendFailure(Component.literal("No measurement of " + dimension + " is running."));
                return 0;
            }
            source.sendSuccess(
                    () -> Component.literal("Stopped the measurement of " + dimension + ". Nothing was saved."), true);
            return 1;
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            source.sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int table(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            ServerLevel level = DimensionArgument.getDimension(context, "dimension");
            DimensionId dimension = BiomeSampling.dimensionId(level);
            Optional<MeasuredFrequencies> measured =
                    BiomeFrequencySavedData.get(source.getServer()).get(dimension);
            if (measured.isEmpty()) {
                source.sendFailure(
                        Component.literal(dimension + " was not measured yet. Use /kontor property measure start."));
                return 0;
            }
            MeasurementReport report =
                    MeasurementReport.of(measured.get().frequencies(), BiomeSampling.possibleBiomes(level));

            MutableComponent text = Component.literal("=== Biome frequencies of " + dimension + " ("
                    + measured.get().frequencies().total() + " points) ===");
            for (MeasurementReport.Row row : report.hit()) {
                text.append(Component.literal(String.format(
                        Locale.ROOT, "\n%s: %d (%.3f %%)", row.biome(), row.samples(), row.share() * 100.0)));
            }
            if (report.neverHit().isEmpty()) {
                text.append(Component.literal("\nNever hit: none."));
            } else {
                text.append(
                        Component.literal("\nNever hit (" + report.neverHit().size() + "):"));
                for (BiomeId biome : report.neverHit()) {
                    text.append(Component.literal("\n" + biome));
                }
            }
            source.sendSuccess(() -> text, false);
            return 1;
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            source.sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int pricePreview(
            CommandContext<CommandSourceStack> context, double exponent, double minFactor, double maxFactor) {
        CommandSourceStack source = context.getSource();
        try {
            ServerLevel level = DimensionArgument.getDimension(context, "dimension");
            DimensionId dimension = BiomeSampling.dimensionId(level);
            BiomeId referenceBiome = new BiomeId(
                    ResourceLocationArgument.getId(context, "referenceBiome").toString());
            Money referencePrice = Money.fromDollar(DoubleArgumentType.getDouble(context, "referencePrice"));

            Optional<MeasuredFrequencies> measured =
                    BiomeFrequencySavedData.get(source.getServer()).get(dimension);
            if (measured.isEmpty()) {
                source.sendFailure(
                        Component.literal(dimension + " was not measured yet. Use /kontor property measure start."));
                return 0;
            }
            BiomeFrequencies frequencies = measured.get().frequencies();

            PropertyPricingParams params = new PropertyPricingParams(
                    exponent,
                    minFactor,
                    maxFactor,
                    List.of(new DimensionPricing(dimension, referenceBiome, referencePrice)));
            if (!PropertyPricingRules.isPriceable(params, dimension, frequencies)) {
                source.sendFailure(Component.literal("The reference biome " + referenceBiome + " was not hit in "
                        + dimension + ", so nothing can be priced against it."));
                return 0;
            }

            Map<BiomeId, Money> prices = PropertyPricingRules.pricingMap(params, dimension, frequencies);
            MutableComponent text = Component.literal(String.format(
                    Locale.ROOT,
                    "=== Price preview %s | reference %s = %s | exponent %s, factor %s to %s | cheapest first ===",
                    dimension,
                    referenceBiome,
                    referencePrice,
                    exponent,
                    minFactor,
                    maxFactor));
            for (Map.Entry<BiomeId, Money> entry : prices.entrySet()) {
                text.append(Component.literal(String.format(
                        Locale.ROOT,
                        "\n%s: %s  (%d points)",
                        entry.getKey(),
                        entry.getValue(),
                        frequencies.count(entry.getKey()))));
            }

            // A biome that was never hit is priced like one hit zero times (the maximum factor).
            Money unmeasured = PropertyPricingRules.priceOf(params, dimension, frequencies, new BiomeId("never:hit"));
            text.append(Component.literal("\nNever hit: " + unmeasured));
            source.sendSuccess(() -> text, false);
            return 1;
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            source.sendFailure(Component.literal(e.getMessage()));
            return 0;
        } catch (IllegalArgumentException | IllegalStateException e) {
            source.sendFailure(Component.literal("The preview is not possible: " + e.getMessage()));
            return 0;
        }
    }
}
