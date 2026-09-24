package com.timder.kontor.registry;

import com.timder.kontor.CreateKontor;
import com.timder.kontor.game.block.ShippingExitBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public final class KontorBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateKontor.MODID);

    public static final DeferredBlock<ShippingExitBlock> SHIPPING_EXIT =
            registerWithItem("shipping_exit", ShippingExitBlock::new, basicProperties());

    private static <B extends Block> DeferredBlock<B> registerWithItem(String name,
                                                                       Function<BlockBehaviour.Properties, B> factory,
                                                                       BlockBehaviour.Properties properties) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, properties);
        KontorItems.ITEMS.registerSimpleBlockItem(name, block);
        return block;
    }

    /**
     * Properties for the andesite tier blocks. Recipes come later.
     */
    private static BlockBehaviour.Properties basicProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE);
    }
}
