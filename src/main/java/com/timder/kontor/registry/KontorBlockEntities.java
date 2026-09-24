package com.timder.kontor.registry;

import com.timder.kontor.CreateKontor;
import com.timder.kontor.game.block.ShippingExitBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class KontorBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateKontor.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShippingExitBlockEntity>> SHIPPING_EXIT =
            BLOCK_ENTITY_TYPES.register("warehouse_exit",
                    () -> BlockEntityType.Builder.of(ShippingExitBlockEntity::new, KontorBlocks.SHIPPING_EXIT.get()).build(null));

}
