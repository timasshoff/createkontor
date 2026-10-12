package com.timder.kontor.game.wiki.page;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.game.ui.element.WikiElements;
import net.minecraft.network.chat.Component;

public final class LegalFormsWikiPage {
    public static void build(UIElement page) {
        page.addChild(WikiElements.paragraph(Component.translatable("wiki.createkontor.page.legal_forms.p1")));
        page.addChild(WikiElements.bold(Component.translatable("wiki.createkontor.page.legal_forms.b1")));
        page.addChild(WikiElements.paragraph(Component.translatable("wiki.createkontor.page.legal_forms.p2")));
        page.addChild(WikiElements.bulletList(Component.translatable("wiki.createkontor.page.legal_forms.requirements")));
        page.addChild(WikiElements.paragraph(Component.translatable("wiki.createkontor.page.legal_forms.p3")));
        page.addChild(WikiElements.paragraph(Component.translatable("wiki.createkontor.page.legal_forms.p4")));
    }
}
