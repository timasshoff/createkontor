package com.timder.kontor.game.ui;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.timder.kontor.game.block.KontorDeskBlockEntity;

public class KontorDeskBoundUi {
    public static ModularUI create(BlockUIMenuType.BlockUIHolder holder, KontorDeskBlockEntity be) {
        var root = new UIElement().layout(layout -> layout.paddingAll(4).gapAll(2));
        root.addChild(new Label().setText("This Kontor is bound to a company."));
        return new ModularUI(UI.of(root), holder.player);
    }
}
