package com.timder.kontor.client.ui.element;

import java.util.Locale;

/**
 * A nicely formatted axis scale
 * @param min Lowest tick (multiple of step)
 * @param max Highest tick (multiple of step)
 * @param step Distance between two ticks
 * @param decimals How many decimals a label needs
 */
public record NiceScale(
        double min,
        double max,
        double step,
        int decimals
) {

    private static final double EPS = 1e-9;

    /**
     * Computes a scale for a given data range.
     * @param dataMin Smallest value that has to fit
     * @param dataMax Highest value that has to fit
     * @param targetTickCount Roughly how many ticks to be displayed (min 2)
     * @return
     */
    public static NiceScale of(double dataMin, double dataMax, int targetTickCount) {
        if (!Double.isFinite(dataMin) || !Double.isFinite(dataMax)) {
            throw new IllegalArgumentException("data range must be finite: " + dataMin + ".." + dataMax);
        }
        if (targetTickCount < 2) {
            throw new IllegalArgumentException("targetTickCount must be at least 2.");
        }

        double lo = Math.min(dataMin, dataMax);
        double hi = Math.max(dataMin, dataMax);

        if (hi - lo <= Math.abs(hi) * EPS) {
            double pad = hi == 0.0 ? 1.0 : Math.abs(hi) * 0.05;
            lo -= pad;
            hi += pad;
        }

        double rawStep = (hi - lo) / (targetTickCount - 1);
        int exponent = (int) Math.floor(Math.log10(rawStep));
        double fraction = rawStep / Math.pow(10.0, exponent);

        double niceFraction;
        if (fraction <= 1.0 + EPS) {
            niceFraction = 1.0;
        } else if (fraction <= 2.0 + EPS) {
            niceFraction = 2.0;
        } else if (fraction <= 5.0 + EPS) {
            niceFraction = 5.0;
        } else {
            niceFraction = 1.0;
            exponent++; // 10 * 10^e is 1 * 10^(e+1)
        }

        double step = niceFraction * Math.pow(10.0, exponent);
        double niceMin = Math.floor(lo / step + EPS) * step + 0.0;
        double niceMax = Math.ceil(hi / step - EPS) * step + 0.0;

        return new NiceScale(niceMin, niceMax, step, Math.max(0, -exponent));
    }

    public int tickCount() {
        return (int) Math.round((max - min) / step) + 1;
    }

    public double[] ticks() {
        double[] ticks = new double[tickCount()];
        for (int i = 0; i < ticks.length; i++) {
            ticks[i] = min + i * step;
        }
        return ticks;
    }

    public double[] ticksWithin(double lowest, double highest) {
        double tolerance = step * EPS;
        return java.util.Arrays.stream(ticks())
                .filter(t -> t >= lowest - tolerance && t <= highest + tolerance)
                .toArray();
    }

    /**
     * Formats a tick label.
     * @param value The value
     * @param decimals The amount of decimals
     * @return The text
     */
    public static String format(double value, int decimals) {
        if (Math.abs(value) < 0.5 * Math.pow(10.0, -decimals)) {
            value = 0.0;
        }
        return String.format(Locale.ROOT, "%." + decimals + "f", value);
    }
}
