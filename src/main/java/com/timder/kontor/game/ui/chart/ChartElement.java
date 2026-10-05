package com.timder.kontor.game.ui.chart;

import com.lowdragmc.lowdraglib2.gui.LDLibFonts;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Selector;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Vector2f;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.DoubleFunction;
import java.util.function.IntFunction;

public class ChartElement extends UIElement {

    /**
     * Turns a tick value into label text
     */
    @FunctionalInterface
    public interface ValueFormatter {
        String format(double value, int decimals);
    }

    private record ReferenceLine(String label, double value, int color) {}

    private record TipLine(int color, String text) {}

    private record Dot(float x, float y, int color) {}

    private record Plot(float left, float top, float right, float bottom, double xMin, double xMax, double yMin, double yMax) {
        float width() {
            return right - left;
        }

        float height() {
            return bottom - top;
        }

        float x(double value) {
            return left + (float) ((value - xMin) / (xMax - xMin)) * width();
        }

        float y(double value) {
            return bottom - (float) ((value - yMin) / (yMax - yMin)) * height();
        }
    }

    private record LegendHit(float x, float y, float width, float height, String label) {
        boolean contains(float px, float py) {
            return px >= x && px <= x + width && py >= y && py <= y + height;
        }
    }

    private static final int PANEL_BACKGROUND = 0xFF1B1B1B;
    private static final int PLOT_BACKGROUND = 0xFF262626;
    private static final int GRID = 0xFF3A3A3A;
    private static final int AXIS = 0xFF8A8A8A;
    private static final int TEXT = 0xFFE0E0E0;
    private static final int TEXT_DIM = 0xFFA0A0A0;
    private static final int CURSOR = 0x80FFFFFF;
    private static final int DOT_OUTLINE = 0xFF000000;
    private static final int TOOLTIP_BACKGROUND = 0xFF212121;
    private static final int TOOLTIP_BORDER = 0xFFFFFFFF;

    private static final float PAD = 6f;
    private static final float MIN_PLOT_SIZE = 20f;
    private static final float LINE_HALF_WIDTH = 0.75f;
    private static final float DASH = 4f;
    private static final float GAP = 3f;
    private static final float SWATCH = 5f;
    private static final float TOOLTIP_PAD = 4f;
    private static final int Y_TICK_TARGET = 6;
    private static final int X_TICK_TARGET = 6;
    private static final float BAR_WIDTH_FRACTION = 0.6f;

    private static final float LEGEND_SWATCH = 8f;
    private static final float LEGEND_GAP = 4f;
    private static final float LEGEND_ENTRY_GAP = 8f;
    private static final float LEGEND_DROPDOWN_GAP = 6f;
    private static final float LEGEND_HIT_PAD = 2f;

    private ChartKind kind = ChartKind.LINE;
    private List<ChartSeries> series = List.of();
    private final List<ReferenceLine> referenceLines = new ArrayList<>();
    @Nullable
    private Component title;
    private boolean includeZero = false;

    private ValueFormatter xFormatter = NiceScale::format;
    private ValueFormatter yFormatter = NiceScale::format;
    private DoubleFunction<String> tooltipValueFormatter = v -> String.format(Locale.ROOT, "%.2f", v);
    @Nullable
    private IntFunction<String> tooltipHeader;

    private List<ChartSeries> fullSeries = List.of();
    private List<String> fullPointLabels = List.of();
    private List<String> pointLabels = List.of();
    @Nullable
    private Selector<Integer> rangeSelector;
    private float rangeSelectorWidth = 0f;
    private int visibleRange = 0;
    private boolean showXAxis = true;

    private final Set<String> hiddenLabels = new HashSet<>();
    private final List<LegendHit> legendHits = new ArrayList<>();
    private List<ChartSeries> shownSeries = List.of();


    public ChartElement() {
        style(s -> s.overflowVisible(false));
        addEventListener(UIEvents.MOUSE_DOWN, this::onMouseDown);
    }

    public static ChartElement from(ChartSpec spec) {
        ChartElement chart = new ChartElement();
        chart.update(spec);
        return chart;
    }

