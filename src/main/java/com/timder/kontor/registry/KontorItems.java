package com.timder.kontor.registry;

import static com.timder.kontor.registry.KontorRegistries.REGISTRATE;

import com.timder.kontor.game.item.NotebookItem;
import com.tterrag.registrate.util.entry.ItemEntry;

public final class KontorItems {

    public static final ItemEntry<NotebookItem> NOTEBOOK = REGISTRATE
            .item("notebook", NotebookItem::new)
            .properties(properties -> properties.stacksTo(1))
            .register();

    static void touch() {}

    private KontorItems() {}
}
