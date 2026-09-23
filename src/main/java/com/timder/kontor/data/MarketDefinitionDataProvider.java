package com.timder.kontor.data;

import com.google.gson.JsonObject;
import com.timder.kontor.CreateKontor;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

        /*
        Group: Building
         */
        futures.add(save(cache, "brick", "minecraft:brick", "building", 1500, 0.80, 64));
        futures.add(save(cache, "glass", "minecraft:glass", "building", 1600, 0.80, 64));

        futures.add(save(cache, "oak_planks", "minecraft:oak_planks", "building", 2800, 0.85, 64));
        futures.add(save(cache, "oak_stairs", "minecraft:oak_stairs", "building", 1800, 0.80, 64));
        futures.add(save(cache, "oak_slab", "minecraft:oak_slab", "building", 1800, 0.80, 64));

        futures.add(save(cache, "spruce_planks", "minecraft:spruce_planks", "building", 2800, 0.85, 64));
        futures.add(save(cache, "spruce_stairs", "minecraft:spruce_stairs", "building", 1800, 0.80, 64));
        futures.add(save(cache, "spruce_slab", "minecraft:spruce_slab", "building", 1800, 0.80, 64));

        futures.add(save(cache, "birch_planks", "minecraft:birch_planks", "building", 2800, 0.85, 64));
        futures.add(save(cache, "birch_stairs", "minecraft:birch_stairs", "building", 1800, 0.80, 64));
        futures.add(save(cache, "birch_slab", "minecraft:birch_slab", "building", 1800, 0.80, 64));

        futures.add(save(cache, "jungle_planks", "minecraft:jungle_planks", "building", 2800, 0.85, 64));
        futures.add(save(cache, "jungle_stairs", "minecraft:jungle_stairs", "building", 1800, 0.80, 64));
        futures.add(save(cache, "birch_slab", "minecraft:birch_slab", "building", 1800, 0.80, 64));

        futures.add(save(cache, "acacia_planks", "minecraft:acacia_planks", "building", 2800, 0.85, 64));
        futures.add(save(cache, "acacia_stairs", "minecraft:acacia_stairs", "building", 1800, 0.80, 64));
        futures.add(save(cache, "acacia_slab", "minecraft:acacia_slab", "building", 1800, 0.80, 64));

        futures.add(save(cache, "dark_oak_planks", "minecraft:dark_oak_planks", "building", 2800, 0.85, 64));
        futures.add(save(cache, "dark_oak_stairs", "minecraft:dark_oak_stairs", "building", 1800, 0.80, 64));
        futures.add(save(cache, "dark_oak_slab", "minecraft:dark_oak_slab", "building", 1800, 0.80, 64));

        futures.add(save(cache, "mangrove_planks", "minecraft:mangrove_planks", "building", 2800, 0.85, 64));
        futures.add(save(cache, "mangrove_stairs", "minecraft:mangrove_stairs", "building", 1800, 0.80, 64));
        futures.add(save(cache, "mangrove_slab", "minecraft:mangrove_slab", "building", 1800, 0.80, 64));

        futures.add(save(cache, "cherry_planks", "minecraft:cherry_planks", "building", 2800, 0.85, 64));
        futures.add(save(cache, "cherry_stairs", "minecraft:cherry_stairs", "building", 1800, 0.80, 64));
        futures.add(save(cache, "cherry_slab", "minecraft:cherry_slab", "building", 1800, 0.80, 64));

        futures.add(save(cache, "crimson_planks", "minecraft:crimson_planks", "building", 2800, 0.85, 64));
        futures.add(save(cache, "crimson_stairs", "minecraft:crimson_stairs", "building", 1800, 0.80, 64));
        futures.add(save(cache, "crimson_slab", "minecraft:crimson_slab", "building", 1800, 0.80, 64));

        futures.add(save(cache, "warped_planks", "minecraft:warped_planks", "building", 2800, 0.85, 64));
        futures.add(save(cache, "warped_stairs", "minecraft:warped_stairs", "building", 1800, 0.80, 64));
        futures.add(save(cache, "warped_slab", "minecraft:warped_slab", "building", 1800, 0.80, 64));

        futures.add(save(cache, "polished_andesite", "minecraft:polished_andesite", "building", 1000, 0.80, 64));
        futures.add(save(cache, "polished_andesite_stairs", "minecraft:polished_andesite", "building", 800, 0.80, 64));
        futures.add(save(cache, "polished_andesite_slabs", "minecraft:polished_andesite", "building", 800, 0.80, 64));
        futures.add(save(cache, "polished_andesite_wall", "minecraft:polished_andesite", "building", 800, 0.80, 64));

        futures.add(save(cache, "andesite_stairs", "minecraft:andesite_stairs", "building", 1400, 0.80, 64));
        futures.add(save(cache, "andesite_slabs", "minecraft:andesite_slabs", "building", 1400, 0.80, 64));
        futures.add(save(cache, "andesite_wall", "minecraft:andesite_wall", "building", 1400, 0.80, 64));

        futures.add(save(cache, "cobblestone_stairs", "minecraft:cobblestone_stairs", "building", 1000, 0.80, 64));
        futures.add(save(cache, "cobblestone_slab", "minecraft:cobblestone_stairs", "building", 1000, 0.80, 64));
        futures.add(save(cache, "cobblestone_wall", "minecraft:cobblestone_stairs", "building", 800, 0.80, 64));

        futures.add(save(cache, "stone_bricks", "minecraft:stone_bricks", "building", 2000, 0.85, 64));
        futures.add(save(cache, "stone_brick_stairs", "minecraft:stone_bricks_stairs", "building", 1800, 0.85, 64));
        futures.add(save(cache, "stone_brick_slabs", "minecraft:stone_bricks_slabs", "building", 1800, 0.85, 64));
        futures.add(save(cache, "stone_brick_wall", "minecraft:stone_bricks_wall", "building", 1800, 0.85, 64));

        /*
        Group: Metal
         */
        futures.add(save(cache, "iron_ingot", "minecraft:iron_ingot", "metal", 2200, 0.80, 32));
        futures.add(save(cache, "iron_sheet", "create:iron_sheet", "metal", 1800, 0.80, 16));

        futures.add(save(cache, "copper_ingot", "minecraft:copper_ingot", "metal", 1600, 0.80, 32));
        futures.add(save(cache, "copper_sheet", "create:copper_sheet", "metal", 900, 0.80, 16));

        futures.add(save(cache, "gold_ingot", "minecraft:gold_ingot", "metal", 700, 0.75, 32));
        futures.add(save(cache, "golden_sheet", "create:golden_sheet", "metal", 500, 0.75, 16));

        futures.add(save(cache, "zinc_ingot", "create:zinc_ingot", "metal", 1400, 0.80, 32));

        futures.add(save(cache, "brass_ingot", "create:brass_ingot", "metal", 1200, 0.80, 32));
        futures.add(save(cache, "brass_sheet", "create:brass_sheet", "metal", 700, 0.80, 16));

        futures.add(save(cache, "sturdy_sheet", "create:sturdy_sheet", "metal", 900, 0.70, 4));

        futures.add(save(cache, "andesite_alloy", "create:andesite_alloy", "metal", 2500, 0.85, 32));
        futures.add(save(cache, "netherite_ingot", "minecraft:netherite_ingot", "metal", 100, 0.70, 2));

        futures.add(save(cache, "rose_quartz", "create:rose_quartz", "metal", 700, 0.8, 12));

        /*
        Group: Mechanical
         */
        futures.add(save(cache, "andesite_casing", "create:andesite_casing", "mechanical", 1900, 0.75, 32));
        futures.add(save(cache, "brass_casing", "create:brass_casing", "mechanical", 1200, 0.7, 16));
        futures.add(save(cache, "copper_casing", "create:copper_casing", "mechanical", 1400, 0.7, 16));
        futures.add(save(cache, "railway_casing", "create:railway_casing", "mechanical", 750, 0.7, 8));

        futures.add(save(cache, "shaft", "create:shaft", "mechanical", 2500, 0.9, 32));
        futures.add(save(cache, "gearbox", "create:gearbox", "mechanical", 2000, 0.85, 16));
        futures.add(save(cache, "cogwheel", "create:cogwheel", "mechanical", 2000, 0.85, 32));
        futures.add(save(cache, "large_cogwheel", "create:large_cogwheel", "mechanical", 1200, 0.85, 16));
        futures.add(save(cache, "belt_connector", "create:belt_connector", "mechanical", 1000, 0.8, 12));

        futures.add(save(cache, "precision_mechanism", "create:precision_mechanism", "mechanical", 250, 0.75, 4));
        futures.add(save(cache, "electron_tube", "create:electron_tube", "mechanical", 250, 0.75, 8));
        futures.add(save(cache, "nixie_tube", "create:nixie_tube", "mechanical", 250, 0.75, 12));
        futures.add(save(cache, "factory_gauge", "create:factory_gauge", "mechanical", 300, 0.75, 8));

        /*
        Group: Food
         */
        futures.add(save(cache, "bread", "minecraft:bread", "food", 3500, 0.9, 32));
        futures.add(save(cache, "wheat_flour", "create:wheat_flour", "food", 3000, 0.8, 48));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(CachedOutput cache,
                                      String fileName,
                                      String item,
                                      String group,
                                      double baseDemand,
                                      double targetUtilisation,
                                      int packageSize
    ) {
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
