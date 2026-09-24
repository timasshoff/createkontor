package com.timder.kontor.datagen;

import com.timder.kontor.CreateKontor;
import com.timder.kontor.registry.KontorBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class KontorBlockStateProvider extends BlockStateProvider {

    public KontorBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CreateKontor.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        cube(KontorBlocks.SHIPPING_EXIT);
    }

    private void cube(DeferredBlock<? extends Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }
}
