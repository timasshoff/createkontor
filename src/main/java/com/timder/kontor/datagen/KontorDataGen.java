package com.timder.kontor.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

public final class KontorDataGen {

    private KontorDataGen() {
    }

    public static void register(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new KontorBlockStateProvider(output, event.getExistingFileHelper()));

        generator.addProvider(event.includeServer(), new LootTableProvider(
                output,
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(provider -> new KontorBlockLootTables(), LootContextParamSets.BLOCK)),
                event.getLookupProvider()));

        generator.addProvider(event.includeServer(), new KontorBlockTagsProvider(output, event.getLookupProvider(), event.getExistingFileHelper()));
    }
}
