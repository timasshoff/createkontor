package com.timder.kontor.data.provider;

import com.google.gson.JsonObject;
import com.timder.kontor.CreateKontor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class MarketDefinitionDataProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;

    public MarketDefinitionDataProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "kontor/market_definitions");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        /*
        Group: Basic Goods
         */

        futures.add(save(cache, "charcoal", "minecraft:charcoal", "basic_goods", 4800, 0.85, 64));

        /*
        Group: Building
         */

        futures.add(save(cache, "oak_planks", "minecraft:oak_planks", "building", 4800, 0.85, 64));
        futures.add(save(cache, "oak_stairs", "minecraft:oak_stairs", "building", 4480, 0.80, 64));
        futures.add(save(cache, "oak_slab", "minecraft:oak_slab", "building", 4640, 0.80, 64));
        futures.add(save(cache, "oak_fence", "minecraft:oak_fence", "building", 3680, 0.80, 64));
        futures.add(save(cache, "oak_fence_gate", "minecraft:oak_fence_gate", "building", 3680, 0.80, 64));
        futures.add(save(cache, "oak_door", "minecraft:oak_door", "building", 3200, 0.80, 6));
        futures.add(save(cache, "oak_trapdoor", "minecraft:oak_trapdoor", "building", 3200, 0.80, 16));
        futures.add(save(cache, "oak_window", "create:oak_window", "building", 2800, 0.80, 32));

        futures.add(save(cache, "spruce_planks", "minecraft:spruce_planks", "building", 4800, 0.85, 64));
        futures.add(save(cache, "spruce_stairs", "minecraft:spruce_stairs", "building", 4480, 0.80, 64));
        futures.add(save(cache, "spruce_slab", "minecraft:spruce_slab", "building", 4640, 0.80, 64));
        futures.add(save(cache, "spruce_fence", "minecraft:spruce_fence", "building", 3680, 0.80, 64));
        futures.add(save(cache, "spruce_fence_gate", "minecraft:spruce_fence_gate", "building", 3680, 0.80, 64));
        futures.add(save(cache, "spruce_door", "minecraft:spruce_door", "building", 3200, 0.80, 6));
        futures.add(save(cache, "spruce_trapdoor", "minecraft:spruce_trapdoor", "building", 3200, 0.80, 16));
        futures.add(save(cache, "spruce_window", "create:spruce_window", "building", 2800, 0.80, 32));

        futures.add(save(cache, "birch_planks", "minecraft:birch_planks", "building", 4800, 0.85, 64));
        futures.add(save(cache, "birch_stairs", "minecraft:birch_stairs", "building", 4480, 0.80, 64));
        futures.add(save(cache, "birch_slab", "minecraft:birch_slab", "building", 4640, 0.80, 64));
        futures.add(save(cache, "birch_fence", "minecraft:birch_fence", "building", 3680, 0.80, 64));
        futures.add(save(cache, "birch_fence_gate", "minecraft:birch_fence_gate", "building", 3680, 0.80, 64));
        futures.add(save(cache, "birch_door", "minecraft:birch_door", "building", 3200, 0.80, 6));
        futures.add(save(cache, "birch_trapdoor", "minecraft:birch_trapdoor", "building", 3200, 0.80, 16));
        futures.add(save(cache, "birch_window", "create:birch_window", "building", 2800, 0.80, 32));

        futures.add(save(cache, "jungle_planks", "minecraft:jungle_planks", "building", 4800, 0.85, 64));
        futures.add(save(cache, "jungle_stairs", "minecraft:jungle_stairs", "building", 4480, 0.80, 64));
        futures.add(save(cache, "jungle_slab", "minecraft:jungle_slab", "building", 4640, 0.80, 64));
        futures.add(save(cache, "jungle_fence", "minecraft:jungle_fence", "building", 3680, 0.80, 64));
        futures.add(save(cache, "jungle_fence_gate", "minecraft:jungle_fence_gate", "building", 3680, 0.80, 64));
        futures.add(save(cache, "jungle_door", "minecraft:jungle_door", "building", 3200, 0.80, 6));
        futures.add(save(cache, "jungle_trapdoor", "minecraft:jungle_trapdoor", "building", 3200, 0.80, 16));
        futures.add(save(cache, "jungle_window", "create:jungle_window", "building", 2800, 0.80, 32));

        futures.add(save(cache, "acacia_planks", "minecraft:acacia_planks", "building", 4800, 0.85, 64));
        futures.add(save(cache, "acacia_stairs", "minecraft:acacia_stairs", "building", 4480, 0.80, 64));
        futures.add(save(cache, "acacia_slab", "minecraft:acacia_slab", "building", 4640, 0.80, 64));
        futures.add(save(cache, "acacia_fence", "minecraft:acacia_fence", "building", 3680, 0.80, 64));
        futures.add(save(cache, "acacia_fence_gate", "minecraft:acacia_fence_gate", "building", 3680, 0.80, 64));
        futures.add(save(cache, "acacia_door", "minecraft:acacia_door", "building", 3200, 0.80, 6));
        futures.add(save(cache, "acacia_trapdoor", "minecraft:acacia_trapdoor", "building", 3200, 0.80, 16));
        futures.add(save(cache, "acacia_window", "create:acacia_window", "building", 2800, 0.80, 32));

        futures.add(save(cache, "dark_oak_planks", "minecraft:dark_oak_planks", "building", 4800, 0.85, 64));
        futures.add(save(cache, "dark_oak_stairs", "minecraft:dark_oak_stairs", "building", 4480, 0.80, 64));
        futures.add(save(cache, "dark_oak_slab", "minecraft:dark_oak_slab", "building", 4640, 0.80, 64));
        futures.add(save(cache, "dark_oak_fence", "minecraft:dark_oak_fence", "building", 3680, 0.80, 64));
        futures.add(save(cache, "dark_oak_fence_gate", "minecraft:dark_oak_fence_gate", "building", 3680, 0.80, 64));
        futures.add(save(cache, "dark_oak_door", "minecraft:dark_oak_door", "building", 3200, 0.80, 6));
        futures.add(save(cache, "dark_oak_trapdoor", "minecraft:dark_oak_trapdoor", "building", 3200, 0.80, 16));
        futures.add(save(cache, "dark_oak_window", "create:dark_oak_window", "building", 2800, 0.80, 32));

        futures.add(save(cache, "mangrove_planks", "minecraft:mangrove_planks", "building", 4800, 0.85, 64));
        futures.add(save(cache, "mangrove_stairs", "minecraft:mangrove_stairs", "building", 4480, 0.80, 64));
        futures.add(save(cache, "mangrove_slab", "minecraft:mangrove_slab", "building", 4640, 0.80, 64));
        futures.add(save(cache, "mangrove_fence", "minecraft:mangrove_fence", "building", 3680, 0.80, 64));
        futures.add(save(cache, "mangrove_fence_gate", "minecraft:mangrove_fence_gate", "building", 3680, 0.80, 64));
        futures.add(save(cache, "mangrove_door", "minecraft:mangrove_door", "building", 3200, 0.80, 6));
        futures.add(save(cache, "mangrove_trapdoor", "minecraft:mangrove_trapdoor", "building", 3200, 0.80, 16));
        futures.add(save(cache, "mangrove_window", "create:mangrove_window", "building", 2800, 0.80, 32));

        futures.add(save(cache, "cherry_planks", "minecraft:cherry_planks", "building", 4800, 0.85, 64));
        futures.add(save(cache, "cherry_stairs", "minecraft:cherry_stairs", "building", 4480, 0.80, 64));
        futures.add(save(cache, "cherry_slab", "minecraft:cherry_slab", "building", 4640, 0.80, 64));
        futures.add(save(cache, "cherry_fence", "minecraft:cherry_fence", "building", 3680, 0.80, 64));
        futures.add(save(cache, "cherry_fence_gate", "minecraft:cherry_fence_gate", "building", 3680, 0.80, 64));
        futures.add(save(cache, "cherry_door", "minecraft:cherry_door", "building", 3200, 0.80, 6));
        futures.add(save(cache, "cherry_trapdoor", "minecraft:cherry_trapdoor", "building", 3200, 0.80, 16));
        futures.add(save(cache, "cherry_window", "create:cherry_window", "building", 2800, 0.80, 32));

        futures.add(save(cache, "crimson_planks", "minecraft:crimson_planks", "building", 4800, 0.85, 64));
        futures.add(save(cache, "crimson_stairs", "minecraft:crimson_stairs", "building", 4480, 0.80, 64));
        futures.add(save(cache, "crimson_slab", "minecraft:crimson_slab", "building", 4640, 0.80, 64));
        futures.add(save(cache, "crimson_fence", "minecraft:crimson_fence", "building", 3680, 0.80, 64));
        futures.add(save(cache, "crimson_fence_gate", "minecraft:crimson_fence_gate", "building", 3680, 0.80, 64));
        futures.add(save(cache, "crimson_door", "minecraft:crimson_door", "building", 3200, 0.80, 6));
        futures.add(save(cache, "crimson_trapdoor", "minecraft:crimson_trapdoor", "building", 3200, 0.80, 16));
        futures.add(save(cache, "crimson_window", "create:crimson_window", "building", 2800, 0.80, 32));

        futures.add(save(cache, "warped_planks", "minecraft:warped_planks", "building", 4800, 0.85, 64));
        futures.add(save(cache, "warped_stairs", "minecraft:warped_stairs", "building", 4480, 0.80, 64));
        futures.add(save(cache, "warped_slab", "minecraft:warped_slab", "building", 4640, 0.80, 64));
        futures.add(save(cache, "warped_fence", "minecraft:warped_fence", "building", 3680, 0.80, 64));
        futures.add(save(cache, "warped_fence_gate", "minecraft:warped_fence_gate", "building", 3680, 0.80, 64));
        futures.add(save(cache, "warped_door", "minecraft:warped_door", "building", 3200, 0.80, 6));
        futures.add(save(cache, "warped_trapdoor", "minecraft:warped_trapdoor", "building", 3200, 0.80, 16));
        futures.add(save(cache, "warped_window", "create:warped_window", "building", 2800, 0.80, 32));

        futures.add(save(cache, "chest", "minecraft:chest", "building", 3200, 0.85, 16));
        futures.add(save(cache, "barrel", "minecraft:barrel", "building", 3200, 0.85, 16));
        futures.add(save(cache, "crafting_table", "minecraft:crafting_table", "building", 3200, 0.85, 16));
        futures.add(save(cache, "ladder", "minecraft:ladder", "building", 3700, 0.85, 64));
        futures.add(save(cache, "stick", "minecraft:stick", "building", 3700, 0.85, 64));

        futures.add(save(cache, "cobblestone_stairs", "minecraft:cobblestone_stairs", "building", 3520, 0.80, 64));
        futures.add(save(cache, "cobblestone_slab", "minecraft:cobblestone_slab", "building", 3360, 0.80, 64));
        futures.add(save(cache, "cobblestone_wall", "minecraft:cobblestone_wall", "building", 3200, 0.80, 64));

        futures.add(save(cache, "stone", "minecraft:stone", "building", 4480, 0.80, 64));
        futures.add(save(cache, "stone_stairs", "minecraft:stone_stairs", "building", 3680, 0.80, 64));
        futures.add(save(cache, "stone_slab", "minecraft:stone_slab", "building", 3680, 0.80, 64));

        futures.add(save(cache, "stone_bricks", "minecraft:stone_bricks", "building", 4480, 0.85, 64));
        futures.add(save(cache, "stone_brick_stairs", "minecraft:stone_brick_stairs", "building", 3520, 0.85, 64));
        futures.add(save(cache, "stone_brick_slabs", "minecraft:stone_brick_slab", "building", 3200, 0.85, 64));
        futures.add(save(cache, "stone_brick_wall", "minecraft:stone_brick_wall", "building", 3200, 0.85, 64));

        futures.add(save(cache, "andesite_stairs", "minecraft:andesite_stairs", "building", 3200, 0.80, 64));
        futures.add(save(cache, "andesite_slab", "minecraft:andesite_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "andesite_wall", "minecraft:andesite_wall", "building", 3200, 0.80, 64));

        futures.add(save(cache, "polished_andesite", "minecraft:polished_andesite", "building", 4480, 0.80, 64));
        futures.add(save(
                cache, "polished_andesite_stairs", "minecraft:polished_andesite_stairs", "building", 3200, 0.80, 64));
        futures.add(
                save(cache, "polished_andesite_slab", "minecraft:polished_andesite_slab", "building", 3520, 0.80, 64));

        futures.add(save(cache, "granite", "minecraft:granite", "building", 4480, 0.80, 64));
        futures.add(save(cache, "granite_stairs", "minecraft:granite_stairs", "building", 3200, 0.80, 64));
        futures.add(save(cache, "granite_slab", "minecraft:granite_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "granite_wall", "minecraft:granite_wall", "building", 3200, 0.80, 64));

        futures.add(save(cache, "diorite", "minecraft:diorite", "building", 4480, 0.80, 64));
        futures.add(save(cache, "diorite_stairs", "minecraft:diorite_stairs", "building", 3200, 0.80, 64));
        futures.add(save(cache, "diorite_slab", "minecraft:diorite_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "diorite_wall", "minecraft:diorite_wall", "building", 3200, 0.80, 64));

        futures.add(save(cache, "sandstone_stairs", "minecraft:sandstone_stairs", "building", 3200, 0.80, 64));
        futures.add(save(cache, "sandstone_slab", "minecraft:sandstone_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "sandstone_wall", "minecraft:sandstone_wall", "building", 3200, 0.80, 64));

        futures.add(save(cache, "tuff_stairs", "minecraft:tuff_stairs", "building", 3200, 0.80, 64));
        futures.add(save(cache, "tuff_slab", "minecraft:tuff_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "tuff_wall", "minecraft:tuff_wall", "building", 3200, 0.80, 64));

        futures.add(save(
                cache, "cobbled_deepslate_stairs", "minecraft:cobbled_deepslate_stairs", "building", 3200, 0.80, 64));
        futures.add(
                save(cache, "cobbled_deepslate_slab", "minecraft:cobbled_deepslate_slab", "building", 3520, 0.80, 64));
        futures.add(
                save(cache, "cobbled_deepslate_wall", "minecraft:cobbled_deepslate_wall", "building", 3200, 0.80, 64));

        futures.add(save(cache, "polished_deepslate", "minecraft:polished_deepslate", "building", 4480, 0.80, 64));
        futures.add(save(
                cache, "polished_deepslate_stairs", "minecraft:polished_deepslate_stairs", "building", 3200, 0.80, 64));
        futures.add(save(
                cache, "polished_deepslate_slab", "minecraft:polished_deepslate_slab", "building", 3520, 0.80, 64));
        futures.add(save(
                cache, "polished_deepslate_wall", "minecraft:polished_deepslate_wall", "building", 3200, 0.80, 64));

        futures.add(save(cache, "deepslate_bricks", "minecraft:deepslate_bricks", "building", 3200, 0.85, 64));
        futures.add(
                save(cache, "deepslate_brick_stairs", "minecraft:deepslate_brick_stairs", "building", 3200, 0.85, 64));
        futures.add(save(cache, "deepslate_brick_slab", "minecraft:deepslate_brick_slab", "building", 3200, 0.85, 64));
        futures.add(save(cache, "deepslate_brick_wall", "minecraft:deepslate_brick_wall", "building", 3200, 0.85, 64));

        futures.add(save(cache, "blackstone", "minecraft:blackstone", "building", 4480, 0.80, 64));
        futures.add(save(cache, "blackstone_stairs", "minecraft:blackstone_stairs", "building", 3200, 0.80, 64));
        futures.add(save(cache, "blackstone_slab", "minecraft:blackstone_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "blackstone_wall", "minecraft:blackstone_wall", "building", 3200, 0.80, 64));

        futures.add(save(cache, "polished_blackstone", "minecraft:polished_blackstone", "building", 4480, 0.80, 64));
        futures.add(save(
                cache,
                "polished_blackstone_stairs",
                "minecraft:polished_blackstone_stairs",
                "building",
                3200,
                0.80,
                64));
        futures.add(save(
                cache, "polished_blackstone_slab", "minecraft:polished_blackstone_slab", "building", 3520, 0.80, 64));
        futures.add(save(
                cache, "polished_blackstone_wall", "minecraft:polished_blackstone_wall", "building", 3200, 0.80, 64));

        futures.add(save(cache, "brick", "minecraft:brick", "building", 2400, 0.80, 64));
        futures.add(save(cache, "bricks", "minecraft:bricks", "building", 3200, 0.85, 64));
        futures.add(save(cache, "brick_stairs", "minecraft:brick_stairs", "building", 3200, 0.85, 64));
        futures.add(save(cache, "brick_slab", "minecraft:brick_slab", "building", 3200, 0.85, 64));
        futures.add(save(cache, "brick_wall", "minecraft:brick_wall", "building", 3200, 0.85, 64));

        futures.add(save(cache, "nether_bricks", "minecraft:nether_bricks", "building", 3200, 0.85, 64));
        futures.add(save(cache, "nether_brick_stairs", "minecraft:nether_brick_stairs", "building", 3200, 0.85, 64));
        futures.add(save(cache, "nether_brick_slab", "minecraft:nether_brick_slab", "building", 3200, 0.85, 64));
        futures.add(save(cache, "nether_brick_wall", "minecraft:nether_brick_wall", "building", 3200, 0.85, 64));

        futures.add(save(cache, "cut_andesite", "create:cut_andesite", "building", 4000, 0.80, 64));
        futures.add(save(cache, "cut_andesite_slab", "create:cut_andesite_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "cut_andesite_stairs", "create:cut_andesite_stairs", "building", 3200, 0.80, 64));

        futures.add(save(cache, "cut_tuff", "create:cut_tuff", "building", 4000, 0.80, 64));
        futures.add(save(cache, "cut_tuff_slab", "create:cut_tuff_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "cut_tuff_stairs", "create:cut_tuff_stairs", "building", 3200, 0.80, 64));

        futures.add(save(cache, "cut_calcite", "create:cut_calcite", "building", 4000, 0.80, 64));
        futures.add(save(cache, "cut_calcite_slab", "create:cut_calcite_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "cut_calcite_stairs", "create:cut_calcite_stairs", "building", 3200, 0.80, 64));

        futures.add(save(cache, "cut_dripstone", "create:cut_dripstone", "building", 4000, 0.80, 64));
        futures.add(save(cache, "cut_dripstone_slab", "create:cut_dripstone_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "cut_dripstone_stairs", "create:cut_dripstone_stairs", "building", 3200, 0.80, 64));

        futures.add(save(cache, "cut_limestone", "create:cut_limestone", "building", 4000, 0.80, 64));
        futures.add(save(cache, "cut_limestone_slab", "create:cut_limestone_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "cut_limestone_stairs", "create:cut_limestone_stairs", "building", 3200, 0.80, 64));

        futures.add(save(cache, "cut_deepslate", "create:cut_deepslate", "building", 4000, 0.80, 64));
        futures.add(save(cache, "cut_deepslate_slab", "create:cut_deepslate_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "cut_deepslate_stairs", "create:cut_deepslate_stairs", "building", 3200, 0.80, 64));

        futures.add(save(cache, "cut_scoria", "create:cut_scoria", "building", 4000, 0.80, 64));
        futures.add(save(cache, "cut_scoria_slab", "create:cut_scoria_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "cut_scoria_stairs", "create:cut_scoria_stairs", "building", 3200, 0.80, 64));

        futures.add(save(cache, "cut_scorchia", "create:cut_scorchia", "building", 4000, 0.80, 64));
        futures.add(save(cache, "cut_scorchia_slab", "create:cut_scorchia_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "cut_scorchia_stairs", "create:cut_scorchia_stairs", "building", 3200, 0.80, 64));

        futures.add(save(cache, "cut_diorite", "create:cut_diorite", "building", 4000, 0.80, 64));
        futures.add(save(cache, "cut_diorite_slab", "create:cut_diorite_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "cut_diorite_stairs", "create:cut_diorite_stairs", "building", 3200, 0.80, 64));

        futures.add(save(cache, "cut_granite", "create:cut_granite", "building", 4000, 0.80, 64));
        futures.add(save(cache, "cut_granite_slab", "create:cut_granite_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "cut_granite_stairs", "create:cut_granite_stairs", "building", 3200, 0.80, 64));

        futures.add(save(cache, "cut_asurine", "create:cut_asurine", "building", 4000, 0.80, 64));
        futures.add(save(cache, "cut_asurine_slab", "create:cut_asurine_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "cut_asurine_stairs", "create:cut_asurine_stairs", "building", 3200, 0.80, 64));

        futures.add(save(cache, "cut_veridium", "create:cut_veridium", "building", 4000, 0.80, 64));
        futures.add(save(cache, "cut_veridium_slab", "create:cut_veridium_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "cut_veridium_stairs", "create:cut_veridium_stairs", "building", 3200, 0.80, 64));

        futures.add(save(cache, "cut_crimsite", "create:cut_crimsite", "building", 4000, 0.80, 64));
        futures.add(save(cache, "cut_crimsite_slab", "create:cut_crimsite_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "cut_crimsite_stairs", "create:cut_crimsite_stairs", "building", 3200, 0.80, 64));

        futures.add(save(cache, "cut_ochrum", "create:cut_ochrum", "building", 4000, 0.80, 64));
        futures.add(save(cache, "cut_ochrum_slab", "create:cut_ochrum_slab", "building", 3520, 0.80, 64));
        futures.add(save(cache, "cut_ochrum_stairs", "create:cut_ochrum_stairs", "building", 3200, 0.80, 64));

        futures.add(save(cache, "cut_copper", "minecraft:cut_copper", "building", 2800, 0.80, 32));
        futures.add(save(cache, "cut_copper_stairs", "minecraft:cut_copper_stairs", "building", 2800, 0.80, 32));
        futures.add(save(cache, "cut_copper_slab", "minecraft:cut_copper_slab", "building", 2800, 0.80, 32));

        futures.add(save(cache, "copper_shingles", "create:copper_shingles", "building", 2800, 0.80, 32));
        futures.add(save(cache, "copper_shingle_stairs", "create:copper_shingle_stairs", "building", 2800, 0.80, 32));
        futures.add(save(cache, "copper_shingle_slab", "create:copper_shingle_slab", "building", 2800, 0.80, 32));

        futures.add(save(cache, "copper_tiles", "create:copper_tiles", "building", 2800, 0.80, 32));
        futures.add(save(cache, "copper_tile_stairs", "create:copper_tile_stairs", "building", 2800, 0.80, 32));
        futures.add(save(cache, "copper_tile_slab", "create:copper_tile_slab", "building", 2800, 0.80, 32));

        futures.add(save(cache, "glass", "minecraft:glass", "building", 2560, 0.80, 64));
        futures.add(save(cache, "glass_pane", "minecraft:glass_pane", "building", 2560, 0.80, 64));
        futures.add(save(cache, "framed_glass", "create:framed_glass", "building", 2800, 0.80, 32));
        futures.add(save(cache, "tiled_glass", "create:tiled_glass", "building", 2800, 0.80, 32));

        futures.add(save(cache, "white_stained_glass", "minecraft:white_stained_glass", "building", 2400, 0.75, 32));
        futures.add(save(
                cache, "light_gray_stained_glass", "minecraft:light_gray_stained_glass", "building", 2400, 0.75, 32));
        futures.add(save(cache, "gray_stained_glass", "minecraft:gray_stained_glass", "building", 2400, 0.75, 32));
        futures.add(save(cache, "black_stained_glass", "minecraft:black_stained_glass", "building", 2400, 0.75, 32));
        futures.add(save(cache, "red_stained_glass", "minecraft:red_stained_glass", "building", 2400, 0.75, 32));
        futures.add(save(cache, "orange_stained_glass", "minecraft:orange_stained_glass", "building", 2400, 0.75, 32));
        futures.add(save(cache, "yellow_stained_glass", "minecraft:yellow_stained_glass", "building", 2400, 0.75, 32));
        futures.add(save(cache, "lime_stained_glass", "minecraft:lime_stained_glass", "building", 2400, 0.75, 32));
        futures.add(save(cache, "green_stained_glass", "minecraft:green_stained_glass", "building", 2400, 0.75, 32));
        futures.add(save(cache, "cyan_stained_glass", "minecraft:cyan_stained_glass", "building", 2400, 0.75, 32));
        futures.add(save(
                cache, "light_blue_stained_glass", "minecraft:light_blue_stained_glass", "building", 2400, 0.75, 32));
        futures.add(save(cache, "blue_stained_glass", "minecraft:blue_stained_glass", "building", 2400, 0.75, 32));
        futures.add(save(cache, "purple_stained_glass", "minecraft:purple_stained_glass", "building", 2400, 0.75, 32));
        futures.add(
                save(cache, "magenta_stained_glass", "minecraft:magenta_stained_glass", "building", 2400, 0.75, 32));
        futures.add(save(cache, "pink_stained_glass", "minecraft:pink_stained_glass", "building", 2400, 0.75, 32));

        futures.add(save(cache, "white_terracotta", "minecraft:white_terracotta", "building", 2800, 0.80, 32));
        futures.add(
                save(cache, "light_gray_terracotta", "minecraft:light_gray_terracotta", "building", 2800, 0.80, 32));
        futures.add(save(cache, "gray_terracotta", "minecraft:gray_terracotta", "building", 2800, 0.80, 32));
        futures.add(save(cache, "black_terracotta", "minecraft:black_terracotta", "building", 2800, 0.80, 32));
        futures.add(save(cache, "red_terracotta", "minecraft:red_terracotta", "building", 2800, 0.80, 32));
        futures.add(save(cache, "orange_terracotta", "minecraft:orange_terracotta", "building", 2800, 0.80, 32));
        futures.add(save(cache, "yellow_terracotta", "minecraft:yellow_terracotta", "building", 2800, 0.80, 32));
        futures.add(save(cache, "lime_terracotta", "minecraft:lime_terracotta", "building", 2800, 0.80, 32));
        futures.add(save(cache, "green_terracotta", "minecraft:green_terracotta", "building", 2800, 0.80, 32));
        futures.add(save(cache, "cyan_terracotta", "minecraft:cyan_terracotta", "building", 2800, 0.80, 32));
        futures.add(
                save(cache, "light_blue_terracotta", "minecraft:light_blue_terracotta", "building", 2800, 0.80, 32));
        futures.add(save(cache, "blue_terracotta", "minecraft:blue_terracotta", "building", 2800, 0.80, 32));
        futures.add(save(cache, "purple_terracotta", "minecraft:purple_terracotta", "building", 2800, 0.80, 32));
        futures.add(save(cache, "magenta_terracotta", "minecraft:magenta_terracotta", "building", 2800, 0.80, 32));
        futures.add(save(cache, "pink_terracotta", "minecraft:pink_terracotta", "building", 2800, 0.80, 32));

        futures.add(save(cache, "metal_girder", "create:metal_girder", "building", 3360, 0.85, 64));
        futures.add(save(cache, "industrial_iron_block", "create:industrial_iron_block", "building", 4000, 0.85, 64));

        futures.add(save(cache, "industrial_iron_window", "create:industrial_iron_window", "building", 2800, 0.80, 32));
        futures.add(save(cache, "ornate_iron_window", "create:ornate_iron_window", "building", 2800, 0.80, 32));
        futures.add(save(cache, "weathered_iron_window", "create:weathered_iron_window", "building", 2800, 0.80, 32));

        /*
        Group: Metal
         */

        futures.add(save(cache, "iron_ingot", "minecraft:iron_ingot", "metal", 4800, 0.80, 32));
        futures.add(save(cache, "iron_sheet", "create:iron_sheet", "metal", 4480, 0.80, 16));

        futures.add(save(cache, "copper_ingot", "minecraft:copper_ingot", "metal", 4800, 0.80, 32));
        futures.add(save(cache, "copper_sheet", "create:copper_sheet", "metal", 4160, 0.80, 16));

        futures.add(save(cache, "gold_ingot", "minecraft:gold_ingot", "metal", 4000, 0.75, 32));
        futures.add(save(cache, "golden_sheet", "create:golden_sheet", "metal", 3200, 0.75, 16));

        futures.add(save(cache, "zinc_ingot", "create:zinc_ingot", "metal", 4320, 0.80, 32));

        futures.add(save(cache, "brass_ingot", "create:brass_ingot", "metal", 4000, 0.80, 32));
        futures.add(save(cache, "brass_sheet", "create:brass_sheet", "metal", 3600, 0.80, 16));

        futures.add(save(cache, "netherite_ingot", "minecraft:netherite_ingot", "metal", 1600, 0.70, 2));

        futures.add(save(cache, "andesite_alloy", "create:andesite_alloy", "metal", 4800, 0.85, 32));

        futures.add(save(cache, "sturdy_sheet", "create:sturdy_sheet", "metal", 2880, 0.70, 4));
        futures.add(save(cache, "rose_quartz", "create:rose_quartz", "metal", 3200, 0.8, 12));

        futures.add(save(cache, "iron_bars", "minecraft:iron_bars", "metal", 3200, 0.8, 32));
        futures.add(save(cache, "andesite_bars", "create:andesite_bars", "metal", 3200, 0.80, 32));
        futures.add(save(cache, "brass_bars", "create:brass_bars", "metal", 3200, 0.80, 32));
        futures.add(save(cache, "copper_bars", "create:copper_bars", "metal", 3200, 0.80, 32));

        /*
        Group: Mechanical
         */

        futures.add(save(cache, "andesite_casing", "create:andesite_casing", "mechanical", 4160, 0.75, 32));
        futures.add(save(cache, "copper_casing", "create:copper_casing", "mechanical", 3840, 0.7, 16));
        futures.add(save(cache, "brass_casing", "create:brass_casing", "mechanical", 3840, 0.7, 16));
        futures.add(save(cache, "railway_casing", "create:railway_casing", "mechanical", 3200, 0.7, 8));

        futures.add(save(cache, "shaft", "create:shaft", "mechanical", 4000, 0.9, 32));
        futures.add(save(cache, "cogwheel", "create:cogwheel", "mechanical", 4000, 0.85, 32));
        futures.add(save(cache, "large_cogwheel", "create:large_cogwheel", "mechanical", 3200, 0.85, 16));

        futures.add(save(cache, "gearbox", "create:gearbox", "mechanical", 4000, 0.85, 16));
        futures.add(save(cache, "gearshift", "create:gearshift", "mechanical", 3200, 0.80, 16));
        futures.add(save(cache, "clutch", "create:clutch", "mechanical", 3200, 0.80, 16));
        futures.add(save(cache, "mechanical_bearing", "create:mechanical_bearing", "mechanical", 2400, 0.75, 8));

        futures.add(save(cache, "speedometer", "create:speedometer", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "stressometer", "create:stressometer", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "water_wheel", "create:water_wheel", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "large_water_wheel", "create:large_water_wheel", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "windmill_bearing", "create:windmill_bearing", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "steam_engine", "create:steam_engine", "mechanical", 2400, 0.75, 8));
        futures.add(save(
                cache, "rotation_speed_controller", "create:rotation_speed_controller", "mechanical", 2400, 0.75, 8));

        futures.add(save(cache, "andesite_funnel", "create:andesite_funnel", "metal", 2400, 0.75, 16));
        futures.add(save(cache, "andesite_tunnel", "create:andesite_tunnel", "metal", 2400, 0.75, 16));
        futures.add(save(cache, "brass_funnel", "create:brass_funnel", "metal", 2400, 0.75, 16));
        futures.add(save(cache, "brass_tunnel", "create:brass_tunnel", "metal", 2400, 0.75, 16));
        futures.add(save(cache, "belt_connector", "create:belt_connector", "mechanical", 3200, 0.8, 12));
        futures.add(save(cache, "hopper", "minecraft:hopper", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "chute", "create:chute", "mechanical", 3200, 0.80, 16));
        futures.add(save(cache, "chain_conveyor", "create:chain_conveyor", "mechanical", 1600, 0.70, 4));
        futures.add(save(cache, "cardboard", "create:cardboard", "mechanical", 3200, 0.80, 32));

        futures.add(save(cache, "item_vault", "create:item_vault", "mechanical", 1600, 0.70, 4));
        futures.add(save(cache, "fluid_tank", "create:fluid_tank", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "fluid_pipe", "create:fluid_pipe", "mechanical", 3200, 0.80, 16));
        futures.add(save(cache, "mechanical_pump", "create:mechanical_pump", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "factory_gauge", "create:factory_gauge", "mechanical", 3040, 0.75, 8));

        futures.add(save(cache, "basin", "create:basin", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "depot", "create:depot", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "item_drain", "create:item_drain", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "mechanical_mixer", "create:mechanical_mixer", "mechanical", 2000, 0.70, 4));
        futures.add(save(cache, "mechanical_press", "create:mechanical_press", "mechanical", 2000, 0.70, 4));
        futures.add(save(cache, "spout", "create:spout", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "millstone", "create:millstone", "mechanical", 2400, 0.75, 8));

        futures.add(save(cache, "mechanical_drill", "create:mechanical_drill", "mechanical", 2000, 0.70, 4));
        futures.add(save(cache, "mechanical_plough", "create:mechanical_plough", "mechanical", 2000, 0.70, 4));
        futures.add(save(cache, "mechanical_saw", "create:mechanical_saw", "mechanical", 2000, 0.70, 4));
        futures.add(save(cache, "mechanical_piston", "create:mechanical_piston", "mechanical", 2000, 0.70, 4));
        futures.add(save(
                cache, "sticky_mechanical_piston", "create:sticky_mechanical_piston", "mechanical", 2000, 0.70, 4));
        futures.add(save(cache, "encased_fan", "create:encased_fan", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "mechanical_harvester", "create:mechanical_harvester", "mechanical", 2000, 0.70, 4));

        futures.add(save(cache, "deployer", "create:deployer", "mechanical", 1600, 0.70, 4));
        futures.add(save(cache, "mechanical_crafter", "create:mechanical_crafter", "mechanical", 1600, 0.70, 4));
        futures.add(save(cache, "crushing_wheel", "create:crushing_wheel", "mechanical", 2400, 0.75, 8));

        futures.add(save(cache, "packager", "create:packager", "mechanical", 1600, 0.70, 4));
        futures.add(save(cache, "repackager", "create:repackager", "mechanical", 1600, 0.70, 4));
        futures.add(save(cache, "package_frogport", "create:package_frogport", "mechanical", 1600, 0.70, 4));

        futures.add(save(cache, "stock_link", "create:stock_link", "mechanical", 1600, 0.70, 4));
        futures.add(save(cache, "stock_ticker", "create:stock_ticker", "mechanical", 1600, 0.70, 4));
        futures.add(save(cache, "redstone_requester", "create:redstone_requester", "mechanical", 1600, 0.70, 4));

        futures.add(save(cache, "track", "create:track", "mechanical", 3200, 0.80, 32));
        futures.add(save(cache, "track_signal", "create:track_signal", "mechanical", 1600, 0.70, 4));
        futures.add(save(cache, "track_station", "create:track_station", "mechanical", 1600, 0.70, 4));
        futures.add(save(cache, "rail", "minecraft:rail", "mechanical", 3200, 0.80, 32));
        futures.add(save(cache, "powered_rail", "minecraft:powered_rail", "mechanical", 3200, 0.80, 32));

        futures.add(save(cache, "piston", "minecraft:piston", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "sticky_piston", "minecraft:sticky_piston", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "observer", "minecraft:observer", "mechanical", 2400, 0.75, 8));
        futures.add(save(cache, "repeater", "minecraft:repeater", "mechanical", 2400, 0.75, 16));
        futures.add(save(cache, "comparator", "minecraft:comparator", "mechanical", 2400, 0.75, 16));
        futures.add(save(cache, "redstone_lamp", "minecraft:redstone_lamp", "mechanical", 2400, 0.75, 16));

        futures.add(save(cache, "electron_tube", "create:electron_tube", "mechanical", 2720, 0.75, 8));
        futures.add(save(cache, "nixie_tube", "create:nixie_tube", "mechanical", 3040, 0.75, 12));
        futures.add(save(cache, "precision_mechanism", "create:precision_mechanism", "mechanical", 3040, 0.75, 4));

        futures.add(save(cache, "mechanical_arm", "create:mechanical_arm", "mechanical", 1600, 0.70, 4));
        futures.add(save(cache, "clockwork_bearing", "create:clockwork_bearing", "mechanical", 1300, 0.75, 8));

        /*
        Group: Food
         */

        futures.add(save(cache, "wheat_flour", "create:wheat_flour", "food", 4800, 0.8, 48));
        futures.add(save(cache, "dough", "create:dough", "food", 4800, 0.80, 48));
        futures.add(save(cache, "bread", "minecraft:bread", "food", 5600, 0.9, 32));
        futures.add(save(cache, "baked_potato", "minecraft:baked_potato", "food", 4000, 0.85, 32));

        futures.add(save(cache, "cooked_beef", "minecraft:cooked_beef", "food", 4000, 0.85, 32));
        futures.add(save(cache, "cooked_porkchop", "minecraft:cooked_porkchop", "food", 4000, 0.85, 32));
        futures.add(save(cache, "cooked_mutton", "minecraft:cooked_mutton", "food", 4000, 0.85, 32));
        futures.add(save(cache, "cooked_chicken", "minecraft:cooked_chicken", "food", 4000, 0.85, 32));
        futures.add(save(cache, "cooked_rabbit", "minecraft:cooked_rabbit", "food", 4000, 0.85, 32));
        futures.add(save(cache, "cooked_cod", "minecraft:cooked_cod", "food", 4000, 0.85, 32));
        futures.add(save(cache, "cooked_salmon", "minecraft:cooked_salmon", "food", 4000, 0.85, 32));

        /*
        Group: Luxury
         */

        futures.add(save(cache, "white_dye", "minecraft:white_dye", "luxury", 4800, 0.75, 16));
        futures.add(save(cache, "light_gray_dye", "minecraft:light_gray_dye", "luxury", 4800, 0.75, 16));
        futures.add(save(cache, "gray_dye", "minecraft:gray_dye", "luxury", 4800, 0.75, 16));
        futures.add(save(cache, "black_dye", "minecraft:black_dye", "luxury", 4800, 0.75, 16));
        futures.add(save(cache, "brown_dye", "minecraft:brown_dye", "luxury", 4800, 0.75, 16));
        futures.add(save(cache, "red_dye", "minecraft:red_dye", "luxury", 4800, 0.75, 16));
        futures.add(save(cache, "orange_dye", "minecraft:orange_dye", "luxury", 4800, 0.75, 16));
        futures.add(save(cache, "yellow_dye", "minecraft:yellow_dye", "luxury", 4800, 0.75, 16));
        futures.add(save(cache, "lime_dye", "minecraft:lime_dye", "luxury", 4800, 0.75, 16));
        futures.add(save(cache, "green_dye", "minecraft:green_dye", "luxury", 4800, 0.75, 16));
        futures.add(save(cache, "cyan_dye", "minecraft:cyan_dye", "luxury", 4800, 0.75, 16));
        futures.add(save(cache, "light_blue_dye", "minecraft:light_blue_dye", "luxury", 4800, 0.75, 16));
        futures.add(save(cache, "blue_dye", "minecraft:blue_dye", "luxury", 4800, 0.75, 16));
        futures.add(save(cache, "purple_dye", "minecraft:purple_dye", "luxury", 4800, 0.75, 16));
        futures.add(save(cache, "magenta_dye", "minecraft:magenta_dye", "luxury", 4800, 0.75, 16));

        futures.add(save(cache, "white_wool", "minecraft:white_wool", "luxury", 3200, 0.75, 32));
        futures.add(save(cache, "light_gray_wool", "minecraft:light_gray_wool", "luxury", 3200, 0.75, 32));
        futures.add(save(cache, "gray_wool", "minecraft:gray_wool", "luxury", 3200, 0.75, 32));
        futures.add(save(cache, "black_wool", "minecraft:black_wool", "luxury", 3200, 0.75, 32));
        futures.add(save(cache, "brown_wool", "minecraft:brown_wool", "luxury", 3200, 0.75, 32));
        futures.add(save(cache, "red_wool", "minecraft:red_wool", "luxury", 3200, 0.75, 32));
        futures.add(save(cache, "orange_wool", "minecraft:orange_wool", "luxury", 3200, 0.75, 32));
        futures.add(save(cache, "yellow_wool", "minecraft:yellow_wool", "luxury", 3200, 0.75, 32));
        futures.add(save(cache, "lime_wool", "minecraft:lime_wool", "luxury", 3200, 0.75, 32));
        futures.add(save(cache, "green_wool", "minecraft:green_wool", "luxury", 3200, 0.75, 32));
        futures.add(save(cache, "cyan_wool", "minecraft:cyan_wool", "luxury", 3200, 0.75, 32));
        futures.add(save(cache, "light_blue_wool", "minecraft:light_blue_wool", "luxury", 3200, 0.75, 32));
        futures.add(save(cache, "blue_wool", "minecraft:blue_wool", "luxury", 3200, 0.75, 32));
        futures.add(save(cache, "purple_wool", "minecraft:purple_wool", "luxury", 3200, 0.75, 32));
        futures.add(save(cache, "magenta_wool", "minecraft:magenta_wool", "luxury", 3200, 0.75, 32));
        futures.add(save(cache, "pink_wool", "minecraft:pink_wool", "luxury", 3200, 0.75, 32));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(
            CachedOutput cache,
            String fileName,
            String item,
            String group,
            double baseDemand,
            double targetUtilisation,
            int packageSize) {
        JsonObject json = new JsonObject();
        json.addProperty("item", item);
        json.addProperty("group", group);
        json.addProperty("base_demand", baseDemand);
        json.addProperty("target_utilisation", targetUtilisation);
        json.addProperty("package_size", packageSize);

        ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, fileName);
        return DataProvider.saveStable(cache, json, pathProvider.json(fileId));
    }

    @Override
    public String getName() {
        return "Kontor Market Definitions";
    }
}
