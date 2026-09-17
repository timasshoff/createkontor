package com.timder.kontor.config;

import com.timder.kontor.core.macro.MacroParams;
import com.timder.kontor.core.macro.ProgressParams;
import com.timder.kontor.core.raw.PriceProcessParams;
import net.neoforged.neoforge.common.ModConfigSpec;

public class EconomyConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue MIN_CYCLE_LENGTH;
    public static final ModConfigSpec.IntValue MAX_CYCLE_LENGTH;
    public static final ModConfigSpec.DoubleValue MIN_AMPLITUDE;
    public static final ModConfigSpec.DoubleValue MAX_AMPLITUDE;
    public static final ModConfigSpec.DoubleValue NOISE_DECAY;
    public static final ModConfigSpec.DoubleValue NOISE_SCALE;
    public static final ModConfigSpec.DoubleValue MIN_INDEX;
    public static final ModConfigSpec.DoubleValue MAX_INDEX;
    public static final ModConfigSpec.DoubleValue TREND_PER_DAY;

    public static final ModConfigSpec.DoubleValue DAILY_DECAY;
    public static final ModConfigSpec.DoubleValue PROGRESS_FLOOR;

    public static final ModConfigSpec.DoubleValue MEAN_REVERSION;
    public static final ModConfigSpec.DoubleValue PURCHASE_PRESSURE;
    public static final ModConfigSpec.DoubleValue MIN_FACTOR;
    public static final ModConfigSpec.DoubleValue MAX_FACTOR;

    public static final ModConfigSpec.DoubleValue DEFAULT_TARGET_UTILISATION;

    public static final ModConfigSpec.DoubleValue DEFAULT_PROCESS_COST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Macroeconomic Cycle").push("macro");
        MIN_CYCLE_LENGTH = builder.comment("Shortest possible cycle in days")
                .defineInRange("minCycleLength", 40, 1, Integer.MAX_VALUE);
        MAX_CYCLE_LENGTH = builder.comment("Longest possible cycle in days")
                .defineInRange("maxCycleLength", 80, 1, Integer.MAX_VALUE);
        MIN_AMPLITUDE = builder.comment("Weakest possible amplitude")
                .defineInRange("minAmplitude", 0.08, 0.0, Double.MAX_VALUE);
        MAX_AMPLITUDE = builder.comment("Strongest possible amplitude")
                .defineInRange("maxAmplitude", 0.18, 0.0, Double.MAX_VALUE);
        NOISE_DECAY = builder.comment("How much noise from the previous day decays")
                .defineInRange("noiseDecay", 0.8, 0.0, 0.999999);
        NOISE_SCALE = builder.comment("How much noise gets added each day")
                .defineInRange("noiseScale", 0.01, 0.0, Double.MAX_VALUE);
        MIN_INDEX = builder.comment("Minimal cycle index")
                .defineInRange("minIndex", 0.70, 1.0E-6, Double.MAX_VALUE);
        MAX_INDEX = builder.comment("Maximum cycle index")
                .defineInRange("maxIndex", 1.30, 1.0E-6, Double.MAX_VALUE);
        TREND_PER_DAY = builder.comment("Long-term demand growth")
                .defineInRange("maxIndex", 1.30, 0, Double.MAX_VALUE);
        builder.pop();

        builder.comment("Technical Progress").push("progress");
        DAILY_DECAY = builder.comment("How much processing costs sink every day (for competitors)")
                .defineInRange("dailyDecay", 0.002, 0.0, 0.999999);
        PROGRESS_FLOOR = builder.comment("Minimum processing costs")
                .defineInRange("floor", 0.60, 1.0E-6, 1.0);
        builder.pop();

        builder.comment("Processing of raw material prices").push("priceProcess");
        MEAN_REVERSION = builder.comment("Daily reversion of a price back to its base")
                .defineInRange("meanReversion", 0.15, 1.0E-6, 1.0);
        PURCHASE_PRESSURE = builder.comment("How much a purchase influences the price")
                .defineInRange("purchasePressure", 0.10, 0.0, Double.MAX_VALUE);
        MIN_FACTOR = builder.comment("Floor relative to the base price")
                .defineInRange("minFactor", 0.3, 1.0E-6, Double.MAX_VALUE);
        MAX_FACTOR = builder.comment("Ceiling relative to the base price")
                .defineInRange("maxFactor", 4.0, 1.0E-6, Double.MAX_VALUE);
        builder.pop();

        builder.comment("Default values for the market simulation").push("market");
        DEFAULT_TARGET_UTILISATION = builder
                .comment("Fallback for the target utilisation of competitors")
                .defineInRange("defaultTargetUtilisation", 0.80, 1.0E-6, 1.0);
        builder.pop();

        builder.comment("Processing cost").push("processCosts");
        DEFAULT_PROCESS_COST = builder.comment("Fallback")
                .defineInRange("defaultCost", 0.50, 0.0, Double.MAX_VALUE);
        builder.pop();

        SPEC = builder.build();
    }

    public static MacroParams toMacroParams() {
        return new MacroParams(
                MIN_CYCLE_LENGTH.get(), MAX_CYCLE_LENGTH.get(),
                MIN_AMPLITUDE.get(), MAX_AMPLITUDE.get(),
                NOISE_DECAY.get(), NOISE_SCALE.get(),
                MIN_INDEX.get(), MAX_INDEX.get(),
                TREND_PER_DAY.get());
    }

    public static ProgressParams toProgressParams() {
        return new ProgressParams(DAILY_DECAY.get(), PROGRESS_FLOOR.get());
    }

    public static PriceProcessParams toPriceProcessParams() {
        return new PriceProcessParams(MEAN_REVERSION.get(), PURCHASE_PRESSURE.get(), MIN_FACTOR.get(), MAX_FACTOR.get());
    }
}
