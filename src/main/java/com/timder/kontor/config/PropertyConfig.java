package com.timder.kontor.config;

import com.timder.kontor.core.company.property.measurement.MeasurementGrid;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class PropertyConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue MEASUREMENT_CENTER_X;
    public static final ModConfigSpec.IntValue MEASUREMENT_CENTER_Z;
    public static final ModConfigSpec.IntValue MEASUREMENT_RADIUS_BLOCKS;
    public static final ModConfigSpec.IntValue MEASUREMENT_STEP_BLOCKS;
    public static final ModConfigSpec.IntValue MEASUREMENT_SAMPLES_PER_TICK;
    public static final ModConfigSpec.IntValue MEASUREMENT_CEILING_SAMPLE_Y;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MEASUREMENT_AUTO_DIMENSIONS;

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
        SPEC = builder.build();
    }

    public static MeasurementGrid toMeasurementGrid() {
        return new MeasurementGrid(
                MEASUREMENT_CENTER_X.get(),
                MEASUREMENT_CENTER_Z.get(),
                MEASUREMENT_RADIUS_BLOCKS.get(),
                MEASUREMENT_STEP_BLOCKS.get());
    }
}
