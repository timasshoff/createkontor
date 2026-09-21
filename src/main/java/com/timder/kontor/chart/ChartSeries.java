package com.timder.kontor.chart;

import java.util.Objects;

/**
 * One line of a chart.
 */
public final class ChartSeries {

    private final String label;
    private final int color;
    private final double[] x;
    private final double[] y;

    /**
     * @param label Name shown in legend and the tooltip
     * @param color Colour of the line
     * @param x X values to display
     * @param y Y values to display
     */
    public ChartSeries(String label, int color, double[] x, double[] y) {
        this.label = Objects.requireNonNull(label, "label must not be null.");
        this.color = color;
        if (x.length != y.length) {
            throw new IllegalArgumentException("x and y need the same length: " + x.length + " vs " + y.length);
        }
        for (int i = 0; i < x.length; i++) {
            if (!Double.isFinite(x[i])) {
                throw new IllegalArgumentException("x[" + i + "] is not finite: " + x[i]);
            }
            if (i > 0 && x[i] < x[i - 1]) {
                throw new IllegalArgumentException("x must be ascending, but x[" + i + "] < x[" + (i - 1) + "]");
            }
            if (Double.isInfinite(y[i])) {
                throw new IllegalArgumentException("y[" + i + "] is infinite.");
            }
        }
        this.x = x.clone();
        this.y = y.clone();
    }

    public String label() {
        return label;
    }

    public int color() {
        return color;
    }

    public int size() {
        return x.length;
    }

    public double x(int index) {
        return x[index];
    }

    public double y(int index) {
        return y[index];
    }
}
