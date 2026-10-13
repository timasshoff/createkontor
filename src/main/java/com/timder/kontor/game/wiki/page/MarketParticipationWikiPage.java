package com.timder.kontor.game.wiki.page;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.game.ui.element.WikiUtils;
import net.minecraft.network.chat.Component;

public class MarketParticipationWikiPage {
    public static void build(UIElement page) {
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.market_participation.p1")));
    }
}
