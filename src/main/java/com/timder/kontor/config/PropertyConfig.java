package com.timder.kontor.config;

import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.property.*;
import com.timder.kontor.core.company.property.measurement.MeasurementGrid;
import com.timder.kontor.game.property.BiomeFrequencySavedData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class PropertyConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue MEASUREMENT_CENTER_X;
    public static final ModConfigSpec.IntValue MEASUREMENT_CENTER_Z;
    public static final ModConfigSpec.IntValue MEASUREMENT_RADIUS_BLOCKS;
    public static final ModConfigSpec.IntValue MEASUREMENT_STEP_BLOCKS;
    public static final ModConfigSpec.IntValue MEASUREMENT_SAMPLES_PER_TICK;
    public static final ModConfigSpec.IntValue MEASUREMENT_CEILING_SAMPLE_Y;
    public static final ModConfigSpec.IntValue MEASUREMENT_MAX_MILLIS_PER_TICK;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MEASUREMENT_AUTO_DIMENSIONS;
    public static final ModConfigSpec.DoubleValue PRICING_RARITY_EXPONENT;
    public static final ModConfigSpec.DoubleValue PRICING_MIN_FACTOR;
    public static final ModConfigSpec.DoubleValue PRICING_MAX_FACTOR;
    public static final ModConfigSpec.DoubleValue DAILY_RENT_RATE;
    public static final ModConfigSpec.DoubleValue RESALE_SHARE;

    private record DimensionSetting(
            String dimension,
            ModConfigSpec.ConfigValue<String> referenceBiome,
            ModConfigSpec.IntValue referencePrice) {}

    private static final List<DimensionSetting> DIMENSIONS = new ArrayList<>();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment(
                        "How the biome frequencies of a dimension are measured. The frequencies decide how rare a biome is, "
                                + "and so what land in that biome costs. A measurement samples a square grid of points; at each point "
                                + "the biome of the world generator is read (no chunk is loaded).")
                .push("measurement");

        MEASUREMENT_CENTER_X = builder.comment(
                        "X coordinate (blocks) of the middle of the measured area, in every dimension")
                .defineInRange("centerX", 0, -29_999_984, 29_999_984);
        MEASUREMENT_CENTER_Z = builder.comment(
                        "Z coordinate (blocks) of the middle of the measured area, in every dimension")
                .defineInRange("centerZ", 0, -29_999_984, 29_999_984);
        MEASUREMENT_RADIUS_BLOCKS = builder.comment(
                        "How far the measured area reaches from its middle, in blocks. Rounded down to a multiple of the step.")
                .defineInRange("radiusBlocks", 16_384, 0, 1_000_000);
        MEASUREMENT_STEP_BLOCKS = builder.comment(
                        "Distance between two sample points, in blocks. A grid may have at most 1999 points per side.")
                .defineInRange("stepBlocks", 128, 1, 100_000);
        MEASUREMENT_SAMPLES_PER_TICK = builder.comment(
                        "How many points are sampled per server tick while a measurement runs. Lower = less lag, slower.")
                .defineInRange("samplesPerTick", 64, 1, 100_000);
        MEASUREMENT_MAX_MILLIS_PER_TICK = builder.comment(
                        "Upper limit for the time spent sampling per server tick, in milliseconds. "
                                + "At least one point is sampled per tick, even if that takes longer. "
                                + "Lower = less lag, slower.")
                .defineInRange("maxMillisPerTick", 40, 1, 1_000);
        MEASUREMENT_CEILING_SAMPLE_Y = builder.comment(
                        "Height at which dimensions with a bedrock ceiling (the Nether) are sampled. "
                                + "Other dimensions are sampled at the height of the surface.")
                .defineInRange("ceilingSampleY", 64, -64, 320);
        MEASUREMENT_AUTO_DIMENSIONS = builder.comment(
                        "Dimensions that are measured automatically when the server has started, if they have no saved "
                                + "measurement yet. A saved measurement is never replaced automatically; use "
                                + "/kontor property measure start for that. Land cannot be priced in a dimension "
                                + "until its measurement is done.")
                .defineListAllowEmpty(
                        "autoDimensions",
                        List.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"),
                        () -> "minecraft:overworld",
                        value -> value instanceof String string && ResourceLocation.tryParse(string) != null);

        builder.pop();

        builder.comment("What land costs: reference price of a dimension times a factor from the rarity of the biome, "
                        + "(samples of the reference biome / samples of the biome) ^ exponent, kept between the "
                        + "minimum and the maximum factor. A biome the measurement never hit gets the maximum factor.")
                .push("pricing");
        PRICING_RARITY_EXPONENT = builder.comment(
                        "0 = every biome costs the reference price, 1 = price follows rarity linearly")
                .defineInRange("rarityExponent", 0.4, 0.0, 5.0);
        PRICING_MIN_FACTOR = builder.comment("Lowest factor, only biomes more common than the reference biome get it")
                .defineInRange("minFactor", 0.2, 0.01, 1.0);
        PRICING_MAX_FACTOR = builder.comment("Highest factor, also for biomes that were never hit")
                .defineInRange("maxFactor", 50.0, 1.0, 1_000.0);
        defineDimension(builder, "overworld", "minecraft:overworld", "minecraft:plains", 8000);
        defineDimension(builder, "nether", "minecraft:the_nether", "minecraft:nether_wastes", 8000);
        defineDimension(builder, "end", "minecraft:the_end", "minecraft:end_highlands", 16000);
        builder.pop();

        builder.comment("Owning and renting land").push("ownership");
        DAILY_RENT_RATE = builder.comment("Daily rent of a rented chunk as a share of its current price")
                .defineInRange("dailyRentRate", 0.02, 0.0, 1.0);
        RESALE_SHARE = builder.comment(
                        "Share of the current price a company gets when it sells a chunk, and at which owned land counts as net worth")
                .defineInRange("resaleShare", 0.6, 0.0, 1.0);
        builder.pop();

        SPEC = builder.build();
    }

    private static void defineDimension(
            ModConfigSpec.Builder builder, String name, String dimension, String biome, int priceInDollars) {
        builder.comment("Reference biome and price of " + dimension
                        + ". The reference biome must appear in the measurement "
                        + "(/kontor property table), otherwise nothing in this dimension can be priced.")
                .push(name);
        ModConfigSpec.ConfigValue<String> referenceBiome =
                builder.define("referenceBiome", biome, value -> value instanceof String text && !text.isBlank());
        ModConfigSpec.IntValue referencePrice = builder.comment("Price of a chunk of the reference biome")
                .defineInRange("referencePriceInDollars", priceInDollars, 1, Integer.MAX_VALUE);
        builder.pop();
        DIMENSIONS.add(new DimensionSetting(dimension, referenceBiome, referencePrice));
    }

    public static PropertyParams toPropertyParams() {
        List<DimensionPricing> dimensions = new ArrayList<>();
        for (DimensionSetting setting : DIMENSIONS) {
            dimensions.add(new DimensionPricing(
                    new DimensionId(setting.dimension()),
                    new BiomeId(setting.referenceBiome().get()),
                    Money.ofDollars(setting.referencePrice().get())));
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        Map<DimensionId, BiomeFrequencies> frequencies =
                server == null ? Map.of() : BiomeFrequencySavedData.get(server).frequencies();
        return new PropertyParams(
                new PropertyPricingParams(
                        PRICING_RARITY_EXPONENT.get(), PRICING_MIN_FACTOR.get(), PRICING_MAX_FACTOR.get(), dimensions),
                frequencies,
                DAILY_RENT_RATE.get(),
                RESALE_SHARE.get());
    }

    public static MeasurementGrid toMeasurementGrid() {
        return new MeasurementGrid(
                MEASUREMENT_CENTER_X.get(),
                MEASUREMENT_CENTER_Z.get(),
                MEASUREMENT_RADIUS_BLOCKS.get(),
                MEASUREMENT_STEP_BLOCKS.get());
    }
}
