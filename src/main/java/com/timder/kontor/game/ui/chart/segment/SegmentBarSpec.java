package com.timder.kontor.game.ui.chart.segment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Every data needed to show one segment bar: a single horizontal bar split into sections.
 *
 * @param title Heading above the bar
 * @param segments The sections, from left to right
 * @param display How the value of a section is written
 * @param unit Appended to the value when the display is value
 * @param decimals Digits after the decimal point
 */
public record SegmentBarSpec(
        String title,
        List<SegmentBarSegment> segments,
        Display display,
        String unit,
        int decimals
) {
    public enum Display {
        PERCENT,
        VALUE
    }

    public static final int MAX_SEGMENTS = 6;
    public static final int MAX_DECIMALS = 4;
    public static final int MAX_TITLE_LENGTH = 128;
    public static final int MAX_UNIT_LENGTH = 16;
    public static final int MAX_LABEL_LENGTH = 64;
    public static final int MAX_DESCRIPTION_LENGTH = 256;

    public SegmentBarSpec {
        Objects.requireNonNull(title, "title must not be null.");
        Objects.requireNonNull(segments, "segments must not be null.");
        Objects.requireNonNull(display, "display must not be null.");
        Objects.requireNonNull(unit, "unit must not be null.");
        segments = List.copyOf(segments);

        limit(title, MAX_TITLE_LENGTH, "title");
        limit(unit, MAX_UNIT_LENGTH, "unit");
        if (segments.size() > MAX_SEGMENTS) {
            throw new IllegalArgumentException("too many segments: " + segments.size() + " (max " + MAX_SEGMENTS + ")");
        }
        for (SegmentBarSegment segment : segments) {
            limit(segment.label(), MAX_LABEL_LENGTH, "segment label");
            limit(segment.description(), MAX_DESCRIPTION_LENGTH, "segment description");
        }
        if (decimals < 0 || decimals > MAX_DECIMALS) {
            throw new IllegalArgumentException("decimals out of range: " + decimals);
        }
    }

    public static Builder builder(String title) {
        return new Builder(title);
    }

    /**
     * @return The sum of all section values
     */
    public double total() {
        double total = 0.0;
        for (SegmentBarSegment segment : segments) {
            total += segment.value();
        }
        return total;
    }

    /**
     * @return True, if there is something to draw
     */
    public boolean hasData() {
        return total() > 0.0;
    }

    /**
     * @param index Index of the section
     * @return The value of the section as it is written in the bar, legend and tooltip
     */
    public String valueText(int index) {
        SegmentBarSegment segment = segments.get(index);
        return switch (display) {
            case PERCENT -> SegmentBarMath.percent(segment.value(), total(), decimals);
            case VALUE -> SegmentBarMath.amount(segment.value(), unit, decimals);
        };
    }

    private static void limit(String text, int max, String what) {
        if (text.length() > max) {
            throw new IllegalArgumentException(what + " is too long: " + text.length() + " (max " + max + ")");
        }
    }

    public static final class Builder {
        private final String title;
        private final List<SegmentBarSegment> segments = new ArrayList<>();
        private Display display = Display.PERCENT;
        private String unit = "";
        private int decimals = 1;

        private Builder(String title) {
            this.title = title;
        }

        public Builder segment(String label, int color, double value) {
            segments.add(new SegmentBarSegment(label, color, value));
            return this;
        }

        public Builder segment(String label, int color, double value, String description) {
            segments.add(new SegmentBarSegment(label, color, value, description));
            return this;
        }

        public Builder display(Display display) {
            this.display = display;
            return this;
        }

        public Builder unit(String unit) {
            this.unit = unit;
            return this;
        }

        public Builder decimals(int decimals) {
            this.decimals = decimals;
            return this;
        }

        public SegmentBarSpec build() {
            return new SegmentBarSpec(title, segments, display, unit, decimals);
        }
    }
}
