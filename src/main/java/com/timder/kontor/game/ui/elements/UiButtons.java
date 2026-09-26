package com.timder.kontor.game.ui.elements;

import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import net.minecraft.network.chat.Component;

public final class UiButtons {
    public static Button primary(Component text) {
        return (Button) new Button()
                .setText(text)
                .layout(layout -> layout.height(20));
    }
}
