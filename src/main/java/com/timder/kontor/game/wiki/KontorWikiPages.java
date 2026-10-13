package com.timder.kontor.game.wiki;

import com.timder.kontor.game.block.consultant.ConsultantWiki;
import com.timder.kontor.game.wiki.page.LegalFormsWikiPage;
import net.minecraft.network.chat.Component;

public final class KontorWikiPages {
    public static void register() {
        ConsultantWiki.register("getting_started", WikiCategory.GUIDES, Component.translatable("wiki.createkontor.page.getting_started.title"), (e) -> {});

        ConsultantWiki.register("daily_settlement", WikiCategory.BASICS, Component.translatable("wiki.createkontor.page.daily_settlement.title"), (e) -> {});
        ConsultantWiki.register("company_activity", WikiCategory.BASICS, Component.translatable("wiki.createkontor.page.company_activity.title"), (e) -> {});
        ConsultantWiki.register("chunk_loading", WikiCategory.BASICS, Component.translatable("wiki.createkontor.page.chunk_loading.title"), (e) -> {});
        ConsultantWiki.register("finances", WikiCategory.BASICS, Component.translatable("wiki.createkontor.page.finances.title"), (e) -> {});
        ConsultantWiki.register("legal_forms", WikiCategory.BASICS, Component.translatable("wiki.createkontor.page.legal_forms.title"), LegalFormsWikiPage::build);
        ConsultantWiki.register("markets", WikiCategory.BASICS, Component.translatable("wiki.createkontor.page.markets.title"), (e) -> {});
        ConsultantWiki.register("licenses", WikiCategory.BASICS, Component.translatable("wiki.createkontor.page.licenses.title"), (e) -> {});
        ConsultantWiki.register("market_participation", WikiCategory.BASICS, Component.translatable("wiki.createkontor.page.market_participation.title"), (e) -> {});
        ConsultantWiki.register("requests_orders", WikiCategory.BASICS, Component.translatable("wiki.createkontor.page.requests_orders.title"), (e) -> {});
        ConsultantWiki.register("reputation", WikiCategory.BASICS, Component.translatable("wiki.createkontor.page.reputation.title"), (e) -> {});
        ConsultantWiki.register("market_share", WikiCategory.BASICS, Component.translatable("wiki.createkontor.page.market_share.title"), (e) -> {});
        ConsultantWiki.register("managers", WikiCategory.BASICS, Component.translatable("wiki.createkontor.page.managers.title"), (e) -> {});
        ConsultantWiki.register("economic_cycle", WikiCategory.BASICS, Component.translatable("wiki.createkontor.page.economic_cycle.title"), (e) -> {});
        ConsultantWiki.register("policy_rate", WikiCategory.BASICS, Component.translatable("wiki.createkontor.page.policy_rate.title"), (e) -> {});

        ConsultantWiki.register("employees", WikiCategory.EMPLOYEES, Component.translatable("wiki.createkontor.page.employees.title"), (e) -> {});
        ConsultantWiki.register("lawyer", WikiCategory.EMPLOYEES, Component.translatable("wiki.createkontor.page.lawyer.title"), (e) -> {});
        ConsultantWiki.register("market_analyst", WikiCategory.EMPLOYEES, Component.translatable("wiki.createkontor.page.market_analyst.title"), (e) -> {});
        ConsultantWiki.register("buyer", WikiCategory.EMPLOYEES, Component.translatable("wiki.createkontor.page.buyer.title"), (e) -> {});
    }
}