    /**
     * Applies a description to this chart
     * @param spec The chart description
     * @return This chart element
     */
    public ChartElement update(ChartSpec spec) {
        setTitle(spec.title().isEmpty() ? null : Component.literal(spec.title()));
        setKind(spec.kind());
        referenceLines.clear();
        for (ChartReferenceLine line : spec.referenceLines()) {
            addReferenceLine(line.label(), line.value(), line.color());
        }
        setIncludeZero(spec.includeZero());
        setShowXAxis(spec.showXAxis());

        String xUnit = spec.xUnit();
        String xZeroLabel = spec.xZeroLabel();
        String yUnit = spec.yUnit();
        String tooltipFormat = "%." + spec.tooltipDecimals() + "f";
        setXFormatter((value, decimals) ->
                !xZeroLabel.isEmpty() && Math.abs(value) < 1e-9 ? xZeroLabel : NiceScale.format(value, decimals) + xUnit);
        setYFormatter((value, decimals) -> NiceScale.format(value, decimals) + yUnit);
        setTooltipValueFormatter(value -> String.format(Locale.ROOT, tooltipFormat, value) + yUnit);

        setTooltipHeader(spec.pointLabels().isEmpty() ? null : this::pointLabelAt);
        setPointLabels(spec.pointLabels());
        setSeries(spec.series());

        if (rangeSelector == null && !spec.timeRangeOptionsDays().isEmpty()) {
            enableRangeFilter(spec.timeRangeOptionsDays(), spec.defaultTimeRangeDays(), spec.xRangeUnit());
        }
        return this;
    }

    public ChartElement setTitle(@Nullable Component title) {
        this.title = title;
        return this;
    }

    public ChartElement setKind(ChartKind kind) {
        this.kind = kind;
        return this;
    }

    /**
     * Replaces all lines
     * @param series The new chart series
     * @return This chart element
     */
    public ChartElement setSeries(List<ChartSeries> series) {
        this.fullSeries = List.copyOf(series);
        applyXRangeFilter();
        return this;
    }

    public ChartElement setPointLabels(List<String> pointLabels) {
        this.fullPointLabels = List.copyOf(pointLabels);
        applyXRangeFilter();
        return this;
    }

