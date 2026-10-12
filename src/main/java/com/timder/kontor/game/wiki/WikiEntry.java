package com.timder.kontor.game.wiki;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;

public record WikiEntry(String id, WikiCategory category, Component title, Consumer<UIElement> page) {
    public WikiEntry {
        Objects.requireNonNull(id, "id must not be null.");
        Objects.requireNonNull(category, "category must not be null.");
        Objects.requireNonNull(title, "title must not be null.");
        Objects.requireNonNull(page, "page must not be null.");
    }
}
