package com.timder.kontor.game.ui.elements;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
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
                        .textAlignHorizontal(horizontalAlignment))
                .layout(layout -> layout
                        .marginAll(2));
    }

    public static Label h2(Component text, Horizontal horizontalAlignment) {
        return (Label) new Label()
                .setText(text)
                .textStyle(style -> style
                        .fontSize(10)
                        .textAlignHorizontal(horizontalAlignment))
                .layout(layout -> layout
                        .marginAll(2));
    }

    public static Label primary(Component text, Horizontal horizontalAlignment) {
        return (Label) new Label()
                .setText(text)
                .textStyle(style -> style
                        .textAlignHorizontal(horizontalAlignment)
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true))
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
                        .adaptiveHeight(true))
                .layout(layout -> layout
                        .marginAll(2));
    }
}
