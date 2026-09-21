package com.timder.kontor.client.ui.element;

import com.lowdragmc.lowdraglib2.gui.LDLibFonts;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import com.timder.kontor.chart.ChartReferenceLine;
import com.timder.kontor.chart.ChartSeries;
import com.timder.kontor.chart.ChartSpec;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.joml.Vector2f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    public ChartElement() {
        style(s -> s.overflowVisible(false)); // Prevent overflow
    }

    /**
     * Builds a chart from a description.
     * @param spec The chart description
     * @return The configured chart. Size is up to the caller.
     */
    public static ChartElement from(ChartSpec spec) {
        ChartElement chart = new ChartElement();
        if (!spec.title().isEmpty()) {
            chart.setTitle(Component.literal(spec.title()));
        }
        chart.setSeries(spec.series());
        for (ChartReferenceLine line : spec.referenceLines()) {
            chart.addReferenceLine(line.label(), line.value(), line.color());
        }
        chart.setIncludeZero(spec.includeZero());

        String xUnit = spec.xUnit();
        String xZeroLabel = spec.xZeroLabel();
        String yUnit = spec.yUnit();
        String tooltipFormat = "%." + spec.tooltipDecimals() + "f";
        chart.setXFormatter((value, decimals) ->
                !xZeroLabel.isEmpty() && Math.abs(value) < 1e-9 ? xZeroLabel : NiceScale.format(value, decimals) + xUnit);
        chart.setYFormatter((value, decimals) -> NiceScale.format(value, decimals) + yUnit);
        chart.setTooltipValueFormatter(value -> String.format(Locale.ROOT, tooltipFormat, value) + yUnit);

        List<String> pointLabels = spec.pointLabels();
        if (!pointLabels.isEmpty()) {
            chart.setTooltipHeader(index -> index < pointLabels.size() ? pointLabels.get(index) : "");
        }
        return chart;
    }

    public ChartElement setTitle(@Nullable Component title) {
        this.title = title;
        return this;
    }

    /**
     * Replaces all lines
     * @param series The new chart series
     * @return This chart element
     */
    public ChartElement setSeries(List<ChartSeries> series) {
        this.series = List.copyOf(series);
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

        double[] range = dataRange();
        if (range == null) {
            String text = "No data";
            drawText(g, font, text, left + (width - font.width(text)) / 2f, top + (height - lineHeight) / 2f, TEXT_DIM);
            return;
        }

        NiceScale yScale = NiceScale.of(range[2], range[3], Y_TICK_TARGET);
        NiceScale xScale = NiceScale.of(range[0], range[1], X_TICK_TARGET);
        double[] yTicks = yScale.ticks();
        double[] xTicks = xScale.ticksWithin(range[0], range[1]);

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
            drawLegend(g, font, left + PAD, y);
            y += lineHeight + 4f;
        }

        Plot plot = new Plot(
                left + PAD + labelWidth + 4f,
                y,
                left + width - PAD - 2f,
                top + height - PAD - lineHeight - 4f,
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

        for (ChartSeries s : series) {
            drawSeries(g, plot, s);
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

        for (ChartSeries s : series) {
            for (int i = 0; i < s.size(); i++) {
                double v = s.y(i);
                if (Double.isNaN(v)) {
                    continue;
                }
                xMin = Math.min(xMin, s.x(i));
                xMax = Math.max(xMax, s.x(i));
                yMin = Math.min(yMin, v);
                yMax = Math.max(yMax, v);
            }
        }
        if (xMin > xMax) {
            return null; // not a single usable point
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

    private static void flushRun(GuiGraphics g, List<Vector2f> run, int color) {
        if (run.size() >= 2) {
            DrawerHelper.drawLines(g, run, color, color, LINE_HALF_WIDTH);
        } else if (run.size() == 1) {
            Vector2f p = run.get(0);
            DrawerHelper.drawSolidRect(g, Math.round(p.x) - 1f, Math.round(p.y) - 1f, 3f, 3f, color);
        }
    }

    private void drawHover(GUIContext ctx, GuiGraphics g, Font font, Plot plot, float mx, float my, float left, float top, float width, float height) {
        double hoverX = plot.xMin() + (mx - plot.left()) / plot.width() * (plot.xMax() - plot.xMin());

        List<TipLine> lines = new ArrayList<>();
        List<Dot> dots = new ArrayList<>();
        float cursorX = Float.NaN;
        int headerIndex = -1;
        double headerX = 0.0;

        for (ChartSeries s : series) {
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
            dots.add(new Dot(plot.x(s.x(index)), plot.y(value), s.color()));
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

    private void drawLegend(GuiGraphics g, Font font, float x, float y) {
        float lineHeight = font.lineHeight;
        for (ChartSeries s : series) {
            DrawerHelper.drawSolidRect(g, Math.round(x), Math.round(y + lineHeight / 2f - 1f), 8f, 2f, s.color());
            x += 8f + 4f;
            drawText(g, font, s.label(), x, y, TEXT);
            x += font.width(s.label()) + 12f;
        }
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
