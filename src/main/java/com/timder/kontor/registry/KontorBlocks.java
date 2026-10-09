package com.timder.kontor.registry;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.timder.kontor.game.block.KontorDeskBlock;
import com.timder.kontor.game.block.MarketAnalystDeskBlock;
import com.timder.kontor.game.block.ShippingExitBlock;
import com.timder.kontor.game.block.LawyerDeskBlock;
import com.timder.kontor.game.block.buyer.BuyerDeskBlock;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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
            .onRegisterAfter(Registries.ITEM, KontorBlocks::tooltip)
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
            .onRegisterAfter(Registries.ITEM, KontorBlocks::tooltip)
            .simpleItem()
            .register();

    public static final BlockEntry<BuyerDeskBlock> BUYER_DESK = REGISTRATE
            .block("buyer_desk", BuyerDeskBlock::new)
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
            .lang("Buyer's Desk")
            .onRegisterAfter(Registries.ITEM, KontorBlocks::tooltip)
            .simpleItem()
            .register();

    public static final BlockEntry<MarketAnalystDeskBlock> MARKET_ANALYST_DESK = REGISTRATE
            .block("market_analyst_desk", MarketAnalystDeskBlock::new)
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
            .lang("Market Analyst's Desk")
            .onRegisterAfter(Registries.ITEM, KontorBlocks::tooltip)
            .simpleItem()
            .register();

    static void touch() {
    }

    public static void tooltip(Block block) {
        Item item = block.asItem();
        TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE));
    }
}
