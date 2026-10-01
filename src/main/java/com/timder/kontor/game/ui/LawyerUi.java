package com.timder.kontor.game.ui;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.timder.kontor.game.block.LawyerActions;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.element.UiContainer;
import net.minecraft.network.chat.Component;

public final class LawyerUi {

    public static ModularUI create(EmployeeDeskContext context, LawyerActions actions) {
        var tabView = UiContainer.largeTabView();
        tabView.addTab(UiContainer.tab(Component.literal("Legal Form")), legalFormTab(context));
        tabView.addTab(UiContainer.tab(Component.literal("Licenses")), legalFormTab(context));
        tabView.addTab(UiContainer.tab(Component.literal("License Catalog")), legalFormTab(context));

        var root = UiContainer.withHud(tabView, UiContainer.hudBox(context.company(), context.economy()));
        return new ModularUI(UI.of(root, StylesheetManager.GDP), context.player());
    }

    public static UIElement legalFormTab(EmployeeDeskContext context) {
        return new UIElement();
    }
}
