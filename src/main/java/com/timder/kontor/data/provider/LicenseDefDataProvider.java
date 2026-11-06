package com.timder.kontor.data.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.timder.kontor.CreateKontor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class LicenseDefDataProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;

    public LicenseDefDataProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "kontor/license_definitions");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        /*
        Each license has a reference cost.
        It is calculated as the REFERENCE_FEE_RATE * Base Demand * Product Reference cost.

        The fee factor is a factor for both the daily fee and the application fee.
        This factor is applied onto the reference cost. It can be used to make an entire license cheaper or more expensive.

        The application fee is the reference cost * fee factor * 3.

        The daily fraction is the fraction of the reference cost * fee factor that is due every day.

        The revenue share is the fraction of revenue of the product that is due every day.
         */

        futures.add(save(cache, "charcoal", "charcoal", List.of("charcoal"), 0.8, 1.0, 0.0, 1));

        futures.add(save(
                cache,
                "oak_bundle",
                "oak_bundle",
                List.of("oak_planks", "oak_stairs", "oak_slab", "oak_fence"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "oak_accessories_bundle",
                "oak_accessories_bundle",
                List.of("oak_fence_gate", "oak_door", "oak_trapdoor", "create:oak_window"),
                0.8,
                0.1,
                0.1,
                2));

        futures.add(save(
                cache,
                "spruce_bundle",
                "spruce_bundle",
                List.of("spruce_planks", "spruce_stairs", "spruce_slab", "spruce_fence"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "spruce_accessories_bundle",
                "spruce_accessories_bundle",
                List.of("spruce_fence_gate", "spruce_door", "spruce_trapdoor", "create:spruce_window"),
                0.8,
                0.1,
                0.1,
                2));

        futures.add(save(
                cache,
                "birch_bundle",
                "birch_bundle",
                List.of("birch_planks", "birch_stairs", "birch_slab", "birch_fence"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "birch_accessories_bundle",
                "birch_accessories_bundle",
                List.of("birch_fence_gate", "birch_door", "birch_trapdoor", "create:birch_window"),
                0.8,
                0.1,
                0.1,
                2));

        futures.add(save(
                cache,
                "jungle_bundle",
                "jungle_bundle",
                List.of("jungle_planks", "jungle_stairs", "jungle_slab", "jungle_fence"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "jungle_accessories_bundle",
                "jungle_accessories_bundle",
                List.of("jungle_fence_gate", "jungle_door", "jungle_trapdoor", "create:jungle_window"),
                0.8,
                0.1,
                0.1,
                2));

        futures.add(save(
                cache,
                "acacia_bundle",
                "acacia_bundle",
                List.of("acacia_planks", "acacia_stairs", "acacia_slab", "acacia_fence"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "acacia_accessories_bundle",
                "acacia_accessories_bundle",
                List.of("acacia_fence_gate", "acacia_door", "acacia_trapdoor", "create:acacia_window"),
                0.8,
                0.1,
                0.1,
                2));

        futures.add(save(
                cache,
                "dark_oak_bundle",
                "dark_oak_bundle",
                List.of("dark_oak_planks", "dark_oak_stairs", "dark_oak_slab", "dark_oak_fence"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "dark_oak_accessories_bundle",
                "dark_oak_accessories_bundle",
                List.of("dark_oak_fence_gate", "dark_oak_door", "dark_oak_trapdoor", "create:dark_oak_window"),
                0.8,
                0.1,
                0.1,
                2));

        futures.add(save(
                cache,
                "mangrove_bundle",
                "mangrove_bundle",
                List.of("mangrove_planks", "mangrove_stairs", "mangrove_slab", "mangrove_fence"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "mangrove_accessories_bundle",
                "mangrove_accessories_bundle",
                List.of("mangrove_fence_gate", "mangrove_door", "mangrove_trapdoor", "create:mangrove_window"),
                0.8,
                0.1,
                0.15,
                2));

        futures.add(save(
                cache,
                "cherry_bundle",
                "cherry_bundle",
                List.of("cherry_planks", "cherry_stairs", "cherry_slab", "cherry_fence"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "cherry_accessories_bundle",
                "cherry_accessories_bundle",
                List.of("cherry_fence_gate", "cherry_door", "cherry_trapdoor", "create:cherry_window"),
                0.8,
                0.1,
                0.1,
                2));

        futures.add(save(
                cache,
                "nether_wood_bundle",
                "nether_wood_bundle",
                List.of(
                        "crimson_planks",
                        "crimson_stairs",
                        "crimson_slab",
                        "warped_planks",
                        "warped_stairs",
                        "warped_slab"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "nether_wood_bundle_turnover",
                "nether_wood_bundle_turnover",
                List.of(
                        "crimson_planks",
                        "crimson_stairs",
                        "crimson_slab",
                        "warped_planks",
                        "warped_stairs",
                        "warped_slab"),
                0.8,
                0.1,
                0.06,
                1));

        futures.add(save(
                cache,
                "nether_wood_accessories_bundle",
                "nether_wood_accessories_bundle",
                List.of(
                        "crimson_fence_gate",
                        "crimson_door",
                        "crimson_trapdoor",
                        "create:crimson_window",
                        "warped_fence_gate",
                        "warped_door",
                        "warped_trapdoor",
                        "create:warped_window"),
                0.8,
                0.1,
                0.1,
                2));

        futures.add(save(
                cache,
                "nether_fence_bundle",
                "nether_fence_bundle",
                List.of("crimson_fence", "warped_fence"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "wood_furniture",
                "wood_furniture",
                List.of("barrel", "chest", "crafting_table", "ladder", "stick"),
                0.7,
                0.1,
                0.1,
                1));

        futures.add(save(
                cache,
                "cobblestone_bundle",
                "cobblestone_bundle",
                List.of("cobblestone_stairs", "cobblestone_slab", "cobblestone_wall"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "stone_bundle",
                "stone_bundle",
                List.of(
                        "stone",
                        "stone_stairs",
                        "stone_slab",
                        "stone_bricks",
                        "stone_brick_stairs",
                        "stone_brick_slab",
                        "stone_brick_wall"),
                0.8,
                0.1,
                0.05,
                1));

        futures.add(save(
                cache,
                "small_andesite_bundle",
                "small_andesite_bundle",
                List.of("andesite_stairs", "andesite_slab", "andesite_wall"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "large_andesite_bundle",
                "large_andesite_bundle",
                List.of(
                        "andesite_stairs",
                        "andesite_slab",
                        "andesite_wall",
                        "polished_andesite",
                        "polished_andesite_stairs",
                        "polished_andesite_slab",
                        "create:andesite_alloy"),
                0.8,
                0.15,
                0.05,
                1));

        futures.add(save(
                cache,
                "diorite_bundle",
                "diorite_bundle",
                List.of("diorite", "diorite_slab", "diorite_stairs", "diorite_wall"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "granite_diorite_bundle",
                "granite_diorite_bundle",
                List.of(
                        "granite",
                        "granite_slab",
                        "granite_stairs",
                        "granite_wall",
                        "diorite",
                        "diorite_slab",
                        "diorite_stairs",
                        "diorite_wall"),
                0.8,
                0.12,
                0.07,
                2));

        futures.add(save(
                cache,
                "soft_stone_bundle",
                "soft_stone_bundle",
                List.of(
                        "sandstone_slab",
                        "sandstone_stairs",
                        "sandstone_wall",
                        "tuff_slab",
                        "tuff_stairs",
                        "tuff_wall"),
                0.8,
                0.1,
                0.06,
                1));

        futures.add(save(
                cache,
                "cobbled_deepslate_bundle",
                "cobbled_deepslate_bundle",
                List.of("cobbled_deepslate_stairs", "cobbled_deepslate_slab", "cobbled_deepslate_wall"),
                0.8,
                1.0,
                0.00,
                1));

        futures.add(save(
                cache,
                "deepslate_masonry_bundle",
                "deepslate_masonry_bundle",
                List.of(
                        "polished_deepslate",
                        "polished_deepslate_slab",
                        "polished_deepslate_stairs",
                        "polished_deepslate_wall",
                        "deepslate_bricks",
                        "deepslate_brick_slab",
                        "deepslate_brick_stairs",
                        "deepslate_brick_wall"),
                0.8,
                1.0,
                0.0,
                2));

        futures.add(save(
                cache,
                "deepslate_masonry_bundle_turnover",
                "deepslate_masonry_bundle_turnover",
                List.of(
                        "polished_deepslate",
                        "polished_deepslate_slab",
                        "polished_deepslate_stairs",
                        "polished_deepslate_wall",
                        "deepslate_bricks",
                        "deepslate_brick_slab",
                        "deepslate_brick_stairs",
                        "deepslate_brick_wall"),
                0.8,
                0.1,
                0.07,
                2));

        futures.add(save(
                cache,
                "blackstone_bundle",
                "blackstone_bundle",
                List.of("blackstone", "blackstone_stairs", "blackstone_slab", "blackstone_wall"),
                0.8,
                0.16,
                0.14,
                2));

        futures.add(save(
                cache,
                "polished_blackstone_bundle",
                "polished_blackstone_bundle",
                List.of(
                        "polished_blackstone",
                        "polished_blackstone_slab",
                        "polished_blackstone_stairs",
                        "polished_blackstone_wall"),
                0.8,
                0.16,
                0.12,
                3));

        futures.add(save(
                cache,
                "brick_bundle",
                "brick_bundle",
                List.of("brick", "brick_stairs", "brick_slab", "brick_wall", "bricks"),
                0.8,
                0.15,
                0.08,
                1));

        futures.add(save(
                cache,
                "nether_brick_bundle",
                "nether_brick_bundle",
                List.of("nether_bricks", "nether_brick_slab", "nether_brick_stairs", "nether_brick_wall"),
                0.8,
                0.15,
                0.1,
                3));

        futures.add(save(
                cache,
                "cut_stone_bundle_1",
                "cut_stone_bundle_1",
                List.of(
                        "create:cut_andesite",
                        "create:cut_andesite_slab",
                        "create:cut_andesite_stairs",
                        "create:cut_tuff",
                        "create:cut_tuff_slab",
                        "create:cut_tuff_stairs",
                        "create:cut_calcite",
                        "create:cut_calcite_slab",
                        "create:cut_calcite_stairs"),
                0.8,
                1.0,
                0.0,
                2));

        futures.add(save(
                cache,
                "cut_stone_bundle_2",
                "cut_stone_bundle_2",
                List.of(
                        "create:cut_dripstone",
                        "create:cut_dripstone_slab",
                        "create:cut_dripstone_stairs",
                        "create:cut_limestone",
                        "create:cut_limestone_slab",
                        "create:cut_limestone_stairs",
                        "create:cut_deepslate",
                        "create:cut_deepslate_slab",
                        "create:cut_deepslate_stairs"),
                0.8,
                0.12,
                0.07,
                2));

        futures.add(save(
                cache,
                "cut_nether_stone_bundle",
                "cut_nether_stone_bundle",
                List.of(
                        "create:cut_scoria",
                        "create:cut_scoria_slab",
                        "create:cut_scoria_stairs",
                        "create:cut_scorchia",
                        "create:cut_scorchia_slab",
                        "create:cut_scorchia_stairs"),
                0.8,
                1.0,
                0.0,
                3));

        futures.add(save(
                cache,
                "cut_rock_bundle",
                "cut_rock_bundle",
                List.of(
                        "create:cut_diorite",
                        "create:cut_diorite_slab",
                        "create:cut_diorite_stairs",
                        "create:cut_granite",
                        "create:cut_granite_slab",
                        "create:cut_granite_stairs"),
                0.8,
                0.15,
                0.08,
                3));

        futures.add(save(
                cache,
                "cut_ore_stone_bundle_1",
                "cut_ore_stone_bundle_1",
                List.of(
                        "create:cut_asurine",
                        "create:cut_asurine_slab",
                        "create:cut_asurine_stairs",
                        "create:cut_veridium",
                        "create:cut_veridium_slab",
                        "create:cut_veridium_stairs"),
                0.8,
                1.0,
                0.0,
                3));

        futures.add(save(
                cache,
                "cut_ore_stone_bundle_2",
                "cut_ore_stone_bundle_2",
                List.of(
                        "create:cut_crimsite",
                        "create:cut_crimsite_slab",
                        "create:cut_crimsite_stairs",
                        "create:cut_ochrum",
                        "create:cut_ochrum_slab",
                        "create:cut_ochrum_stairs"),
                0.8,
                0.2,
                0.08,
                3));

        futures.add(save(
                cache,
                "cut_ore_stone_collection",
                "cut_ore_stone_collection",
                List.of(
                        "create:cut_asurine",
                        "create:cut_asurine_slab",
                        "create:cut_asurine_stairs",
                        "create:cut_veridium",
                        "create:cut_veridium_slab",
                        "create:cut_veridium_stairs",
                        "create:cut_crimsite",
                        "create:cut_crimsite_slab",
                        "create:cut_crimsite_stairs",
                        "create:cut_ochrum",
                        "create:cut_ochrum_slab",
                        "create:cut_ochrum_stairs"),
                0.9,
                0.15,
                0.09,
                4));

        futures.add(save(
                cache,
                "copper_shingle_bundle",
                "copper_shingle_bundle",
                List.of("create:copper_shingles", "create:copper_shingle_slab", "create:copper_shingle_stairs"),
                0.8,
                1.0,
                0.0,
                2));

        futures.add(save(
                cache,
                "copper_roof_bundle",
                "copper_roof_bundle",
                List.of(
                        "create:copper_shingles",
                        "create:copper_shingle_slab",
                        "create:copper_shingle_stairs",
                        "create:copper_tiles",
                        "create:copper_tile_slab",
                        "create:copper_tile_stairs"),
                0.8,
                0.1,
                0.08,
                3));

        futures.add(save(
                cache,
                "cut_copper_bundle",
                "cut_copper_bundle",
                List.of("cut_copper", "cut_copper_slab", "cut_copper_stairs"),
                0.8,
                0.15,
                0.07,
                3));

        futures.add(save(
                cache,
                "clear_glass_bundle",
                "clear_glass_bundle",
                List.of("glass", "glass_pane", "create:framed_glass", "create:tiled_glass"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "colored_glass_bundle_1",
                "colored_glass_bundle_1",
                List.of(
                        "orange_stained_glass",
                        "yellow_stained_glass",
                        "white_stained_glass",
                        "gray_stained_glass",
                        "blue_stained_glass",
                        "green_stained_glass",
                        "red_stained_glass",
                        "black_stained_glass"),
                0.8,
                1.0,
                0.0,
                3));

        futures.add(save(
                cache,
                "colored_glass_bundle_2",
                "colored_glass_bundle_2",
                List.of(
                        "magenta_stained_glass",
                        "light_blue_stained_glass",
                        "lime_stained_glass",
                        "light_gray_stained_glass",
                        "cyan_stained_glass",
                        "purple_stained_glass",
                        "pink_stained_glass",
                        "brown_stained_glass"),
                0.9,
                0.16,
                0.09,
                3));

        futures.add(save(
                cache,
                "terracotta_basic_bundle",
                "terracotta_basic_bundle",
                List.of(
                        "white_terracotta",
                        "light_gray_terracotta",
                        "gray_terracotta",
                        "black_terracotta",
                        "brown_terracotta"),
                0.8,
                1.0,
                0.0,
                2));

        futures.add(save(
                cache,
                "terracotta_bright_bundle",
                "terracotta_bright_bundle",
                List.of(
                        "red_terracotta",
                        "orange_terracotta",
                        "yellow_terracotta",
                        "lime_terracotta",
                        "green_terracotta",
                        "cyan_terracotta",
                        "light_blue_terracotta",
                        "blue_terracotta",
                        "purple_terracotta",
                        "magenta_terracotta",
                        "pink_terracotta"),
                0.9,
                0.12,
                0.07,
                2));

        futures.add(save(
                cache,
                "terracotta_collection",
                "terracotta_collection",
                List.of(
                        "black_terracotta",
                        "blue_terracotta",
                        "brown_terracotta",
                        "cyan_terracotta",
                        "gray_terracotta",
                        "green_terracotta",
                        "light_blue_terracotta",
                        "light_gray_terracotta",
                        "lime_terracotta",
                        "magenta_terracotta",
                        "orange_terracotta",
                        "pink_terracotta",
                        "purple_terracotta",
                        "red_terracotta",
                        "white_terracotta",
                        "yellow_terracotta"),
                0.8,
                0.08,
                0.09,
                3));

        futures.add(save(
                cache,
                "wool_basic_bundle",
                "wool_basic_bundle",
                List.of("white_wool", "light_gray_wool", "gray_wool", "black_wool"),
                0.8,
                1.0,
                0.0,
                2));

        futures.add(save(
                cache,
                "wool_warm_bundle",
                "wool_warm_bundle",
                List.of("red_wool", "orange_wool", "yellow_wool", "pink_wool", "brown_wool", "magenta_wool"),
                0.9,
                0.15,
                0.07,
                3));

        futures.add(save(
                cache,
                "wool_cool_bundle",
                "wool_cool_bundle",
                List.of("lime_wool", "green_wool", "cyan_wool", "light_blue_wool", "blue_wool", "purple_wool"),
                0.8,
                0.05,
                0.12,
                3));

        futures.add(save(
                cache,
                "dye_bundle_1",
                "dye_bundle_1",
                List.of(
                        "orange_dye",
                        "yellow_dye",
                        "white_dye",
                        "gray_dye",
                        "blue_dye",
                        "green_dye",
                        "red_dye",
                        "black_dye"),
                0.8,
                1.0,
                0.0,
                2));

        futures.add(save(
                cache,
                "dye_bundle_2",
                "dye_bundle_2",
                List.of(
                        "magenta_dye",
                        "light_blue_dye",
                        "lime_dye",
                        "light_gray_dye",
                        "cyan_dye",
                        "purple_dye",
                        "pink_dye",
                        "brown_dye"),
                0.9,
                0.15,
                0.08,
                2));

        futures.add(save(
                cache,
                "metal_bundle",
                "metal_bundle_1",
                List.of("iron_ingot", "copper_ingot", "create:zinc_ingot", "create:brass_ingot", "gold_ingot"),
                0.8,
                1.0,
                0.0,
                2));

        futures.add(save(cache, "netherite_ingot", "netherite_ingot", List.of("netherite_ingot"), 0.8, 0.15, 0.125, 3));

        futures.add(save(
                cache,
                "sheet_metal_bundle",
                "sheet_metal_bundle",
                List.of("create:iron_sheet", "create:brass_sheet", "create:copper_sheet", "create:golden_sheet"),
                0.8,
                1.0,
                0.0,
                2));

        futures.add(save(
                cache,
                "advanced_materials_bundle",
                "advanced_materials_bundle",
                List.of("create:sturdy_sheet", "create:rose_quartz"),
                0.8,
                0.1,
                0.12,
                3));

        futures.add(save(
                cache,
                "casing_bundle",
                "casing_bundle",
                List.of(
                        "create:andesite_casing",
                        "create:copper_casing",
                        "create:brass_casing",
                        "create:railway_casing"),
                0.8,
                1.0,
                0.0,
                3));

        futures.add(save(
                cache,
                "metal_works_bundle_1",
                "metal_works_bundle_1",
                List.of("create:industrial_iron_block", "create:metal_girder", "create:andesite_bars", "iron_bars"),
                0.8,
                1.0,
                0.0,
                2));

        futures.add(save(
                cache,
                "metal_works_bundle_2",
                "metal_works_bundle_2",
                List.of(
                        "create:industrial_iron_window",
                        "create:ornate_iron_window",
                        "create:weathered_iron_window",
                        "create:brass_bars",
                        "create:copper_bars"),
                0.8,
                0.17,
                0.18,
                3));

        futures.add(save(
                cache,
                "basic_mechanical_bundle",
                "basic_mechanical_bundle",
                List.of("create:andesite_alloy", "create:shaft", "create:cogwheel", "create:large_cogwheel"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "advanced_mechanical_bundle",
                "advanced_mechanical_bundle",
                List.of("create:gearbox", "create:gearshift", "create:clutch", "create:mechanical_bearing"),
                0.8,
                1.0,
                0.0,
                2));

        futures.add(save(
                cache,
                "small_power_bundle",
                "small_power_bundle",
                List.of(
                        "create:speedometer",
                        "create:stressometer",
                        "create:water_wheel",
                        "create:large_water_wheel",
                        "create:windmill_bearing"),
                0.8,
                1.0,
                0.0,
                2));

        futures.add(save(
                cache,
                "large_power_bundle",
                "large_power_bundle",
                List.of(
                        "create:speedometer",
                        "create:stressometer",
                        "create:steam_engine",
                        "create:rotation_speed_controller"),
                0.9,
                0.2,
                0.12,
                4));

        futures.add(save(
                cache,
                "logistics_bundle_1",
                "logistics_bundle_1",
                List.of("create:andesite_funnel", "create:andesite_tunnel", "create:belt_connector", "hopper"),
                0.8,
                1.0,
                0.0,
                2));

        futures.add(save(
                cache,
                "logistics_bundle_2",
                "logistics_bundle_2",
                List.of("create:item_vault", "create:fluid_tank", "create:fluid_pipe", "create:mechanical_pump"),
                0.8,
                0.15,
                0.1,
                2));

        futures.add(save(
                cache,
                "logistics_bundle_3",
                "logistics_bundle_3",
                List.of("create:factory_gauge", "create:brass_funnel", "create:brass_tunnel"),
                0.8,
                1.0,
                0.03,
                3));

        futures.add(save(
                cache,
                "chute_conveyor_bundle",
                "chute_conveyor_bundle",
                List.of("create:chute", "create:chain_conveyor", "create:cardboard"),
                0.8,
                1.0,
                0.0,
                2));

        futures.add(save(
                cache,
                "packaging_bundle",
                "packaging_bundle",
                List.of("create:packager", "create:repackager", "create:package_frogport"),
                0.8,
                0.12,
                0.07,
                3));

        futures.add(save(
                cache,
                "stock_network_bundle",
                "stock_network_bundle",
                List.of("create:stock_link", "create:stock_ticker", "create:redstone_requester"),
                0.9,
                1.0,
                0.0,
                4));

        futures.add(save(
                cache,
                "stock_network_bundle_turnover",
                "stock_network_bundle_turnover",
                List.of("create:stock_link", "create:stock_ticker", "create:redstone_requester"),
                0.9,
                0.08,
                0.14,
                4));

        futures.add(save(
                cache,
                "railway_bundle",
                "railway_bundle",
                List.of("create:track", "create:track_signal", "create:track_station"),
                0.8,
                0.1,
                0.1,
                3));

        futures.add(save(cache, "rail_bundle", "rail_bundle", List.of("rail", "powered_rail"), 0.8, 1.0, 0.0, 2));

        futures.add(save(
                cache,
                "processing_bundle_1",
                "processing_bundle_1",
                List.of(
                        "create:basin",
                        "create:depot",
                        "create:item_drain",
                        "create:mechanical_mixer",
                        "create:mechanical_press",
                        "create:spout",
                        "create:millstone"),
                0.8,
                1.0,
                0.00,
                2));

        futures.add(save(
                cache,
                "processing_bundle_2",
                "processing_bundle_2",
                List.of(
                        "create:mechanical_plough",
                        "create:mechanical_saw",
                        "create:mechanical_piston",
                        "create:sticky_mechanical_piston",
                        "create:encased_fan",
                        "create:mechanical_harvester"),
                0.8,
                1.0,
                0.00,
                3));

        futures.add(save(
                cache,
                "processing_bundle_3",
                "processing_bundle_3",
                List.of(
                        "create:deployer",
                        "create:mechanical_drill",
                        "create:mechanical_crafter",
                        "create:crushing_wheel"),
                0.9,
                0.5,
                0.1,
                4));

        futures.add(save(
                cache,
                "piston_bundle",
                "piston_bundle",
                List.of("piston", "sticky_piston", "observer"),
                0.8,
                1.0,
                0.0,
                2));

        futures.add(save(
                cache,
                "redstone_bundle",
                "redstone_bundle",
                List.of("repeater", "comparator", "redstone_lamp"),
                0.8,
                0.12,
                0.09,
                3));

        futures.add(save(
                cache,
                "electronics_bundle",
                "electronics_bundle",
                List.of("create:electron_tube", "create:nixie_tube"),
                0.8,
                1.0,
                0.0,
                3));

        futures.add(save(
                cache,
                "precision_electronics_bundle",
                "precision_electronics_bundle",
                List.of("create:electron_tube", "create:nixie_tube", "create:precision_mechanism"),
                0.9,
                0.1,
                0.1,
                4));

        futures.add(save(
                cache,
                "robotics_bundle",
                "robotics_bundle",
                List.of("create:mechanical_arm", "create:clockwork_bearing"),
                0.9,
                0.4,
                0.05,
                4));

        futures.add(save(
                cache,
                "baking_bundle",
                "baking_bundle",
                List.of("create:wheat_flour", "create:dough", "bread", "baked_potato"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "butcher_bundle",
                "butcher_bundle",
                List.of("cooked_beef", "cooked_porkchop", "cooked_mutton", "cooked_chicken", "cooked_rabbit"),
                0.8,
                1.0,
                0.0,
                1));

        futures.add(save(
                cache,
                "grill_bundle",
                "grill_bundle",
                List.of(
                        "cooked_beef",
                        "cooked_porkchop",
                        "cooked_mutton",
                        "cooked_chicken",
                        "cooked_rabbit",
                        "cooked_cod",
                        "cooked_salmon"),
                0.8,
                0.1,
                0.06,
                1));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(
            CachedOutput cache,
            String fileName,
            String id,
            List<String> markets,
            double feeFactor,
            double dailyFraction,
            double revenueShare,
            int minLegalLevel) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        JsonArray marketArray = new JsonArray();
        for (String market : markets) {
            marketArray.add(market);
        }
        json.add("markets", marketArray);
        json.addProperty("fee_factor", feeFactor);
        json.addProperty("daily_fraction", dailyFraction);
        json.addProperty("revenue_share", revenueShare);
        json.addProperty("min_legal_level", minLegalLevel);

        ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, fileName);
        return DataProvider.saveStable(cache, json, pathProvider.json(fileId));
    }

    @Override
    public String getName() {
        return "Kontor License Definitions";
    }
}
