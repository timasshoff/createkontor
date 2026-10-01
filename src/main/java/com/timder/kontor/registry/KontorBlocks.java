package com.timder.kontor.registry;

import com.timder.kontor.game.block.KontorDeskBlock;
import com.timder.kontor.game.block.ShippingExitBlock;
import com.timder.kontor.game.block.LawyerDeskBlock;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import static com.timder.kontor.registry.KontorRegistries.REGISTRATE;

public final class KontorBlocks {

    public static final BlockEntry<ShippingExitBlock> SHIPPING_EXIT = REGISTRATE
            .block("shipping_exit", ShippingExitBlock::new)
            .properties(p -> p
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .blockstate((ctx, prov) -> prov.simpleBlockWithItem(ctx.get(), prov.cubeAll(ctx.get())))
            .loot((loot, block) -> loot.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(block))
                    .when(ExplosionCondition.survivesExplosion()))))
            .lang("Shipping Exit")
            .simpleItem()
            .register();

    public static final BlockEntry<KontorDeskBlock> KONTOR_DESK = REGISTRATE
            .block("kontor_desk", KontorDeskBlock::new)
            .properties(p -> p
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .blockstate((ctx, prov) -> prov.simpleBlockWithItem(ctx.get(), prov.cubeAll(ctx.get())))
            .loot((loot, block) -> loot.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(block))
                    .when(ExplosionCondition.survivesExplosion()))))
            .lang("Kontor Desk")
            .simpleItem()
            .register();

    public static final BlockEntry<LawyerDeskBlock> LAWYER_DESK = REGISTRATE
            .block("lawyer_desk", LawyerDeskBlock::new)
            .properties(p -> p
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .blockstate((ctx, prov) -> prov.simpleBlockWithItem(ctx.get(), prov.cubeAll(ctx.get())))
            .loot((loot, block) -> loot.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(block))
                    .when(ExplosionCondition.survivesExplosion()))))
            .lang("Lawyer's Desk")
            .simpleItem()
            .register();

    static void touch() {
    }

    private KontorBlocks() {
    }
}
