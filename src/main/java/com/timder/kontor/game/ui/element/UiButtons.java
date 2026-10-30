package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.network.chat.Component;

public final class UiButtons {

    public static Button primary(Component text) {
        return (Button) new Button().setText(text).addClass("kontor-primary").layout(layout -> layout.height(20));
    }

    public static Button secondary(Component text) {
        return (Button) new Button().setText(text).layout(layout -> layout.height(20));
    }

    public static Button critical(Component text) {
        return (Button) new Button().setText(text).addClass("kontor-danger").layout(layout -> layout.height(20));
    }

    public static Button wrapping(Button button) {
        button.layout(layout ->
                layout.heightAuto().minHeight(20).alignItems(AlignItems.CENTER).paddingVertical(4));
        button.textStyle(style -> style.adaptiveWidth(false)
                .textWrap(TextWrap.WRAP)
                .adaptiveHeight(true)
                .textAlignHorizontal(Horizontal.CENTER));
        button.text.layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1));
        return button;
    }

    public static Button primaryWrapping(Component text) {
        return wrapping(primary(text));
    }

    public static Button secondaryWrapping(Component text) {
        return wrapping(secondary(text));
    }

    public static Button criticalWrapping(Component text) {
        return wrapping(critical(text));
    }
}
