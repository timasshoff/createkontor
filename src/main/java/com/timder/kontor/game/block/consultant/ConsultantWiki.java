package com.timder.kontor.game.block.consultant;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.game.wiki.WikiCategory;
import com.timder.kontor.game.wiki.WikiEntry;
import com.timder.kontor.game.wiki.WikiRegistry;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public final class ConsultantWiki {

    private static final WikiRegistry REGISTRY = new WikiRegistry();

    public static void register(String id, WikiCategory category, Component title, Consumer<UIElement> page) {
        REGISTRY.register(new WikiEntry(id, category, title, page));
    }

    public static void register(WikiEntry entry) {
        REGISTRY.register(entry);
    }

    public static List<WikiEntry> entries() {
        return REGISTRY.entries();
    }
}
