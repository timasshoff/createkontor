package com.timder.kontor.datagen;

import com.timder.kontor.registry.KontorBlocks;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.function.BiConsumer;

public class KontorBlockLootTables implements LootTableSubProvider {

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        KontorBlocks.BLOCKS.getEntries().forEach(entry -> dropSelf(consumer, entry.get()));
    }

    private static void dropSelf(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer, Block block) {
        consumer.accept(block.getLootTable(), LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(block))
                .when(ExplosionCondition.survivesExplosion())));
    }
}
