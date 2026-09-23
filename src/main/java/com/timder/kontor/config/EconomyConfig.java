package com.timder.kontor.config;

import com.timder.kontor.core.company.request.ReputationParams;
import com.timder.kontor.core.macro.MacroParams;
import com.timder.kontor.core.macro.PolicyRateParams;
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

    public static final ModConfigSpec.DoubleValue POLICY_BASE_RATE;
    public static final ModConfigSpec.DoubleValue POLICY_MIN_RATE;
    public static final ModConfigSpec.DoubleValue POLICY_MAX_RATE;
    public static final ModConfigSpec.IntValue POLICY_DECISION_INTERVAL_DAYS;
    public static final ModConfigSpec.DoubleValue POLICY_TARGET_SENSITIVITY;
    public static final ModConfigSpec.DoubleValue POLICY_SMALL_STEP;
    public static final ModConfigSpec.DoubleValue POLICY_LARGE_STEP;
    public static final ModConfigSpec.DoubleValue POLICY_SMALL_THRESHOLD;
    public static final ModConfigSpec.DoubleValue POLICY_LARGE_THRESHOLD;

    public static final ModConfigSpec.DoubleValue REPUTATION_ON_TIME_GAIN;
    public static final ModConfigSpec.DoubleValue REPUTATION_ON_TIME_URGENCY_FACTOR;
    public static final ModConfigSpec.DoubleValue REPUTATION_LATE_LOSS;
    public static final ModConfigSpec.DoubleValue REPUTATION_FAILURE_LOSS;
    public static final ModConfigSpec.DoubleValue REPUTATION_FOUNDER_PROTECTION_FACTOR;
    public static final ModConfigSpec.DoubleValue REPUTATION_DRIFT_PER_DAY;

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
                .defineInRange("trendPerDay", 0.0015, 0, Double.MAX_VALUE);
        builder.pop();

        builder.comment("Policy rate as a fraction per day, e.g. 0.0020 is 0.20 %").push("policyRate");
        POLICY_BASE_RATE = builder.comment("Start rate and rate of a normal economy (difficulty: 0.0010 easy, 0.0020 standard, 0.0030 realistic)")
                .defineInRange("baseRate", 0.0020, 1.0E-6, 1.0);
        POLICY_MIN_RATE = builder.comment("Lowest rate the process aims for")
                .defineInRange("minRate", 0.0005, 1.0E-6, 1.0);
        POLICY_MAX_RATE = builder.comment("Highest rate the process aims for")
                .defineInRange("maxRate", 0.0050, 1.0E-6, 1.0);
        POLICY_DECISION_INTERVAL_DAYS = builder.comment("A decision is made every this many days")
                .defineInRange("decisionIntervalDays", 7, 1, Integer.MAX_VALUE);
        POLICY_TARGET_SENSITIVITY = builder.comment("How strongly the target rate reacts to the cycle index")
                .defineInRange("targetSensitivity", 2.0, 0.0, Double.MAX_VALUE);
        POLICY_SMALL_STEP = builder.comment("Step when the target is further away than smallThreshold")
                .defineInRange("smallStep", 0.0005, 1.0E-6, 1.0);
        POLICY_LARGE_STEP = builder.comment("Step when the target is further away than largeThreshold")
                .defineInRange("largeStep", 0.0010, 1.0E-6, 1.0);
        POLICY_SMALL_THRESHOLD = builder.comment("The rate stays if the target is at most this far away")
                .defineInRange("smallThreshold", 0.00025, 1.0E-6, 1.0);
        POLICY_LARGE_THRESHOLD = builder.comment("Large step if the target is further away than this")
                .defineInRange("largeThreshold", 0.0015, 1.0E-6, 1.0);
        builder.pop();

        builder.comment("Reputation changes from settled orders").push("reputation");
        REPUTATION_ON_TIME_GAIN = builder.comment("Base reputation gain for an on-time order, before weighting")
                .defineInRange("onTimeGain", 1.5, 0.0, Double.MAX_VALUE);
        REPUTATION_ON_TIME_URGENCY_FACTOR = builder.comment("Extra gain for urgent orders, as the '1 + x*delta' factor")
                .defineInRange("onTimeUrgencyFactor", 0.5, 0.0, Double.MAX_VALUE);
        REPUTATION_LATE_LOSS = builder.comment("Base reputation loss for a late (grace period) order, before weighting")
                .defineInRange("lateLoss", 1.0, 0.0, Double.MAX_VALUE);
        REPUTATION_FAILURE_LOSS = builder.comment("Base reputation loss for a burst or cancelled order, before weighting")
                .defineInRange("failureLoss", 4.0, 0.0, Double.MAX_VALUE);
        REPUTATION_FOUNDER_PROTECTION_FACTOR = builder.comment("Dampening of the failure loss in legal form stage 1")
                .defineInRange("founderProtectionFactor", 0.5, 0.0, 1.0);
        REPUTATION_DRIFT_PER_DAY = builder.comment("Daily movement of an untouched product's reputation back toward 50")
                .defineInRange("driftPerDay", 0.5, 0.0, 100.0);
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
                MIN_CYCLE_LENGTH.get(),
                MAX_CYCLE_LENGTH.get(),
                MIN_AMPLITUDE.get(),
                MAX_AMPLITUDE.get(),
                NOISE_DECAY.get(),
                NOISE_SCALE.get(),
                MIN_INDEX.get(),
                MAX_INDEX.get(),
                TREND_PER_DAY.get(),
                toPolicyRateParams());
    }

    public static PolicyRateParams toPolicyRateParams() {
        return new PolicyRateParams(
                POLICY_BASE_RATE.get(),
                POLICY_MIN_RATE.get(),
                POLICY_MAX_RATE.get(),
                POLICY_DECISION_INTERVAL_DAYS.get(),
                POLICY_TARGET_SENSITIVITY.get(),
                POLICY_SMALL_STEP.get(),
                POLICY_LARGE_STEP.get(),
                POLICY_SMALL_THRESHOLD.get(),
                POLICY_LARGE_THRESHOLD.get());
    }

    public static ReputationParams toReputationParams() {
        return new ReputationParams(
                REPUTATION_ON_TIME_GAIN.get(),
                REPUTATION_ON_TIME_URGENCY_FACTOR.get(),
                REPUTATION_LATE_LOSS.get(),
                REPUTATION_FAILURE_LOSS.get(),
                REPUTATION_FOUNDER_PROTECTION_FACTOR.get(),
                REPUTATION_DRIFT_PER_DAY.get());
    }

    public static ProgressParams toProgressParams() {
        return new ProgressParams(DAILY_DECAY.get(), PROGRESS_FLOOR.get());
    }

    public static PriceProcessParams toPriceProcessParams() {
        return new PriceProcessParams(MEAN_REVERSION.get(), PURCHASE_PRESSURE.get(), MIN_FACTOR.get(), MAX_FACTOR.get());
    }
}
