package com.timder.kontor.game.ui.chart.classic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Every data needed to show one chart.
 */
public record ChartSpec(
        String title,
        ChartKind kind,
        List<ChartSeries> series,
        List<ChartReferenceLine> referenceLines,
        boolean includeZero,
        String xUnit,
        String xZeroLabel,
        String yUnit,
        int tooltipDecimals,
        List<String> pointLabels,
        List<Integer> timeRangeOptionsDays,
        int defaultTimeRangeDays,
        String xRangeUnit,
        int pointsPerRangeUnit,
        boolean showXAxis
) {
    public static final int MAX_SERIES = 6;
    public static final int MAX_POINTS = 5000;
    public static final int MAX_REFERENCE_LINES = 8;
    public static final int MAX_TOOLTIP_DECIMALS = 6;
    public static final int MAX_TIME_RANGE_OPTIONS = 8;

    public static final int MAX_TITLE_LENGTH = 128;
    public static final int MAX_UNIT_LENGTH = 16;
    public static final int MAX_LABEL_LENGTH = 64;
    public static final int MAX_POINT_LABEL_LENGTH = 32;

    public ChartSpec {
        Objects.requireNonNull(title, "title must not be null.");
        Objects.requireNonNull(kind, "kind must not be null.");
        Objects.requireNonNull(xUnit, "xUnit must not be null.");
        Objects.requireNonNull(xZeroLabel, "xZeroLabel must not be null.");
        Objects.requireNonNull(yUnit, "yUnit must not be null.");
        Objects.requireNonNull(xRangeUnit, "xRangeUnit must not be null.");
        series = List.copyOf(series);
        referenceLines = List.copyOf(referenceLines);
        pointLabels = List.copyOf(pointLabels);
        timeRangeOptionsDays = List.copyOf(timeRangeOptionsDays);

        limit(title, MAX_TITLE_LENGTH, "title");
        limit(xUnit, MAX_UNIT_LENGTH, "xUnit");
        limit(xZeroLabel, MAX_UNIT_LENGTH, "xZeroLabel");
        limit(yUnit, MAX_UNIT_LENGTH, "yUnit");
        limit(xRangeUnit, MAX_UNIT_LENGTH, "xRangeUnit");

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

        if (kind == ChartKind.STACKED_BAR) {
            validateStackedBarSeries(series);
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

        if (timeRangeOptionsDays.size() > MAX_TIME_RANGE_OPTIONS) {
            throw new IllegalArgumentException("too many time range options: " + timeRangeOptionsDays.size()
                    + " (max " + MAX_TIME_RANGE_OPTIONS + ")");
        }
        for (int days : timeRangeOptionsDays) {
            if (days <= 0) {
                throw new IllegalArgumentException("time range option must be positive: " + days);
            }
        }
        if (pointsPerRangeUnit < 1) {
            throw new IllegalArgumentException("pointsPerRangeUnit must be >= 1: " + pointsPerRangeUnit);
        }
        if (!timeRangeOptionsDays.isEmpty() && !timeRangeOptionsDays.contains(defaultTimeRangeDays)) {
            throw new IllegalArgumentException("defaultTimeRangeDays must be one of timeRangeOptionsDays.");
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

    private static void validateStackedBarSeries(List<ChartSeries> series) {
        if (series.isEmpty()) {
            return;
        }
        int size = series.get(0).size();
        for (ChartSeries s : series) {
            if (s.size() != size) {
                throw new IllegalArgumentException("stacked bar series must share the same x grid: '"
                        + s.label() + "' has " + s.size() + " points, expected " + size);
            }
        }
        for (int i = 0; i < size; i++) {
            double x0 = series.get(0).x(i);
            for (ChartSeries s : series) {
                if (Math.abs(s.x(i) - x0) > 1e-9) {
                    throw new IllegalArgumentException("stacked bar series must share the same x grid: "
                            + "mismatch at index " + i + " in '" + s.label() + "' (" + s.x(i) + " vs " + x0 + ")");
                }
            }
        }
        for (ChartSeries s : series) {
            for (int i = 0; i < s.size(); i++) {
                double v = s.y(i);
                if (!Double.isNaN(v) && v < 0) {
                    throw new IllegalArgumentException("stacked bar values must be >= 0 or NaN, got "
                            + v + " in '" + s.label() + "' at index " + i);
                }
            }
        }
    }

    public static final class Builder {
        private final String title;
        private ChartKind kind = ChartKind.LINE;
        private final List<ChartSeries> series = new ArrayList<>();
        private final List<ChartReferenceLine> referenceLines = new ArrayList<>();
        private boolean includeZero = false;
        private String xUnit = "";
        private String xZeroLabel = "";
        private String yUnit = "";
        private int tooltipDecimals = 2;
        private List<String> pointLabels = List.of();
        private List<Integer> xRangeOptions = List.of();
        private int defaultXRange = 0;
        private String xRangeUnit;
        private int pointsPerRangeUnit = 1;
        private boolean showXAxis = true;

        private Builder(String title) {
            this.title = title;
        }

        public Builder kind(ChartKind kind) {
            this.kind = kind;
            return this;
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

        public Builder hideXAxis() {
            this.showXAxis = false;
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

        /**
         * Turns on the time-range dropdown for this chart
         *
         * @param options the selectable windows, each a count of the most recent points to show, e.g. [5, 10, 30, 100, 360]
         * @param defaultRange which of options is preselected
         */
        public Builder xRangeOptions(List<Integer> options, int defaultRange) {
            this.xRangeOptions = options;
            this.defaultXRange = defaultRange;
            return this;
        }

        public Builder xRangeUnit(String unit) {
            this.xRangeUnit = unit;
            return this;
        }

        public Builder xRangePointsPerUnit(int pointsPerUnit) {
            this.pointsPerRangeUnit = pointsPerUnit;
            return this;
        }

        public ChartSpec build() {
            String resolvedRangeUnit = xRangeUnit != null ? xRangeUnit : xUnit;
            return new ChartSpec(
                    title,
                    kind,
                    series,
                    referenceLines,
                    includeZero,
                    xUnit,
                    xZeroLabel,
                    yUnit,
                    tooltipDecimals,
                    pointLabels,
                    xRangeOptions,
                    defaultXRange,
                    resolvedRangeUnit,
                    pointsPerRangeUnit,
                    showXAxis
            );
        }
    }
}
