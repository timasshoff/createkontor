package com.timder.kontor.game.block.consultant;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.game.wiki.WikiCategory;
import com.timder.kontor.game.wiki.WikiEntry;
import com.timder.kontor.game.wiki.WikiRegistry;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ConsultantWiki {

    private static final WikiRegistry REGISTRY = new WikiRegistry();

    public static void register(
            String id, WikiCategory category, Component title, Consumer<UIElement> page, ResourceLocation ponder) {
        REGISTRY.register(new WikiEntry(id, category, title, page, ponder));
    }

    public static void register(String id, WikiCategory category, Component title, Consumer<UIElement> page) {
        register(id, category, title, page, null);
    }

    public static void register(WikiEntry entry) {
        REGISTRY.register(entry);
    }

    public static List<WikiEntry> entries() {
        return REGISTRY.entries();
    }
}