    /**
     * Adds a dashed horizontal line at a fixed y value
     * @param label The label to display
     * @param value The y value
     * @param color The color
     * @return This chart element
     */
    public ChartElement addReferenceLine(String label, double value, int color) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("reference line value must be finite: " + value);
        }
        referenceLines.add(new ReferenceLine(label, value, color));
        return this;
    }

    public ChartElement setIncludeZero(boolean includeZero) {
        this.includeZero = includeZero;
        return this;
    }

    public ChartElement setShowXAxis(boolean showXAxis) {
        this.showXAxis = showXAxis;
        return this;
    }

    public ChartElement setXFormatter(ValueFormatter formatter) {
        this.xFormatter = formatter;
        return this;
    }

    public ChartElement setYFormatter(ValueFormatter formatter) {
        this.yFormatter = formatter;
        return this;
    }

    public ChartElement setTooltipValueFormatter(DoubleFunction<String> formatter) {
        this.tooltipValueFormatter = formatter;
        return this;
    }

    public ChartElement setTooltipHeader(@Nullable IntFunction<String> header) {
        this.tooltipHeader = header;
        return this;
    }

    public ChartElement enableRangeFilter(List<Integer> options, int defaultRange) {
        return enableRangeFilter(options, defaultRange, null);
    }

    public ChartElement enableRangeFilter(List<Integer> options, int defaultRange, @Nullable String labelUnit) {
        if (options.isEmpty()) {
            throw new IllegalArgumentException("options must not be empty.");
        }
        if (!options.contains(defaultRange)) {
            throw new IllegalArgumentException("defaultRange must be one of options.");
        }
        if (rangeSelector != null) {
            removeChild(rangeSelector);
        }

        Font font = LDLibFonts.font();
        int maxTextWidth = 0;
        for (Integer option : options) {
            String text = labelUnit != null ? NiceScale.format(option, 0) + labelUnit : xFormatter.format(option, 0);
            maxTextWidth = Math.max(maxTextWidth, font.width(text));
        }
        int selectorWidth = maxTextWidth + 30;
        this.rangeSelectorWidth = selectorWidth;

        Selector<Integer> selector = new Selector<>();
        selector.setCandidateUIProvider(UIElementProvider.text(value -> {
            if (value == null) {
                return Component.literal("—");
            }
            String text = labelUnit != null
                    ? NiceScale.format(value, 0) + labelUnit
                    : xFormatter.format(value, 0);
            return Component.literal(text);
        }));
        selector.setCandidates(options);
        selector.setOnValueChanged(value -> {
            visibleRange = value;
            applyXRangeFilter();
        });
        selector.setSelected(defaultRange, false);
        selector.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .top(PAD)
                .right(PAD)
                .width(selectorWidth));

        addChild(selector);
        rangeSelector = selector;
        visibleRange = defaultRange;
        applyXRangeFilter();
        return this;
    }

    private void applyXRangeFilter() {
        if (visibleRange <= 0) {
            this.series = fullSeries;
            this.pointLabels = fullPointLabels;
        } else {
            List<ChartSeries> filtered = new ArrayList<>(fullSeries.size());
            for (ChartSeries s : fullSeries) {
                filtered.add(lastPoints(s, visibleRange));
            }
            this.series = filtered;
            this.pointLabels = lastPoints(fullPointLabels, visibleRange);
        }
        updateShownSeries();
        if (rangeSelector != null) {
            rangeSelector.setDisplay(dataRange() != null);
        }
    }

    private static ChartSeries lastPoints(ChartSeries s, int count) {
        int size = s.size();
        if (size <= count) {
            return s;
        }
        int from = size - count;
        double[] x = new double[count];
        double[] y = new double[count];
        for (int i = 0; i < count; i++) {
            x[i] = s.x(from + i);
            y[i] = s.y(from + i);
        }
        return new ChartSeries(s.label(), s.color(), x, y);
    }

    private static List<String> lastPoints(List<String> labels, int count) {
        int size = labels.size();
        if (size <= count) {
            return labels;
        }
        return labels.subList(size - count, size);
    }

    private String pointLabelAt(int index) {
        return index >= 0 && index < pointLabels.size() ? pointLabels.get(index) : "";
    }

    private void updateShownSeries() {
        List<ChartSeries> shown = new ArrayList<>(series.size());
        for (ChartSeries s : series) {
            if (!hiddenLabels.contains(s.label())) {
                shown.add(s);
            }
        }
        if (shown.isEmpty() && !series.isEmpty()) {
            hiddenLabels.clear();
            shown.addAll(series);
        }
        this.shownSeries = shown;
    }

    private void onMouseDown(UIEvent event) {
        if (event.button != 0 || event.target != this) {
            return;
        }
        Vector2f mouse = getLocalMouse(event.x, event.y);
        for (LegendHit hit : legendHits) {
            if (hit.contains(mouse.x, mouse.y)) {
                toggleSeries(hit.label());
                return;
            }
        }
    }

    private void toggleSeries(String label) {
        if (!hiddenLabels.remove(label)) {
            if (shownSeries.size() <= 1) {
                return;
            }
            hiddenLabels.add(label);
        }
        updateShownSeries();
    }

    private static int faded(int argb) {
        return (argb & 0x00FFFFFF) | 0x55000000;
    }

    @Override
    public void drawBackgroundAdditional(GUIContext ctx) {
        super.drawBackgroundAdditional(ctx);

        GuiGraphics g = ctx.graphics;
        Font font = LDLibFonts.font();
        float left = getContentX();
        float top = getContentY();
        float width = getContentWidth();
        float height = getContentHeight();
        float lineHeight = font.lineHeight;

        DrawerHelper.drawSolidRect(g, left, top, width, height, PANEL_BACKGROUND);
        legendHits.clear();

        double[] range = dataRange();
        if (range == null) {
            Component message = title != null
                    ? Component.translatable("ui.createkontor.chart.no_data.detailed", title)
                    : Component.translatable("ui.createkontor.chart.no_data.short");
            List<FormattedCharSequence> lines = font.split(message, Math.round(width - 2f * PAD));
            float textY = top + (height - lines.size() * lineHeight) / 2f;
            for (FormattedCharSequence line : lines) {
                float textX = left + (width - font.width(line)) / 2f;
                LDLibFonts.drawText(g, font, line, textX, textY, TEXT_DIM, false);
                textY += lineHeight;
            }
            return;
        }

        NiceScale yScale = NiceScale.of(range[2], range[3], Y_TICK_TARGET);
        NiceScale xScale = NiceScale.of(range[0], range[1], X_TICK_TARGET);
        double[] yTicks = yScale.ticks();
        double[] xTicks = showXAxis ? xScale.ticksWithin(range[0], range[1]) : new double[0];

        String[] yLabels = new String[yTicks.length];
        float labelWidth = 0f;
        for (int i = 0; i < yTicks.length; i++) {
            yLabels[i] = yFormatter.format(yTicks[i], yScale.decimals());
            labelWidth = Math.max(labelWidth, font.width(yLabels[i]));
        }

        float y = top + PAD;
        if (title != null) {
            LDLibFonts.drawText(g, font, title, Math.round(left + PAD), Math.round(y), TEXT, false);
            y += lineHeight + 4f;
        }
        if (!series.isEmpty()) {
            float legendMx = isHover() ? ctx.localMouseX : Float.NaN;
            float legendMy = isHover() ? ctx.localMouseY : Float.NaN;
            drawLegend(g, font, left + PAD, y, width - 2f * PAD, legendMx, legendMy);
            y += lineHeight + 4f;
        }

        Plot plot = new Plot(
                left + PAD + labelWidth + 4f,
                y,
                left + width - PAD - 2f,
                top + height - PAD - (showXAxis ? lineHeight + 4f : 0f),
                range[0],
                range[1],
                yScale.min(),
                yScale.max()
        );

        if (plot.width() < MIN_PLOT_SIZE || plot.height() < MIN_PLOT_SIZE) {
            return; // too small to draw
        }

        DrawerHelper.drawSolidRect(g, plot.left(), plot.top(), plot.width(), plot.height(), PLOT_BACKGROUND);
        for (int i = 0; i < yTicks.length; i++) {
            float py = Math.round(plot.y(yTicks[i]));
            DrawerHelper.drawSolidRect(g, plot.left(), py, plot.width(), 1f, GRID);
            drawText(g, font, yLabels[i], plot.left() - 4f - font.width(yLabels[i]), py - lineHeight / 2f + 1f, TEXT_DIM);
        }

        for (double tick : xTicks) {
            float exactX = plot.x(tick);
            if (exactX < plot.left() - 0.01f || exactX > plot.right() + 0.01f) {
                continue;
            }
            float px = Math.round(exactX);
            DrawerHelper.drawSolidRect(g, px, plot.top(), 1f, plot.height(), GRID);
            String label = xFormatter.format(tick, xScale.decimals());
            float textWidth = font.width(label);
            float labelX = Math.max(plot.left(), Math.min(px - textWidth / 2f, plot.right() - textWidth));
            drawText(g, font, label, labelX, plot.bottom() + 4f, TEXT_DIM);
        }
        DrawerHelper.drawSolidRect(g, plot.left(), plot.top(), 1f, plot.height(), AXIS);
        DrawerHelper.drawSolidRect(g, plot.left(), plot.bottom(), plot.width(),1f, AXIS);
        g.flush();

        for (ReferenceLine line : referenceLines) {
            drawReferenceLine(g, font, plot, line);
        }
        g.flush();

        if (kind == ChartKind.STACKED_BAR) {
            drawStackedBars(g, plot);
        } else {
            for (ChartSeries s : shownSeries) {
                drawSeries(g, plot, s);
            }
        }
        g.flush();

        if (isHover()) {
            float mx = ctx.localMouseX;
            float my = ctx.localMouseY;
            if (mx >= plot.left() && mx <= plot.right() && my >= plot.top() && my <= plot.bottom()) {
                drawHover(ctx, g, font, plot, mx, my, left, top, width, height);
            }
        }
    }

    /**
     * Computes min and max values for data
     * @return xMin, xMax, yMin and yMax
     */
    @Nullable
    private double[] dataRange() {
        double xMin = Double.POSITIVE_INFINITY;
        double xMax = Double.NEGATIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;

        for (ChartSeries s : shownSeries) {
            for (int i = 0; i < s.size(); i++) {
                double v = s.y(i);
                xMin = Math.min(xMin, s.x(i));
                xMax = Math.max(xMax, s.x(i));
                if (kind == ChartKind.LINE && !Double.isNaN(v)) {
                    yMin = Math.min(yMin, v);
                    yMax = Math.max(yMax, v);
                }
            }
        }
        if (xMin > xMax) {
            return null; // not a single usable point
        }
        if (kind == ChartKind.STACKED_BAR) {
            yMin = 0.0;
            yMax = 0.0;
            int size = shownSeries.isEmpty() ? 0 : shownSeries.get(0).size();
            for (int i = 0; i < size; i++) {
                double sum = 0.0;
                for (ChartSeries s : shownSeries) {
                    double v = s.y(i);
                    if (!Double.isNaN(v)) {
                        sum += v;
                    }
                }
                yMax = Math.max(yMax, sum);
            }
            if (size >= 2) {
                double spacing = (shownSeries.get(0).x(size - 1) - shownSeries.get(0).x(0)) / (size - 1);
                double halfSlot = spacing / 2.0;
                xMin -= halfSlot;
                xMax += halfSlot;
            }
        }
        for (ReferenceLine line : referenceLines) {
            yMin = Math.min(yMin, line.value());
            yMax = Math.max(yMax, line.value());
        }
        if (includeZero) {
            yMin = Math.min(yMin, 0.0);
            yMax = Math.max(yMax, 0.0);
        }
        if (xMax - xMin < 1e-12) {
            xMin -= 0.5;
            xMax += 0.5;
        }
        return new double[]{xMin, xMax, yMin, yMax};
    }

    private static void drawText(GuiGraphics g, Font font, String text, float x, float y, int color) {
        LDLibFonts.drawText(g, font, Component.literal(text), Math.round(x), Math.round(y), color, false);
    }

    private void drawReferenceLine(GuiGraphics g, Font font, Plot plot, ReferenceLine line) {
        float py = Math.round(plot.y(line.value()));
        for (float x = plot.left(); x < plot.right; x += DASH + GAP) {
            DrawerHelper.drawSolidRect(g, Math.round(x), py, Math.min(DASH, plot.right() - x), 1f, line.color());
        }
        float lineHeight = font.lineHeight;
        float textX = plot.right() - font.width(line.label()) - 3f;
        float textY = py - lineHeight - 1f;
        if (textY < plot.top()) {
            textY = py + 2f;
        }
        drawText(g, font, line.label(), textX, textY, line.color());
    }

    private void drawSeries(GuiGraphics g, Plot plot, ChartSeries s) {
        List<Vector2f> run = new ArrayList<>();
        for (int i = 0; i < s.size(); i++) {
            double v = s.y(i);
            if (Double.isNaN(v)) {
                flushRun(g, run, s.color());
                run = new ArrayList<>();
                continue;
            }
            run.add(new Vector2f(plot.x(s.x(i)), plot.y(v)));
        }
        flushRun(g, run, s.color());
    }

    private void drawStackedBars(GuiGraphics g, Plot plot) {
        if (shownSeries.isEmpty()) {
            return;
        }
        ChartSeries first = shownSeries.get(0);
        int size = first.size();
        float barWidth = barWidthPx(plot, first);

        for (int i = 0; i < size; i++) {
            float cx = plot.x(first.x(i));
            float left = cx - barWidth / 2f;
            double cumBefore = 0.0;
            for (ChartSeries s : shownSeries) {
                double v = s.y(i);
                if (Double.isNaN(v)) {
                    continue;
                }
                double cumAfter = cumBefore + v;
                float yTop = plot.y(cumAfter);
                float yBottom = plot.y(cumBefore);
                if (yBottom > yTop) {
                    DrawerHelper.drawSolidRect(g, left, yTop, barWidth, yBottom - yTop, s.color());
                }
                cumBefore = cumAfter;
            }
        }
    }

    private static void flushRun(GuiGraphics g, List<Vector2f> run, int color) {
        if (run.size() >= 2) {
            DrawerHelper.drawLines(g, run, color, color, LINE_HALF_WIDTH);
        } else if (run.size() == 1) {
            Vector2f p = run.get(0);
            DrawerHelper.drawSolidRect(g, Math.round(p.x) - 1f, Math.round(p.y) - 1f, 3f, 3f, color);
        }
    }

    private static float barWidthPx(Plot plot, ChartSeries series) {
        int size = series.size();
        double spacing = size >= 2 ? (series.x(size - 1) - series.x(0)) / (size - 1) : (plot.xMax() - plot.xMin());
        if (spacing <= 0) {
            spacing = plot.xMax() - plot.xMin();
        }
        float slotPx = (float) (spacing * plot.width() / (plot.xMax() - plot.xMin()));
        return slotPx * BAR_WIDTH_FRACTION;
    }

    private void drawHover(GUIContext ctx, GuiGraphics g, Font font, Plot plot, float mx, float my, float left, float top, float width, float height) {
        double hoverX = plot.xMin() + (mx - plot.left()) / plot.width() * (plot.xMax() - plot.xMin());

        List<TipLine> lines = new ArrayList<>();
        List<Dot> dots = new ArrayList<>();
        float cursorX = Float.NaN;
        int headerIndex = -1;
        double headerX = 0.0;

        for (ChartSeries s : shownSeries) {
            if (s.size() == 0) {
                continue;
            }
            int index = nearestIndex(s, hoverX);
            if (Float.isNaN(cursorX)) {
                cursorX = plot.x(s.x(index));
                headerIndex = index;
                headerX = s.x(index);
            }
            double value = s.y(index);
            if (Double.isNaN(value)) {
                continue;
            }
            if (kind != ChartKind.STACKED_BAR) {
                dots.add(new Dot(plot.x(s.x(index)), plot.y(value), s.color()));
            }
            lines.add(new TipLine(s.color(), s.label() + ": " + tooltipValueFormatter.apply(value)));
        }
        if (Float.isNaN(cursorX) || lines.isEmpty()) {
            return;
        }

        DrawerHelper.drawSolidRect(g, Math.round(cursorX), plot.top(), 1f, plot.height(), CURSOR);
        for (Dot dot : dots) {
            float dx = Math.round(dot.x());
            float dy = Math.round(dot.y());
            DrawerHelper.drawSolidRect(g, dx - 3f, dy - 3f, 7f, 7f, DOT_OUTLINE);
            DrawerHelper.drawSolidRect(g, dx - 2f, dy - 2f, 5f, 5f, dot.color());
        }

        String header = tooltipHeader != null ? tooltipHeader.apply(headerIndex) : xFormatter.format(headerX, 2);
        ctx.postRendering(c -> drawTooltip(c.graphics, font, header, lines, mx, my, left, top, width, height));
    }

    private static void drawTooltip(GuiGraphics g, Font font, String header, List<TipLine> lines, float mx, float my, float left, float top, float width, float height) {
        float lineHeight = font.lineHeight;
        float textWidth = font.width(header);
        for (TipLine line : lines) {
            textWidth = Math.max(textWidth, SWATCH + 4f + font.width(line.text()));
        }
        float boxWidth = textWidth + 2f * TOOLTIP_PAD;
        float boxHeight = (lines.size() + 1) * (lineHeight + 1f) - 1f + 2f * TOOLTIP_PAD;

        float bx = mx + 12f;
        if (bx + boxWidth > left + width - 2f) {
            bx = mx - 12f - boxWidth;
        }
        bx = Math.max(left + 2f, bx);
        float by = Math.max(top + 2f, Math.min(my - boxHeight / 2f, top + height - boxHeight - 2f));
        bx = Math.round(bx);
        by = Math.round(by);

        DrawerHelper.drawSolidRect(g, bx - 1f, by - 1f, boxWidth + 2f, boxHeight + 2f, TOOLTIP_BORDER);
        DrawerHelper.drawSolidRect(g, bx, by, boxWidth, boxHeight, TOOLTIP_BACKGROUND);

        float ty = by + TOOLTIP_PAD;
        drawText(g, font, header, bx + TOOLTIP_PAD, ty, TEXT_DIM);
        for (TipLine line : lines) {
            ty += lineHeight + 1f;
            DrawerHelper.drawSolidRect(g, Math.round(bx + TOOLTIP_PAD), Math.round(ty + (lineHeight - SWATCH) / 2f), SWATCH, SWATCH, line.color());
            drawText(g, font, line.text(), bx + TOOLTIP_PAD + SWATCH + 4f, ty, TEXT);
        }
    }

    private void drawLegend(GuiGraphics g, Font font, float x0, float y, float maxWidth, float mx, float my) {
        int count = series.size();

        float dropdownClearance = rangeSelectorWidth > 0f ? rangeSelectorWidth + LEGEND_DROPDOWN_GAP : 0f;
        float legendWidth = maxWidth - dropdownClearance;

        double[] importance = new double[count];
        float[] widths = new float[count];
        Integer[] byImportance = new Integer[count];
        for (int i = 0; i < count; i++) {
            ChartSeries s = series.get(i);
            boolean hidden = hiddenLabels.contains(s.label());
            importance[i] = hidden ? Double.POSITIVE_INFINITY : kind == ChartKind.STACKED_BAR ? visibleSum(s) : -i;
            widths[i] = LEGEND_SWATCH + LEGEND_GAP + font.width(s.label());
            byImportance[i] = i;
        }
        Arrays.sort(byImportance, (a, b) -> Double.compare(importance[b], importance[a]));

        int shown = entriesThatFit(widths, byImportance, legendWidth);
        if (shown < count) {
            float markerWidth = LEGEND_ENTRY_GAP + font.width("+" + count);
            shown = Math.max(1, entriesThatFit(widths, byImportance, legendWidth - markerWidth));
        }

        boolean[] visible = new boolean[count];
        for (int k = 0; k < shown; k++) {
            visible[byImportance[k]] = true;
        }

        float lineHeight = font.lineHeight;
        float x = x0;
        for (int i = 0; i < count; i++) {
            if (!visible[i]) {
                continue;
            }
            ChartSeries s = series.get(i);
            boolean hidden = hiddenLabels.contains(s.label());
            String label = fitLabel(font, s.label(), legendWidth - LEGEND_SWATCH - LEGEND_GAP);
            float textWidth = font.width(label);
            float entryWidth = LEGEND_SWATCH + LEGEND_GAP + textWidth;

            LegendHit hit = new LegendHit(x - LEGEND_HIT_PAD, y - LEGEND_HIT_PAD, entryWidth + 2f * LEGEND_HIT_PAD, lineHeight + 2f * LEGEND_HIT_PAD, s.label());
            legendHits.add(hit);
            boolean hovered = hit.contains(mx, my);

            int swatchColor = hidden ? faded(s.color()) : s.color();
            DrawerHelper.drawSolidRect(g, Math.round(x), Math.round(y + lineHeight / 2f - 1f), LEGEND_SWATCH, 2f, swatchColor);
            float textX = x + LEGEND_SWATCH + LEGEND_GAP;
            drawText(g, font, label, textX, y, hidden ? TEXT_DIM : TEXT);
            if (hidden) {
                DrawerHelper.drawSolidRect(g, Math.round(textX), Math.round(y + lineHeight / 2f), textWidth, 1f, TEXT_DIM);
            }
            if (hovered) {
                DrawerHelper.drawSolidRect(g, Math.round(textX), Math.round(y + lineHeight), textWidth, 1f, TEXT);
            }
            x += entryWidth + LEGEND_ENTRY_GAP;
        }
        if (shown < count) {
            drawText(g, font, "+" + (count - shown), x, y, TEXT_DIM);
        }
    }

    private static int entriesThatFit(float[] widths, Integer[] order, float budget) {
        float used = 0f;
        int n = 0;
        for (int index : order) {
            float next = used + (n == 0 ? 0f : LEGEND_ENTRY_GAP) + widths[index];
            if (next > budget) {
                break;
            }
            used = next;
            n++;
        }
        return n;
    }

    private static String fitLabel(Font font, String label, float maxWidth) {
        if (font.width(label) <= maxWidth) {
            return label;
        }
        return font.plainSubstrByWidth(label, (int) (maxWidth - font.width("…"))) + "…";
    }

    private static double visibleSum(ChartSeries s) {
        double sum = 0.0;
        for (int i = 0; i < s.size(); i++) {
            double v = s.y(i);
            if (!Double.isNaN(v)) {
                sum += v;
            }
        }
        return sum;
    }

    private static int nearestIndex(ChartSeries s, double x) {
        int lo = 0;
        int hi = s.size() - 1;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (s.x(mid) < x) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        if (lo > 0 && Math.abs(s.x(lo - 1) - x) <= Math.abs(s.x(lo) - x)) {
            return lo - 1;
        }
        return lo;
    }
}
