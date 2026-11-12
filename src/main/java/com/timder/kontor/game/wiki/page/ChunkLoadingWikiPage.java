package com.timder.kontor.game.wiki.page;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.config.PropertyConfig;
import com.timder.kontor.game.ui.element.WikiUtils;
import net.minecraft.network.chat.Component;

public class ChunkLoadingWikiPage {
    public static void build(UIElement page) {
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.chunk_loading.p1")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.chunk_loading.b1")));
        page.addChild(WikiUtils.paragraph(Component.translatable(
                "wiki.createkontor.page.chunk_loading.p2", PropertyConfig.MAX_LOADED_CHUNKS.get())));
    }
}
