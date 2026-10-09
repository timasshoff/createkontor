package com.timder.kontor.game.ui.chart.segment;

import java.util.Objects;

public record SegmentBarSegment(String label, int color, double value, String description) {
    public SegmentBarSegment {
        Objects.requireNonNull(label, "label must not be null.");
        Objects.requireNonNull(description, "description must not be null.");
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("value must be finite: " + value);
        }
        if (value < 0.0) {
            throw new IllegalArgumentException("value must not be negative: " + value);
        }
    }

    public SegmentBarSegment(String label, int color, double value) {
        this(label, color, value, "");
    }
}
