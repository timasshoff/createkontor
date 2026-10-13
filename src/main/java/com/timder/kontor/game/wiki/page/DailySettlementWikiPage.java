package com.timder.kontor.game.wiki.page;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.game.ui.element.WikiUtils;
import net.minecraft.network.chat.Component;

public class DailySettlementWikiPage {
    public static void build(UIElement page) {
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.daily_settlement.p1")));
        page.addChild(WikiUtils.bulletList(Component.translatable("wiki.createkontor.page.daily_settlement.effects")));
    }
}
