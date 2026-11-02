package com.timder.kontor.registry;

import static com.timder.kontor.registry.KontorRegistries.REGISTRATE;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.timder.kontor.game.block.*;
import com.timder.kontor.game.block.buyer.BuyerDeskBlock;
import com.timder.kontor.game.block.consultant.ConsultantDeskBlock;
import com.timder.kontor.game.display.KontorDisplaySources;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class KontorBlocks {

    private static final int DESK_MODEL_FRONT_OFFSET = 90;

    public static final BlockEntry<ShippingExitBlock> SHIPPING_EXIT = REGISTRATE
            .block("shipping_exit", ShippingExitBlock::new)
            .properties(p -> p.mapColor(MapColor.STONE).strength(2.0F, 6.0F).sound(SoundType.STONE))
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .blockstate((ctx, prov) -> prov.simpleBlockWithItem(ctx.get(), prov.cubeAll(ctx.get())))
            .loot((loot, block) -> loot.add(
                    block,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(block))
                                    .when(ExplosionCondition.survivesExplosion()))))
            .recipe((ctx, prov) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ctx.get())
                    .requires(AllBlocks.PACKAGER)
                    .requires(AllItems.TRANSMITTER)
                    .unlockedBy("has_kontor_desk", RegistrateRecipeProvider.has(KontorBlocks.KONTOR_DESK.get()))
                    .save(prov))
            .lang("Shipping Exit")
            .simpleItem()
            .register();

    public static final BlockEntry<KontorDeskBlock> KONTOR_DESK = REGISTRATE
            .block("kontor_desk", KontorDeskBlock::new)
            .properties(KontorBlocks::deskProperties)
            .blockstate(KontorBlocks::deskBlockstate)
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .loot((loot, block) -> loot.add(
                    block,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(block))
                                    .when(ExplosionCondition.survivesExplosion()))))
            .recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern("BBB")
                    .pattern("L L")
                    .pattern("L L")
                    .define('B', AllItems.BRASS_INGOT)
                    .define('L', ItemTags.LOGS)
                    .unlockedBy("has_kontor_desk", RegistrateRecipeProvider.has(KontorBlocks.KONTOR_DESK.get()))
                    .save(prov))
            .transform(DisplaySource.displaySource(KontorDisplaySources.KONTOR_DESK))
            .lang("Kontor Desk")
            .onRegisterAfter(Registries.ITEM, KontorBlocks::tooltip)
            .simpleItem()
            .register();

    public static final BlockEntry<LawyerDeskBlock> LAWYER_DESK = REGISTRATE
            .block("lawyer_desk", LawyerDeskBlock::new)
            .properties(KontorBlocks::deskProperties)
            .blockstate(KontorBlocks::deskBlockstate)
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .loot((loot, block) -> loot.add(
                    block,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(block))
                                    .when(ExplosionCondition.survivesExplosion()))))
            .recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern("BXB")
                    .pattern("L L")
                    .pattern("L L")
                    .define('B', AllItems.BRASS_INGOT)
                    .define('L', ItemTags.LOGS)
                    .define('X', Items.BOOK)
                    .unlockedBy("has_kontor_desk", RegistrateRecipeProvider.has(KontorBlocks.KONTOR_DESK.get()))
                    .save(prov))
            .lang("Lawyer's Desk")
            .onRegisterAfter(Registries.ITEM, KontorBlocks::tooltip)
            .simpleItem()
            .register();

    public static final BlockEntry<BuyerDeskBlock> BUYER_DESK = REGISTRATE
            .block("buyer_desk", BuyerDeskBlock::new)
            .properties(p -> p.mapColor(MapColor.STONE).strength(2.0F, 6.0F).sound(SoundType.STONE))
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .blockstate((ctx, prov) -> prov.simpleBlockWithItem(ctx.get(), prov.cubeAll(ctx.get())))
            .loot((loot, block) -> loot.add(
                    block,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(block))
                                    .when(ExplosionCondition.survivesExplosion()))))
            .recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern("BXB")
                    .pattern("L L")
                    .pattern("L L")
                    .define('B', AllItems.BRASS_INGOT)
                    .define('L', ItemTags.LOGS)
                    .define('X', AllItems.CARDBOARD)
                    .unlockedBy("has_kontor_desk", RegistrateRecipeProvider.has(KontorBlocks.KONTOR_DESK.get()))
                    .save(prov))
            .lang("Buyer's Desk")
            .onRegisterAfter(Registries.ITEM, KontorBlocks::tooltip)
            .simpleItem()
            .register();

    public static final BlockEntry<MarketAnalystDeskBlock> MARKET_ANALYST_DESK = REGISTRATE
            .block("market_analyst_desk", MarketAnalystDeskBlock::new)
            .properties(p -> p.mapColor(MapColor.STONE).strength(2.0F, 6.0F).sound(SoundType.STONE))
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .blockstate((ctx, prov) -> prov.simpleBlockWithItem(ctx.get(), prov.cubeAll(ctx.get())))
            .loot((loot, block) -> loot.add(
                    block,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(block))
                                    .when(ExplosionCondition.survivesExplosion()))))
            .recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern("BXB")
                    .pattern("L L")
                    .pattern("L L")
                    .define('B', AllItems.BRASS_INGOT)
                    .define('L', ItemTags.LOGS)
                    .define('X', Items.SPYGLASS)
                    .unlockedBy("has_kontor_desk", RegistrateRecipeProvider.has(KontorBlocks.KONTOR_DESK.get()))
                    .save(prov))
            .lang("Market Analyst's Desk")
            .onRegisterAfter(Registries.ITEM, KontorBlocks::tooltip)
            .simpleItem()
            .register();

    public static final BlockEntry<ConsultantDeskBlock> CONSULTANT_DESK = REGISTRATE
            .block("consultant_desk", ConsultantDeskBlock::new)
            .properties(p -> p.mapColor(MapColor.STONE).strength(2.0F, 6.0F).sound(SoundType.STONE))
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .blockstate((ctx, prov) -> prov.simpleBlockWithItem(ctx.get(), prov.cubeAll(ctx.get())))
            .loot((loot, block) -> loot.add(
                    block,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(block))
                                    .when(ExplosionCondition.survivesExplosion()))))
            .recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern("BXB")
                    .pattern("L L")
                    .pattern("L L")
                    .define('B', AllItems.BRASS_INGOT)
                    .define('L', ItemTags.LOGS)
                    .define('X', Items.BOOKSHELF)
                    .unlockedBy("has_kontor_desk", RegistrateRecipeProvider.has(KontorBlocks.KONTOR_DESK.get()))
                    .save(prov))
            .lang("Consultant's Desk")
            .onRegisterAfter(Registries.ITEM, KontorBlocks::tooltip)
            .simpleItem()
            .register();

    public static final BlockEntry<LogisticsManagerDeskBlock> LOGISTICS_MANAGER_DESK = REGISTRATE
            .block("logistics_manager_desk", LogisticsManagerDeskBlock::new)
            .properties(KontorBlocks::deskProperties)
            .blockstate(KontorBlocks::deskBlockstate)
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .loot((loot, block) -> loot.add(
                    block,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(block))
                                    .when(ExplosionCondition.survivesExplosion()))))
            .recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern("BXB")
                    .pattern("L L")
                    .pattern("L L")
                    .define('B', AllItems.BRASS_INGOT)
                    .define('L', ItemTags.LOGS)
                    .define('X', AllBlocks.CLIPBOARD)
                    .unlockedBy("has_kontor_desk", RegistrateRecipeProvider.has(KontorBlocks.KONTOR_DESK.get()))
                    .save(prov))
            .lang("Logistics Manager's Desk")
            .onRegisterAfter(Registries.ITEM, KontorBlocks::tooltip)
            .simpleItem()
            .register();

    public static final BlockEntry<FinancesManagerDeskBlock> FINANCES_MANAGER_DESK = REGISTRATE
            .block("finances_manager_desk", FinancesManagerDeskBlock::new)
            .properties(p -> p.mapColor(MapColor.STONE).strength(2.0F, 6.0F).sound(SoundType.STONE))
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .blockstate((ctx, prov) -> prov.simpleBlockWithItem(ctx.get(), prov.cubeAll(ctx.get())))
            .loot((loot, block) -> loot.add(
                    block,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(block))
                                    .when(ExplosionCondition.survivesExplosion()))))
            .recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern("BXB")
                    .pattern("L L")
                    .pattern("L L")
                    .define('B', AllItems.BRASS_INGOT)
                    .define('L', ItemTags.LOGS)
                    .define('X', Items.EMERALD)
                    .unlockedBy("has_kontor_desk", RegistrateRecipeProvider.has(KontorBlocks.KONTOR_DESK.get()))
                    .save(prov))
            .lang("Finances Manager's Desk")
            .onRegisterAfter(Registries.ITEM, KontorBlocks::tooltip)
            .simpleItem()
            .register();

    public static final BlockEntry<PropertyManagerDeskBlock> PROPERTY_MANAGER_DESK = REGISTRATE
            .block("property_manager_desk", PropertyManagerDeskBlock::new)
            .properties(p -> p.mapColor(MapColor.STONE).strength(2.0F, 6.0F).sound(SoundType.STONE))
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .blockstate((ctx, prov) -> prov.simpleBlockWithItem(ctx.get(), prov.cubeAll(ctx.get())))
            .loot((loot, block) -> loot.add(
                    block,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(block))
                                    .when(ExplosionCondition.survivesExplosion()))))
            .recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern("BXB")
                    .pattern("L L")
                    .pattern("L L")
                    .define('B', AllItems.BRASS_INGOT)
                    .define('L', ItemTags.LOGS)
                    .define('X', Items.MAP)
                    .unlockedBy("has_kontor_desk", RegistrateRecipeProvider.has(KontorBlocks.KONTOR_DESK.get()))
                    .save(prov))
            .lang("Property Manager's Desk")
            .onRegisterAfter(Registries.ITEM, KontorBlocks::tooltip)
            .simpleItem()
            .register();

    public static void tooltip(Block block) {
        Item item = block.asItem();
        TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE));
    }

    private static BlockBehaviour.Properties deskProperties(BlockBehaviour.Properties p) {
        return p.mapColor(MapColor.WOOD)
                .strength(2.0F, 6.0F)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }

    private static <T extends Block> void deskBlockstate(
            DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov) {
        prov.horizontalBlock(
                ctx.get(),
                prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName())),
                DESK_MODEL_FRONT_OFFSET);
    }
}
