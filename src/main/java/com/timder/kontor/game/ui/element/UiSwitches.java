package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.network.chat.Component;

public final class UiSwitches {
    public static UIElement labeled(Component text, boolean on, BooleanConsumer onChange) {
        Switch switchElement = new Switch();
        switchElement.setOn(on, false);
        switchElement.setOnSwitchChanged(onChange);

        Label label = UiLabels.paragraphPrimary(text, Horizontal.LEFT);
        label.textStyle(style -> style.textAlignVertical(Vertical.CENTER));
        label.layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1));

        return new UIElement()
                .layout(layout -> layout
                        .flexDirection(FlexDirection.ROW)
                        .justifyContent(AlignContent.FLEX_START)
                        .alignItems(AlignItems.CENTER)
                        .gapAll(4)
                        .widthPercent(100))
                .addChildren(switchElement, label);
    }
}
