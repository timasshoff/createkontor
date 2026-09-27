package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import net.minecraft.network.chat.Component;

public final class UiLabels {
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
}
