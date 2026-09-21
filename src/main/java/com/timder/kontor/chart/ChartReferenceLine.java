package com.timder.kontor.chart;

import java.util.Objects;

/**
 * A dashed horizontal line at a fixed y value.
 */
public record ChartReferenceLine(String label, double value, int color) {
    public ChartReferenceLine {
        Objects.requireNonNull(label, "label must not be null.");
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("reference line value must be finite: " + value);
        }
    }
}
