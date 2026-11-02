package com.timder.kontor.client.ui;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.timder.kontor.game.ui.element.UiPaper;
import com.timder.kontor.game.ui.element.UiStyles;
import net.minecraft.network.chat.Component;

public final class NotebookUi {
    public static ModularUI create() {
        UIElement root = new UIElement()
                .layout(layout -> layout.width(200)
                        .height(237)
                        .paddingLeft(28)
                        .paddingTop(8)
                        .paddingRight(13)
                        .paddingBottom(13)
                        .gapAll(4))
                .style(UiStyles.NOTEBOOK);
        root.addChild(UiPaper.heading(Component.literal("Notebook"), Horizontal.CENTER));
        return ModularUI.of(UiStyles.ui(root));
    }
}
