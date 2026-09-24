package com.timder.kontor.config;

import com.timder.kontor.core.company.request.ReputationParams;
import com.timder.kontor.core.company.request.RequestParams;
import com.timder.kontor.core.macro.MacroParams;
import com.timder.kontor.core.macro.PolicyRateParams;
import com.timder.kontor.core.macro.ProgressParams;
import com.timder.kontor.core.raw.PriceProcessParams;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

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

    public static final ModConfigSpec.ConfigValue<List<? extends String>> REQUEST_QUANTITY_STEPS;
    public static final ModConfigSpec.DoubleValue REQUEST_URGENCY_PROBABILITY;
    public static final ModConfigSpec.DoubleValue REQUEST_DEADLINE_URGENCY_FACTOR;
    public static final ModConfigSpec.DoubleValue REQUEST_PRICE_URGENCY_FACTOR;
    public static final ModConfigSpec.DoubleValue REQUEST_OFFER_DURATION_URGENCY_FACTOR;
    public static final ModConfigSpec.LongValue REQUEST_DEADLINE_BASE_TICKS;
    public static final ModConfigSpec.LongValue REQUEST_DEADLINE_FLOOR_TICKS;
    public static final ModConfigSpec.LongValue REQUEST_OFFER_DURATION_BASE_TICKS;
    public static final ModConfigSpec.DoubleValue REQUEST_GRACE_PERIOD_PORTION;
    public static final ModConfigSpec.DoubleValue REQUEST_QUANTITY_DISCOUNT_FLOOR;
    public static final ModConfigSpec.DoubleValue REQUEST_QUANTITY_DISCOUNT_PER_STEP;
    public static final ModConfigSpec.DoubleValue REQUEST_WALK_IN_PRICE_THRESHOLD;

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

        RequestParams requestDefaults = RequestParams.standard();
        builder.comment("Requests: what a request looks like (K 12.4). Changes apply to requests that arrive from now on").push("request");
        REQUEST_QUANTITY_STEPS = builder.comment("Quantity factors as 'factor:weight'. The weights are relative and are divided by their sum, they need not add up to 100")
                .defineList("quantitySteps",
                        requestDefaults.quantitySteps().stream().map(QuantityStepFormat::format).toList(),
                        () -> "1:1",
                        QuantityStepFormat::isValid);
        REQUEST_URGENCY_PROBABILITY = builder.comment("Probability that a request is urgent")
                .defineInRange("urgencyProbability", requestDefaults.urgencyProbability(), 0.0, 1.0);
        REQUEST_DEADLINE_URGENCY_FACTOR = builder.comment("An urgent request has (1 - x*urgency) of the normal deadline")
                .defineInRange("deadlineUrgencyFactor", requestDefaults.deadlineUrgencyFactor(), 0.0, 1.0);
        REQUEST_PRICE_URGENCY_FACTOR = builder.comment("An urgent request pays (1 + x*urgency) of the normal price")
                .defineInRange("priceUrgencyFactor", requestDefaults.priceUrgencyFactor(), 0.0, Double.MAX_VALUE);
        REQUEST_OFFER_DURATION_URGENCY_FACTOR = builder.comment("An urgent request stays (1 - x*urgency) of the normal time on the board")
                .defineInRange("offerDurationUrgencyFactor", requestDefaults.offerDurationUrgencyFactor(), 0.0, 0.99);
        REQUEST_DEADLINE_BASE_TICKS = builder.comment("Base of the deadline in ticks, before the manufacturing time of the quantity is added")
                .defineInRange("deadlineBaseTicks", requestDefaults.deadlineBaseTicks(), 1L, Long.MAX_VALUE);
        REQUEST_DEADLINE_FLOOR_TICKS = builder.comment("No deadline is shorter than this many ticks")
                .defineInRange("deadlineFloorTicks", requestDefaults.deadlineFloorTicks(), 1L, Long.MAX_VALUE);
        REQUEST_OFFER_DURATION_BASE_TICKS = builder.comment("How many ticks a normal request stays on the board")
                .defineInRange("offerDurationBaseTicks", requestDefaults.offerDurationBaseTicks(), 100L, Long.MAX_VALUE);
        REQUEST_GRACE_PERIOD_PORTION = builder.comment("Portion of the deadline that is granted as grace period")
                .defineInRange("gracePeriodPortion", requestDefaults.gracePeriodPortion(), 0.01, 0.99);
        REQUEST_QUANTITY_DISCOUNT_FLOOR = builder.comment("Lowest price factor of the quantity discount")
                .defineInRange("quantityDiscountFloor", requestDefaults.quantityDiscountFloor(), 0.01, 1.0);
        REQUEST_QUANTITY_DISCOUNT_PER_STEP = builder.comment("Discount per quantity factor above 1")
                .defineInRange("quantityDiscountPerStep", requestDefaults.quantityDiscountPerStep(), 0.0, 1.0);
        REQUEST_WALK_IN_PRICE_THRESHOLD = builder.comment("List price relative to the market price up to which at least one request per day is guaranteed")
                .defineInRange("walkInPriceThreshold", requestDefaults.walkInPriceThreshold(), 1.0001, Double.MAX_VALUE);
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

    public static RequestParams toRequestParams() {
        return new RequestParams(
                QuantityStepFormat.toSteps(REQUEST_QUANTITY_STEPS.get()),
                REQUEST_URGENCY_PROBABILITY.get(),
                REQUEST_DEADLINE_URGENCY_FACTOR.get(),
                REQUEST_PRICE_URGENCY_FACTOR.get(),
                REQUEST_OFFER_DURATION_URGENCY_FACTOR.get(),
                REQUEST_DEADLINE_BASE_TICKS.get(),
                REQUEST_DEADLINE_FLOOR_TICKS.get(),
                REQUEST_OFFER_DURATION_BASE_TICKS.get(),
                REQUEST_GRACE_PERIOD_PORTION.get(),
                REQUEST_QUANTITY_DISCOUNT_FLOOR.get(),
                REQUEST_QUANTITY_DISCOUNT_PER_STEP.get(),
                REQUEST_WALK_IN_PRICE_THRESHOLD.get());
    }
}
