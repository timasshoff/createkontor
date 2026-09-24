package com.timder.kontor.datagen;

import com.timder.kontor.CreateKontor;
import com.timder.kontor.registry.KontorBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class KontorBlockTagsProvider extends BlockTagsProvider {

    public KontorBlockTagsProvider(PackOutput output,
                                   CompletableFuture<HolderLookup.Provider> lookupProvider,
                                   ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CreateKontor.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        KontorBlocks.BLOCKS.getEntries().forEach(entry -> pickaxe.add(entry.get()));
    }
}
