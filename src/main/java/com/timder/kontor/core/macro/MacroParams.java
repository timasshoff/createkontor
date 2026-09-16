package com.timder.kontor.core.macro;

/**
 * Immutable parameters for the cycle.
 *
 * @param minCycleLength Shortest possible cycle in days
 * @param maxCycleLength Longest possible cycle in days
 * @param minAmplitude Weakest possible swing
 * @param maxAmplitude Strongest possible swing
 * @param noiseDecay How much noise decays from the previous day
 * @param noiseScale How much noise is added each day
 * @param minIndex Min cycle index
 * @param maxIndex Max cycle index
 * @param trendPerDay Long term growth of demand per day
 */
public record MacroParams(
        int minCycleLength,
        int maxCycleLength,
        double minAmplitude,
        double maxAmplitude,
        double noiseDecay,
        double noiseScale,
        double minIndex,
        double maxIndex,
        double trendPerDay
) {

    public MacroParams {
        if (minCycleLength < 1) throw new IllegalArgumentException("minCycleLength must be at least one day.");
        if (maxCycleLength < minCycleLength) throw new IllegalArgumentException("maxCycleLength must not be smaller than minCycleLength.");
        if (minAmplitude < 0) throw new IllegalArgumentException("minAmplitude must not be negative.");
        if (maxAmplitude < minAmplitude) throw new IllegalArgumentException("maxAmplitude must not be smaller than minAmplitude.");
        if (noiseDecay < 0 || noiseDecay >= 1) throw new IllegalArgumentException("noiseDecay must be within [0, 1).");
        if (minIndex <= 0) throw new IllegalArgumentException("minIndex must be positive.");
        if (maxIndex <= minIndex) throw new IllegalArgumentException("maxIndex must be larger than minIndex.");
    }

    // AI Generated values
    public static MacroParams standard() {
        return new MacroParams(40, 80, 0.08, 0.18, 0.8, 0.01, 0.70, 1.30, 0.0015);
    }
}
