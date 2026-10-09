package com.timder.kontor.game.ui.chart.segment;

import com.lowdragmc.lowdraglib2.gui.LDLibFonts;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import com.timder.kontor.game.ui.chart.ChartColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SegmentBarElement extends UIElement {
    private record LegendEntry(int index, String text, float x, float y, float width, float height) {
        boolean contains(float px, float py) {
            return px >= x && px < x + width && py >= y && py < y + height;
        }
    }

    private static final float PAD = 6f;
    private static final float ROW_HEIGHT = 10f;
    private static final float BAR_HEIGHT = 16f;
    private static final float GAP = 4f;
    private static final float MIN_BAR_WIDTH = 20f;
    private static final float INNER_PAD = 3f;

    private static final float LEGEND_SWATCH = 8f;
    private static final float LEGEND_GAP = 4f;
    private static final float LEGEND_ENTRY_GAP = 10f;
    private static final float LEGEND_HIT_PAD = 2f;

    private static final float SWATCH = 5f;
    private static final float TOOLTIP_PAD = 4f;
    private static final int TOOLTIP_WRAP_WIDTH = 160;

    @Nullable
    private SegmentBarSpec spec;
    private float appliedHeight = -1f;

    public SegmentBarElement() {
        style(s -> s.overflowVisible(false));
    }

    public static SegmentBarElement from(SegmentBarSpec spec) {
        SegmentBarElement bar = new SegmentBarElement();
        bar.update(spec);
        return bar;
    }

    public SegmentBarElement update(SegmentBarSpec spec) {
        this.spec = Objects.requireNonNull(spec, "spec must not be null.");
        float height = heightFor(!spec.title().isEmpty());
        if (height != appliedHeight) {
            appliedHeight = height;
            layout(layout -> layout.height(height));
        }
        return this;
    }

    public static float heightFor(boolean hasTitle) {
        return PAD + (hasTitle ? ROW_HEIGHT + GAP : 0f) + BAR_HEIGHT + GAP + ROW_HEIGHT + PAD;
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

        DrawerHelper.drawSolidRect(g, left, top, width, height, ChartColors.PANEL_BACKGROUND);

        if (spec == null || !spec.hasData()) {
            drawNoData(g, font, left, top, width, height);
            return;
        }

        float barLeft = left + PAD;
        float barWidth = width - 2f * PAD;
        if (barWidth < MIN_BAR_WIDTH) {
            return;
        }

        float y = top + PAD;
        if (!spec.title().isEmpty()) {
            drawText(g, font, spec.title(), barLeft, y, ChartColors.TEXT);
            y += ROW_HEIGHT + GAP;
        }
        float barTop = y;
        float legendTop = barTop + BAR_HEIGHT + GAP;

        List<SegmentBarSegment> segments = spec.segments();
        double[] values = new double[segments.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = segments.get(i).value();
        }
        float[] edges = SegmentBarMath.edges(values, barLeft, barWidth);
        List<LegendEntry> legend = layoutLegend(font, barLeft, legendTop, barWidth);

        int hovered = -1;
        float mx = 0f;
        float my = 0f;
        if (isHover()) {
            mx = ctx.localMouseX;
            my = ctx.localMouseY;
            if (my >= barTop && my < barTop + BAR_HEIGHT) {
                hovered = SegmentBarMath.indexAt(edges, mx);
            }
            if (hovered < 0) {
                for (LegendEntry entry : legend) {
                    if (entry.contains(mx, my)) {
                        hovered = entry.index();
                        break;
                    }
                }
            }
        }

        drawBar(g, font, edges, barTop, hovered);
        drawLegend(g, font, legend, hovered);
        g.flush();

        if (hovered >= 0) {
            drawTooltip(ctx, font, hovered, mx, my, left, width);
        }
    }

    private void drawBar(GuiGraphics g, Font font, float[] edges, float barTop, int hovered) {
        List<SegmentBarSegment> segments = spec.segments();
        float textY = barTop + (BAR_HEIGHT - font.lineHeight) / 2f;

        for (int i = 0; i < segments.size(); i++) {
            float x1 = edges[i];
            float x2 = edges[i + 1];
            if (x2 <= x1) {
                continue;
            }
            SegmentBarSegment segment = segments.get(i);
            DrawerHelper.drawSolidRect(g, x1, barTop, x2 - x1, BAR_HEIGHT, segment.color());

            if (x1 > edges[0] && x2 - x1 > 1f) {
                DrawerHelper.drawSolidRect(g, x1, barTop, 1f, BAR_HEIGHT, ChartColors.PANEL_BACKGROUND);
            }

            String text = spec.valueText(i);
            float textWidth = font.width(text);
            if (x2 - x1 >= textWidth + 2f * INNER_PAD) {
                drawText(g, font, text, x1 + (x2 - x1 - textWidth) / 2f, textY, SegmentBarMath.readableTextColor(segment.color()));
            }
        }

        if (hovered >= 0 && edges[hovered + 1] > edges[hovered]) {
            outline(g, edges[hovered], barTop, edges[hovered + 1] - edges[hovered], BAR_HEIGHT, ChartColors.HIGHLIGHT);
        }
    }

    private List<LegendEntry> layoutLegend(Font font, float x0, float y, float maxWidth) {
        List<SegmentBarSegment> segments = spec.segments();
        int count = segments.size();
        String[] labels = new String[count];
        String[] values = new String[count];
        float total = (count - 1) * LEGEND_ENTRY_GAP;
        for (int i = 0; i < count; i++) {
            labels[i] = segments.get(i).label();
            values[i] = " " + spec.valueText(i);
            total += LEGEND_SWATCH + LEGEND_GAP + font.width(labels[i] + values[i]);
        }

        if (total > maxWidth) {
            float budget = (maxWidth - (count - 1) * LEGEND_ENTRY_GAP) / count - LEGEND_SWATCH - LEGEND_GAP;
            for (int i = 0; i < count; i++) {
                labels[i] = fitLabel(font, labels[i], budget - font.width(values[i]));
            }
        }

        List<LegendEntry> entries = new ArrayList<>(count);
        float x = x0;
        for (int i = 0; i < count; i++) {
            String text = labels[i] + values[i];
            float entryWidth = LEGEND_SWATCH + LEGEND_GAP + font.width(text);
            entries.add(new LegendEntry(i, text,
                    x - LEGEND_HIT_PAD, y - LEGEND_HIT_PAD,
                    entryWidth + 2f * LEGEND_HIT_PAD, font.lineHeight + 2f * LEGEND_HIT_PAD));
            x += entryWidth + LEGEND_ENTRY_GAP;
        }
        return entries;
    }

    private void drawLegend(GuiGraphics g, Font font, List<LegendEntry> entries, int hovered) {
        for (LegendEntry entry : entries) {
            SegmentBarSegment segment = spec.segments().get(entry.index());
            float x = entry.x() + LEGEND_HIT_PAD;
            float y = entry.y() + LEGEND_HIT_PAD;
            float swatchY = y + (font.lineHeight - LEGEND_SWATCH) / 2f;
            DrawerHelper.drawSolidRect(g, Math.round(x), Math.round(swatchY), LEGEND_SWATCH, LEGEND_SWATCH, segment.color());

            float textX = x + LEGEND_SWATCH + LEGEND_GAP;
            drawText(g, font, entry.text(), textX, y, ChartColors.TEXT);
            if (entry.index() == hovered) {
                DrawerHelper.drawSolidRect(g, Math.round(textX), Math.round(y + font.lineHeight), font.width(entry.text()), 1f, ChartColors.TEXT);
            }
        }
    }

    private void drawTooltip(GUIContext ctx, Font font, int index, float mx, float my, float left, float width) {
        SegmentBarSegment segment = spec.segments().get(index);
        String header = segment.label();
        String valueLine = spec.valueText(index);
        String shareLine = spec.display() == SegmentBarSpec.Display.VALUE
                ? SegmentBarMath.percent(segment.value(), spec.total(), spec.decimals())
                : null;
        List<FormattedCharSequence> description = segment.description().isEmpty()
                ? List.of()
                : font.split(Component.literal(segment.description()), TOOLTIP_WRAP_WIDTH);
        int color = segment.color();

        ctx.postRendering(c -> {
            GuiGraphics g = c.graphics;
            float lineHeight = font.lineHeight;

            float textWidth = font.width(header);
            textWidth = Math.max(textWidth, SWATCH + 4f + font.width(valueLine));
            if (shareLine != null) {
                textWidth = Math.max(textWidth, SWATCH + 4f + font.width(shareLine));
            }
            for (FormattedCharSequence line : description) {
                textWidth = Math.max(textWidth, font.width(line));
            }

            int rows = 2 + (shareLine != null ? 1 : 0) + description.size();
            float boxWidth = textWidth + 2f * TOOLTIP_PAD;
            float boxHeight = rows * (lineHeight + 1f) - 1f + 2f * TOOLTIP_PAD;

            float bx = mx + 12f;
            if (bx + boxWidth > left + width - 2f) {
                bx = mx - 12f - boxWidth;
            }
            bx = Math.round(Math.max(left + 2f, bx));
            float by = Math.round(my + 14f);

            DrawerHelper.drawSolidRect(g, bx - 1f, by - 1f, boxWidth + 2f, boxHeight + 2f, ChartColors.TOOLTIP_BORDER);
            DrawerHelper.drawSolidRect(g, bx, by, boxWidth, boxHeight, ChartColors.TOOLTIP_BACKGROUND);

            float ty = by + TOOLTIP_PAD;
            drawText(g, font, header, bx + TOOLTIP_PAD, ty, ChartColors.TEXT);

            ty += lineHeight + 1f;
            DrawerHelper.drawSolidRect(g, Math.round(bx + TOOLTIP_PAD), Math.round(ty + (lineHeight - SWATCH) / 2f), SWATCH, SWATCH, color);
            drawText(g, font, valueLine, bx + TOOLTIP_PAD + SWATCH + 4f, ty, ChartColors.TEXT);

            if (shareLine != null) {
                ty += lineHeight + 1f;
                drawText(g, font, shareLine, bx + TOOLTIP_PAD + SWATCH + 4f, ty, ChartColors.TEXT_DIM);
            }
            for (FormattedCharSequence line : description) {
                ty += lineHeight + 1f;
                LDLibFonts.drawText(g, font, line, Math.round(bx + TOOLTIP_PAD), Math.round(ty), ChartColors.TEXT_DIM, false);
            }
        });
    }

    private void drawNoData(GuiGraphics g, Font font, float left, float top, float width, float height) {
        Component message = spec != null && !spec.title().isEmpty()
                ? Component.translatable("ui.createkontor.chart.no_data.detailed", spec.title())
                : Component.translatable("ui.createkontor.chart.no_data.short");
        List<FormattedCharSequence> lines = font.split(message, Math.round(width - 2f * PAD));
        float textY = top + (height - lines.size() * font.lineHeight) / 2f;
        for (FormattedCharSequence line : lines) {
            float textX = left + (width - font.width(line)) / 2f;
            LDLibFonts.drawText(g, font, line, textX, textY, ChartColors.TEXT_DIM, false);
            textY += font.lineHeight;
        }
    }

    private static void outline(GuiGraphics g, float x, float y, float width, float height, int color) {
        DrawerHelper.drawSolidRect(g, x, y, width, 1f, color);
        DrawerHelper.drawSolidRect(g, x, y + height - 1f, width, 1f, color);
        DrawerHelper.drawSolidRect(g, x, y, 1f, height, color);
        DrawerHelper.drawSolidRect(g, x + width - 1f, y, 1f, height, color);
    }

    private static void drawText(GuiGraphics g, Font font, String text, float x, float y, int color) {
        LDLibFonts.drawText(g, font, Component.literal(text), Math.round(x), Math.round(y), color, false);
    }

    private static String fitLabel(Font font, String label, float maxWidth) {
        if (font.width(label) <= maxWidth) {
            return label;
        }
        return font.plainSubstrByWidth(label, Math.max(0, (int) (maxWidth - font.width("…")))) + "…";
    }
}
