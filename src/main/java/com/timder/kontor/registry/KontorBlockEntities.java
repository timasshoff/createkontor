package com.timder.kontor.registry;

import com.timder.kontor.game.block.ShippingExitBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.timder.kontor.registry.KontorRegistries.REGISTRATE;

public final class KontorBlockEntities {

    public static final BlockEntityEntry<ShippingExitBlockEntity> SHIPPING_EXIT = REGISTRATE
            .blockEntity("shipping_exit", ShippingExitBlockEntity::new)
            .validBlocks(KontorBlocks.SHIPPING_EXIT)
            .register();

    static void touch() {
    }

    private KontorBlockEntities() {
    }
}
