package com.timder.kontor.registry;

import net.neoforged.bus.api.IEventBus;

public final class KontorRegistries {
    public static void register(IEventBus modEventBus) {
        KontorBlocks.BLOCKS.register(modEventBus);
        KontorItems.ITEMS.register(modEventBus);
        KontorBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        KontorCreativeTabs.CREATIVE_TABS.register(modEventBus);
    }
}
