package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class UiLabels {

    private static final float MIN_LABEL_WIDTH = 80;

    public static Label h1(Component text, Horizontal horizontalAlignment) {
        return (Label) new Label()
                .setText(text)
                .textStyle(style -> style
                        .fontSize(12)
                        .textAlignHorizontal(horizontalAlignment)
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true)
                        .adaptiveWidth(true))
                .layout(layout -> layout
                        .marginAll(2));
    }

    public static Label h2(Component text, Horizontal horizontalAlignment) {
        return (Label) new Label()
                .setText(text)
                .textStyle(style -> style
                        .fontSize(10)
                        .textAlignHorizontal(horizontalAlignment)
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true)
                        .adaptiveWidth(true))
                .layout(layout -> layout
                        .marginAll(2));
    }

    public static Label primary(Component text, Horizontal horizontalAlignment) {
        return (Label) new Label()
                .setText(text)
                .textStyle(style -> style
                        .textAlignHorizontal(horizontalAlignment)
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true)
                        .adaptiveWidth(true))
                .layout(layout -> layout
                        .marginAll(2));
    }

    public static Label secondary(Component text, Horizontal horizontalAlignment) {
        return (Label) new Label()
                .setText(text)
                .textStyle(style -> style
                        .textColor(0xAAAAAA)
                        .textAlignHorizontal(horizontalAlignment)
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true)
                        .adaptiveWidth(true))
                .layout(layout -> layout
                        .marginAll(2));
    }

    public static Label tertiary(Component text, Horizontal horizontalAlignment) {
        return (Label) new Label()
                .setText(text)
                .textStyle(style -> style
                        .textColor(0x555555)
                        .textAlignHorizontal(horizontalAlignment)
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true)
                        .adaptiveWidth(true))
                .layout(layout -> layout
                        .marginAll(2));
    }

    public static Label error(Component text, Horizontal horizontalAlignment) {
        return (Label) new Label()
                .setText(text)
                .textStyle(style -> style
                        .textColor(0xFF5555)
                        .textAlignHorizontal(horizontalAlignment)
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true)
                        .adaptiveWidth(true))
                .layout(layout -> layout
                        .marginAll(2));
    }

    public static Label paragraphPrimary(Component text, Horizontal horizontalAlignment) {
        return (Label) new Label()
                .setText(text)
                .textStyle(style -> style
                        .textAlignHorizontal(horizontalAlignment)
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true)
                        .adaptiveWidth(false))
                .layout(layout -> layout
                        .marginAll(2)
                        .widthPercent(100));
    }

    public static Label paragraphSecondary(Component text, Horizontal horizontalAlignment) {
        return (Label) new Label()
                .setText(text)
                .textStyle(style -> style
                        .textColor(0xAAAAAA)
                        .textAlignHorizontal(horizontalAlignment)
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true)
                        .adaptiveWidth(false))
                .layout(layout -> layout
                        .marginAll(2)
                        .widthPercent(100));
    }

    public static Label paragraphTertiary(Component text, Horizontal horizontalAlignment) {
        return (Label) new Label()
                .setText(text)
                .textStyle(style -> style
                        .textColor(0x555555)
                        .textAlignHorizontal(horizontalAlignment)
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true)
                        .adaptiveWidth(false))
                .layout(layout -> layout
                        .marginAll(2)
                        .widthPercent(100));
    }

    public static Label paragraphError(Component text, Horizontal horizontalAlignment) {
        return (Label) new Label()
                .setText(text)
                .textStyle(style -> style
                        .textColor(0xFF5555)
                        .textAlignHorizontal(horizontalAlignment)
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true)
                        .adaptiveWidth(false))
                .layout(layout -> layout
                        .marginAll(2)
                        .widthPercent(100));
    }

    public static UIElement seperatedLabelRow(List<Label> labels) {
        UIElement row = new UIElement()
                .layout(layout -> layout
                        .flexDirection(FlexDirection.ROW)
                        .flexWrap(FlexWrap.WRAP)
                        .justifyContent(AlignContent.CENTER)
                        .alignItems(AlignItems.CENTER)
                        .gapAll(4)
                        .widthPercent(100));
        for (int i = 0; i < labels.size(); i++) {
            Label label = labels.get(i);
            boolean last = i == labels.size() - 1;

            row.addChild(label);
            if (!last) {
                row.addChild(UiLabels.secondary(Component.literal("•"), Horizontal.CENTER));
            }
        }
        return row;
    }

    public static UIElement labelValue(Component label, Component value) {
        return labelValue(label, primary(value, Horizontal.RIGHT));
    }

    public static UIElement labelValue(Component label, Label valueLabel) {
        UIElement row = new UIElement().layout(layout -> layout
                .flexDirection(FlexDirection.ROW)
                .flexWrap(FlexWrap.WRAP)
                .alignItems(AlignItems.FLEX_START)
                .gapAll(4)
                .widthPercent(100));
        Label labelElement = (Label) secondary(label, Horizontal.LEFT)
                .textStyle(style -> style.adaptiveWidth(false))
                .layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1)
                        .minWidth(MIN_LABEL_WIDTH));
        row.addChild(labelElement);
        valueLabel.layout(layout -> layout.alignSelf(AlignItems.FLEX_END));
        row.addChild(valueLabel);
        return row;
    }

    public static UIElement labelValueNoWrap(Component label, Component value) {
        UIElement row = new UIElement().layout(layout -> layout
                .flexDirection(FlexDirection.ROW)
                .alignItems(AlignItems.FLEX_START)
                .gapAll(4)
                .widthPercent(100));
        Label labelElement = (Label) secondary(label, Horizontal.LEFT)
                .textStyle(style -> style.adaptiveWidth(false))
                .layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1));
        Label valueElement = primary(value, Horizontal.RIGHT);
        valueElement.layout(layout -> layout.alignSelf(AlignItems.FLEX_END));
        row.addChild(labelElement);
        row.addChild(valueElement);
        return row;
    }
}
