package com.timder.kontor.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Every data needed to show one line chart.
 */
public record ChartSpec(
        String title,
        List<ChartSeries> series,
        List<ChartReferenceLine> referenceLines,
        boolean includeZero,
        String xUnit,
        String xZeroLabel,
        String yUnit,
        int tooltipDecimals,
        List<String> pointLabels
) {
    public static final int MAX_SERIES = 6;
    public static final int MAX_POINTS = 5000;
    public static final int MAX_REFERENCE_LINES = 8;
    public static final int MAX_TOOLTIP_DECIMALS = 6;

    public static final int MAX_TITLE_LENGTH = 128;
    public static final int MAX_UNIT_LENGTH = 16;
    public static final int MAX_LABEL_LENGTH = 64;
    public static final int MAX_POINT_LABEL_LENGTH = 32;

    public ChartSpec {
        Objects.requireNonNull(title, "title must not be null.");
        Objects.requireNonNull(xUnit, "xUnit must not be null.");
        Objects.requireNonNull(xZeroLabel, "xZeroLabel must not be null.");
        Objects.requireNonNull(yUnit, "yUnit must not be null.");
        series = List.copyOf(series);
        referenceLines = List.copyOf(referenceLines);
        pointLabels = List.copyOf(pointLabels);

        limit(title, MAX_TITLE_LENGTH, "title");
        limit(xUnit, MAX_UNIT_LENGTH, "xUnit");
        limit(xZeroLabel, MAX_UNIT_LENGTH, "xZeroLabel");
        limit(yUnit, MAX_UNIT_LENGTH, "yUnit");

        if (series.size() > MAX_SERIES) {
            throw new IllegalArgumentException("too many series: " + series.size() + " (max " + MAX_SERIES + ")");
        }
        for (ChartSeries s : series) {
            limit(s.label(), MAX_LABEL_LENGTH, "series label");
            if (s.size() > MAX_POINTS) {
                throw new IllegalArgumentException("series '" + s.label() + "' has too many points: "
                        + s.size() + " (max " + MAX_POINTS + ")");
            }
        }

        if (referenceLines.size() > MAX_REFERENCE_LINES) {
            throw new IllegalArgumentException("too many reference lines: " + referenceLines.size());
        }
        for (ChartReferenceLine line : referenceLines) {
            limit(line.label(), MAX_LABEL_LENGTH, "reference line label");
        }

        if (tooltipDecimals < 0 || tooltipDecimals > MAX_TOOLTIP_DECIMALS) {
            throw new IllegalArgumentException("tooltipDecimals out of range: " + tooltipDecimals);
        }

        if (!pointLabels.isEmpty()) {
            if (series.isEmpty() || pointLabels.size() != series.get(0).size()) {
                throw new IllegalArgumentException("pointLabels must be empty or have one entry per point of the first series.");
            }
            for (String label : pointLabels) {
                limit(label, MAX_POINT_LABEL_LENGTH, "point label");
            }
        }
    }

    public static Builder builder(String title) {
        return new Builder(title);
    }

    private static void limit(String text, int max, String what) {
        if (text.length() > max) {
            throw new IllegalArgumentException(what + " is too long: " + text.length() + " (max " + max + ")");
        }
    }

    public static final class Builder {
        private final String title;
        private final List<ChartSeries> series = new ArrayList<>();
        private final List<ChartReferenceLine> referenceLines = new ArrayList<>();
        private boolean includeZero = false;
        private String xUnit = "";
        private String xZeroLabel = "";
        private String yUnit = "";
        private int tooltipDecimals = 2;
        private List<String> pointLabels = List.of();

        private Builder(String title) {
            this.title = title;
        }

        public Builder series(ChartSeries series) {
            this.series.add(series);
            return this;
        }

        public Builder referenceLine(String label, double value, int color) {
            this.referenceLines.add(new ChartReferenceLine(label, value, color));
            return this;
        }

        public Builder includeZero(boolean includeZero) {
            this.includeZero = includeZero;
            return this;
        }

        public Builder xAxis(String unit, String zeroLabel) {
            this.xUnit = unit;
            this.xZeroLabel = zeroLabel;
            return this;
        }

        public Builder yUnit(String unit) {
            this.yUnit = unit;
            return this;
        }

        public Builder tooltipDecimals(int decimals) {
            this.tooltipDecimals = decimals;
            return this;
        }

        public Builder pointLabels(List<String> labels) {
            this.pointLabels = labels;
            return this;
        }

        public ChartSpec build() {
            return new ChartSpec(title, series, referenceLines, includeZero, xUnit, xZeroLabel, yUnit, tooltipDecimals, pointLabels);
        }
    }
}
