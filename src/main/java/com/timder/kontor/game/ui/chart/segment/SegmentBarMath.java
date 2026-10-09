package com.timder.kontor.game.ui.chart.segment;

import java.util.Locale;

public final class SegmentBarMath {

    private static final double DARK_TEXT_THRESHOLD = 150.0;
    private static final int DARK_TEXT = 0xFF101010;
    private static final int LIGHT_TEXT = 0xFFFFFFFF;

    public static float[] edges(double[] values, float x, float width) {
        double total = 0.0;
        for (double value : values) {
            total += value;
        }
        if (!(total > 0.0)) {
            throw new IllegalArgumentException("the sum of the values must be positive.");
        }

        long left = Math.round((double) x);
        long right = Math.round((double) x + width);
        long span = right - left;

        float[] edges = new float[values.length + 1];
        edges[0] = left;
        double cumulative = 0.0;
        for (int i = 0; i < values.length; i++) {
            cumulative += values[i];
            edges[i + 1] = left + Math.round(cumulative / total * span);
        }
        edges[values.length] = right;
        return edges;
    }

    public static int indexAt(float[] edges, float x) {
        for (int i = 0; i < edges.length - 1; i++) {
            if (x >= edges[i] && x < edges[i + 1]) {
                return i;
            }
        }
        return -1;
    }

    public static String percent(double value, double total, int decimals) {
        double share = total > 0.0 ? value / total * 100.0 : 0.0;
        return String.format(Locale.ROOT, "%." + decimals + "f %%", share);
    }

    public static String amount(double value, String unit, int decimals) {
        return String.format(Locale.ROOT, "%." + decimals + "f", value) + unit;
    }

    /**
     * @param background The colour behind the text (ARGB)
     * @return Dark text on bright backgrounds, white text otherwise
     */
    public static int readableTextColor(int background) {
        int red = (background >> 16) & 0xFF;
        int green = (background >> 8) & 0xFF;
        int blue = background & 0xFF;
        double luminance = 0.299 * red + 0.587 * green + 0.114 * blue;
        return luminance > DARK_TEXT_THRESHOLD ? DARK_TEXT : LIGHT_TEXT;
    }
}
