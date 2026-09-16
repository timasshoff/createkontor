package com.timder.kontor.core.macro;

/**
 * Immutable parameters of the technical progress.
 * @param dailyDecay How much processing cost shrinks every day
 * @param floor Processing cost will never fall below this value
 */
public record ProgressParams(
        double dailyDecay,
        double floor
) {

    public ProgressParams {
        if (dailyDecay < 0 || dailyDecay >= 1) throw new IllegalArgumentException("dailyDecay must be within [0, 1).");
        if (floor <= 0 || floor > 1) throw new IllegalArgumentException("floor must be within (0, 1].");
    }

    /*
     * The following code is AI generated. Might be changed in the future.
     */

    public static ProgressParams standard() {
        return new ProgressParams(0.002, 0.60);
    }
}
