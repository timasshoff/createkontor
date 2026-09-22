package com.timder.kontor.core.macro;

/**
 * Immutable parameters of the policy rate.
 *
 * @param baseRate The rate the world starts with and goes back to in a normal economy
 * @param minRate The lowest possible rate
 * @param maxRate The highest possible rate
 * @param decisionIntervalDays Each decision on the rate is made in this interval
 * @param targetSensitivity How strongly the target reacts to the cycle index
 * @param smallStep How far the rate moves when the target is further away than the small threshold
 * @param largeStep How far the rate moves when the target is further away than the large threshold
 * @param smallThreshold Below this threshold the rate does not change
 * @param largeThreshold Above this threshold the rate moves by large steps
 */
public record PolicyRateParams(
        double baseRate,
        double minRate,
        double maxRate,
        int decisionIntervalDays,
        double targetSensitivity,
        double smallStep,
        double largeStep,
        double smallThreshold,
        double largeThreshold
) {
    public PolicyRateParams {
        if (!(minRate > 0.0)) throw new IllegalArgumentException("minRate must be positive.");
        if (!(maxRate > minRate)) throw new IllegalArgumentException("maxRate must be larger than minRate.");
        if (!(baseRate >= minRate && baseRate <= maxRate)) throw new IllegalArgumentException("baseRate must be within [minRate, maxRate].");
        if (decisionIntervalDays < 1) throw new IllegalArgumentException("decisionIntervalDays must be at least one day.");
        if (!(targetSensitivity >= 0.0)) throw new IllegalArgumentException("targetSensitivity must not be negative.");
        if (!(smallStep > 0.0)) throw new IllegalArgumentException("smallStep must be positive.");
        if (!(largeStep >= smallStep)) throw new IllegalArgumentException("largeStep must not be smaller than smallStep.");
        if (!(smallThreshold > 0.0)) throw new IllegalArgumentException("smallThreshold must be positive.");
        if (!(largeThreshold > smallThreshold)) throw new IllegalArgumentException("largeThreshold must be larger than smallThreshold.");
    }

    /*
    The following code is AI generated
     */

    public static PolicyRateParams standard() {
        return new PolicyRateParams(
                0.0020,    // r0 = 0.20 %
                0.0005,    // lowest target 0.05 %
                0.0050,    // highest target 0.50 %
                14,        // every 14 days
                2.0,       // r* = r0 * (1 + 2 * (K - 1))
                0.0005,    // small step 0.05 percentage points
                0.0010,    // large step 0.10 percentage points
                0.00025,   // stay if the target is at most 0.025 percentage points away
                0.0015     // large step if the target is more than 0.15 percentage points away
        );
    }
}
