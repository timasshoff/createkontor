package com.timder.kontor.game.wiki.page;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.game.ui.element.WikiUtils;
import net.minecraft.network.chat.Component;

public class LicensesWikiPage {
    public static void build(UIElement page) {
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.licenses.p1")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.licenses.b1")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.licenses.p2")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.licenses.b2")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.licenses.p3")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.licenses.b3")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.licenses.p4")));
    }
}
