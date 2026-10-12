package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Toggle;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.network.chat.Component;

public final class UiToggles {

    public static Toggle wrappingToggle(Component text) {
        Toggle toggle = new Toggle();
        toggle.toggleLabel.setText(text);
        toggle.layout(layout -> layout
                .widthPercent(100)
                .heightAuto()
                .minHeight(14)
                .alignItems(AlignItems.FLEX_START));
        toggle.toggleButton.layout(layout -> layout
                .width(12)
                .height(12)
                .flexShrink(0));
        toggle.toggleLabel
                .textStyle(style -> style
                        .textWrap(TextWrap.WRAP)
                        .adaptiveHeight(true)
                        .adaptiveWidth(false))
                .layout(layout -> layout
                        .heightAuto()
                        .minHeight(12)
                        .minWidth(0)
                        .flexShrink(1));
        return toggle;
    }

}
