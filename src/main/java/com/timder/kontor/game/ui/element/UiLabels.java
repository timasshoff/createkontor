package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class UiLabels {

    private static final float MIN_LABEL_WIDTH = 80;

    private static final int SECONDARY = 0xAAAAAA;
    private static final int TERTIARY = 0x555555;
    private static final int ERROR = 0xFF5555;

    private static final int PAPER_PRIMARY = 0x2A2418;
    private static final int PAPER_SECONDARY = 0x5E5338;
    private static final int PAPER_ERROR = 0xA3342C;

    private static final Map<Integer, Integer> COMPONENT_PAPER_COLORS = Map.of(
            ChatFormatting.GREEN.getColor(), 0x1A6029,
            ChatFormatting.GOLD.getColor(), 0x7A4F00,
            ChatFormatting.RED.getColor(), 0xA02020,
            ChatFormatting.GRAY.getColor(), PAPER_SECONDARY);

    // Necessary to make every heading bold because the LDLib2 text style does not have a setter for bold formatting
    private static final class HeadingLabel extends Label {
        @Override
        public TextElement setText(Component text) {
            return super.setText(text.copy().withStyle(ChatFormatting.BOLD));
        }
    }

    // HEADINGS

    public static Label h1(Component text, Horizontal horizontalAlignment) {
        return heading(text, horizontalAlignment, 12);
    }

    public static Label h2(Component text, Horizontal horizontalAlignment) {
        return heading(text, horizontalAlignment, 10);
    }

    // NORMAL

    public static Label primary(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, null, false);
    }

    public static Label secondary(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, SECONDARY, false);
    }

    public static Label tertiary(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, TERTIARY, false);
    }

    public static Label error(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, ERROR, false);
    }

    public static Label paragraphPrimary(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, null, true);
    }

    public static Label paragraphSecondary(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, SECONDARY, true);
    }

    public static Label paragraphTertiary(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, TERTIARY, true);
    }

    public static Label paragraphError(Component text, Horizontal horizontalAlignment) {
        return label(text, horizontalAlignment, ERROR, true);
    }

    // PAPER

    public static Label paperPrimary(Component text, Horizontal horizontalAlignment) {
        return paper(text, horizontalAlignment, PAPER_PRIMARY, false);
    }

    public static Label paperSecondary(Component text, Horizontal horizontalAlignment) {
        return paper(text, horizontalAlignment, PAPER_SECONDARY, false);
    }

    public static Label paperError(Component text, Horizontal horizontalAlignment) {
        return paper(text, horizontalAlignment, PAPER_ERROR, false);
    }

    public static Label paperParagraphPrimary(Component text, Horizontal horizontalAlignment) {
        return paper(text, horizontalAlignment, PAPER_PRIMARY, true);
    }

    public static Label paperParagraphSecondary(Component text, Horizontal horizontalAlignment) {
        return paper(text, horizontalAlignment, PAPER_SECONDARY, true);
    }

    public static Label paperParagraphError(Component text, Horizontal horizontalAlignment) {
        return paper(text, horizontalAlignment, PAPER_ERROR, true);
    }

    public static UIElement seperatedLabelRow(List<Label> labels) {
        UIElement row = new UIElement()
                .layout(layout -> layout.flexDirection(FlexDirection.ROW)
                        .flexWrap(FlexWrap.WRAP)
                        .justifyContent(AlignContent.CENTER)
                        .alignItems(AlignItems.CENTER)
                        .gapAll(4)
                        .widthPercent(100));
        for (int i = 0; i < labels.size(); i++) {
            row.addChild(labels.get(i));
            if (i < labels.size() - 1) {
                row.addChild(secondary(Component.literal("•"), Horizontal.CENTER));
            }
        }
        return row;
    }

    private static Label paper(Component text, Horizontal horizontalAlignment, int color, boolean paragraph) {
        return (Label) label(adapt(text), horizontalAlignment, color, paragraph)
                .textStyle(style -> style.textShadow(false));
    }

    public static Component adapt(Component text) {
        MutableComponent result = Component.empty();
        text.visit(
                (style, content) -> {
                    TextColor color = style.getColor();
                    Integer ink = color == null ? null : COMPONENT_PAPER_COLORS.get(color.getValue());
                    Style adapted = ink == null ? style : style.withColor(ink);
                    result.append(Component.literal(content).withStyle(adapted));
                    return Optional.empty();
                },
                Style.EMPTY);
        return result;
    }

    public static UIElement labelValue(Component label, Component value) {
        return labelValue(label, primary(value, Horizontal.RIGHT));
    }

    public static UIElement labelValue(Component label, Label valueLabel) {
        return labelValueRow(label, valueLabel, true);
    }

    public static UIElement labelValueNoWrap(Component label, Component value) {
        return labelValueRow(label, primary(value, Horizontal.RIGHT), false);
    }

    private static Label label(
            Component text, Horizontal horizontalAlignment, @Nullable Integer color, boolean paragraph) {
        return (Label) new Label()
                .setText(text)
                .textStyle(style -> {
                    if (color != null) {
                        style.textColor(color);
                    }
                    style.textAlignHorizontal(horizontalAlignment)
                            .textWrap(TextWrap.WRAP)
                            .adaptiveHeight(true)
                            .adaptiveWidth(!paragraph);
                })
                .layout(layout -> {
                    layout.marginAll(2);
                    if (paragraph) {
                        layout.widthPercent(100);
                    }
                });
    }

    private static Label heading(Component text, Horizontal horizontalAlignment, float fontSize) {
        return (Label) new HeadingLabel()
                .setText(text)
                .textStyle(style -> style.fontSize(fontSize)
                        .textAlignHorizontal(horizontalAlignment)
                        .textWrap(TextWrap.WRAP)
                        .textShadow(true)
                        .adaptiveHeight(true)
                        .adaptiveWidth(true))
                .layout(layout -> layout.marginAll(2));
    }

    private static UIElement labelValueRow(Component label, Label valueLabel, boolean wrap) {
        UIElement row = new UIElement().layout(layout -> {
            layout.flexDirection(FlexDirection.ROW).alignItems(AlignItems.FLEX_START).widthPercent(100);
            if (wrap) {
                layout.flexWrap(FlexWrap.WRAP).justifyContent(AlignContent.FLEX_END).gapColumn(4);
            } else {
                layout.gapAll(4);
            }
        });
        Label labelElement = (Label) secondary(label, Horizontal.LEFT)
                .textStyle(style -> style.adaptiveWidth(false))
                .layout(layout -> {
                    layout.widthAuto().flexBasis(0).flexGrow(1);
                    if (wrap) {
                        layout.minWidth(MIN_LABEL_WIDTH);
                    }
                });
        valueLabel.layout(layout -> layout.alignSelf(AlignItems.FLEX_END));
        row.addChild(labelElement);
        row.addChild(valueLabel);
        return row;
    }
}