package com.timder.kontor.game.wiki.page;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.game.ui.element.WikiUtils;
import net.minecraft.network.chat.Component;

public class MarketsWikiPage {
    public static void build(UIElement page) {
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.markets.p1")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.markets.b1")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.markets.p2")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.markets.b2")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.markets.p3")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.markets.b3")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.markets.p4")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.markets.b4")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.markets.p5")));
    }
}
