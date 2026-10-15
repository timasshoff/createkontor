package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.network.chat.Component;

public final class UiSwitches {
    public static final class LabeledSwitch extends UIElement {
        private final Switch switchElement;

        private LabeledSwitch(Switch switchElement) {
            this.switchElement = switchElement;
        }

        public Switch getSwitch() {
            return switchElement;
        }
    }

    public static LabeledSwitch labeled(Component text, boolean on, BooleanConsumer onChange) {
        Switch switchElement = new Switch();
        switchElement.setOn(on, false);
        switchElement.setOnSwitchChanged(onChange);

        Label label = UiLabels.primary(text, Horizontal.LEFT);

        LabeledSwitch row = new LabeledSwitch(switchElement);
        row.layout(layout -> layout
                .flexDirection(FlexDirection.ROW)
                .alignItems(AlignItems.CENTER)
                .gapAll(4));
        row.addChildren(switchElement, label);
        return row;
    }

    public static LabeledSwitch labeledLeft(Component text, boolean on, BooleanConsumer onChange) {
        Switch switchElement = new Switch();
        switchElement.setOn(on, false);
        switchElement.setOnSwitchChanged(onChange);

        Label label = UiLabels.primary(text, Horizontal.RIGHT);

        LabeledSwitch row = new LabeledSwitch(switchElement);
        row.layout(layout -> layout
                .flexDirection(FlexDirection.ROW)
                .alignItems(AlignItems.CENTER)
                .gapAll(4));
        row.addChildren(label, switchElement);
        return row;
    }
}
