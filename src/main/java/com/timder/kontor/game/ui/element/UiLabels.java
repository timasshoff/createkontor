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
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class UiLabels {

    private static final float MIN_LABEL_WIDTH = 80;

    private static final int SECONDARY = 0xAAAAAA;
    private static final int TERTIARY = 0x555555;
    private static final int ERROR = 0xFF5555;

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

    public static UIElement labelValue(Component label, Component value) {
        return labelValue(label, primary(value, Horizontal.RIGHT));
    }

    public static UIElement labelValue(Component label, Label valueLabel) {
        return labelValueRow(secondary(label, Horizontal.LEFT), valueLabel, true);
    }

    public static UIElement labelValueNoWrap(Component label, Component value) {
        return labelValueRow(secondary(label, Horizontal.LEFT), primary(value, Horizontal.RIGHT), false);
    }

    public static Label label(
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

    static UIElement labelValueRow(Label labelElement, Label valueLabel, boolean wrap) {
        UIElement row = new UIElement().layout(layout -> {
            layout.flexDirection(FlexDirection.ROW)
                    .alignItems(AlignItems.FLEX_START)
                    .widthPercent(100);
            if (wrap) {
                layout.flexWrap(FlexWrap.WRAP)
                        .justifyContent(AlignContent.FLEX_END)
                        .gapColumn(4);
            } else {
                layout.gapAll(4);
            }
        });
        labelElement.textStyle(style -> style.adaptiveWidth(false));
        labelElement.layout(layout -> {
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
