package com.timder.kontor.game.wiki.page;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.game.ui.element.WikiUtils;
import net.minecraft.network.chat.Component;

public final class LegalFormsWikiPage {
    public static void build(UIElement page) {
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.legal_forms.p1")));
        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.legal_forms.b1")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.legal_forms.p2")));
        page.addChild(WikiUtils.bulletList(Component.translatable("wiki.createkontor.page.legal_forms.requirements")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.legal_forms.p3")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.legal_forms.p4")));
    }
}
