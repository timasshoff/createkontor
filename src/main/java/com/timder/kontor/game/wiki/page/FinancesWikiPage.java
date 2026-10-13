package com.timder.kontor.game.wiki.page;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.game.ui.element.WikiUtils;
import net.minecraft.network.chat.Component;

public class FinancesWikiPage {
    public static void build(UIElement page) {
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.finances.p1", Money.fromDollar(CompanyConfig.START_DEPOSIT_IN_DOLLARS.get()).toString())));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.finances.b1")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.finances.p2")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.finances.b2")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.finances.p3")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.finances.b3")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.finances.p4")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.finances.b4")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.finances.p5", CompanyConfig.INSOLVENCY_DAYS.get())));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.finances.b5")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.finances.p6")));
    }
}
