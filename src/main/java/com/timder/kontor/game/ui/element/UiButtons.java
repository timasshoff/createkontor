package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import net.minecraft.network.chat.Component;

public final class UiButtons {
    public static Button primary(Component text) {
        return (Button) new Button()
                .setText(text)
                .layout(layout -> layout.height(20));
    }

    public static Button critical(Component text) {
        return (Button) new Button()
                .setText(text)
                .textStyle(style -> style.textColor(0xFF5555))
                .layout(layout -> layout.height(20));
    }
}
