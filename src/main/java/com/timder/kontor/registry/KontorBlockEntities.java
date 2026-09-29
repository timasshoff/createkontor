package com.timder.kontor.registry;

import com.timder.kontor.game.block.KontorDeskBlockEntity;
import com.timder.kontor.game.block.ShippingExitBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.timder.kontor.registry.KontorRegistries.REGISTRATE;

public final class KontorBlockEntities {

    public static final BlockEntityEntry<ShippingExitBlockEntity> SHIPPING_EXIT = REGISTRATE
            .blockEntity("shipping_exit", ShippingExitBlockEntity::new)
            .validBlocks(KontorBlocks.SHIPPING_EXIT)
            .register();

    public static final BlockEntityEntry<KontorDeskBlockEntity> KONTOR_DESK = REGISTRATE
            .blockEntity("kontor_desk", KontorDeskBlockEntity::new)
            .validBlocks(KontorBlocks.KONTOR_DESK)
            .register();

    static void touch() {
    }

    private KontorBlockEntities() {
    }
}
