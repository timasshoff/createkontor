package com.timder.kontor.data.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.timder.kontor.CreateKontor;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

        futures.add(save(cache, "oak_bundle", "oak_bundle", List.of(
                "oak_planks",
                "oak_stairs",
                "oak_slab"
        ), 0.8, 1.0, 0.0, 1));

        futures.add(save(cache, "spruce_bundle", "spruce_bundle", List.of(
                "spruce_planks",
                "spruce_stairs",
                "spruce_slab"
        ), 0.8, 1.0, 0.0, 1));

        futures.add(save(cache, "birch_bundle", "birch_bundle", List.of(
                "birch_planks",
                "birch_stairs",
                "birch_slab"
        ), 0.8, 1.0, 0.0, 1));

        futures.add(save(cache, "jungle_bundle", "jungle_bundle", List.of(
                "jungle_planks",
                "jungle_stairs",
                "jungle_slab"
        ), 0.8, 1.0, 0.0, 1));

        futures.add(save(cache, "acacia_bundle", "acacia_bundle", List.of(
                "acacia_planks",
                "acacia_stairs",
                "acacia_slab"
        ), 0.8, 1.0, 0.0, 1));

        futures.add(save(cache, "dark_oak_bundle", "dark_oak_bundle", List.of(
                "dark_oak_planks",
                "dark_oak_stairs",
                "dark_oak_slab"
        ), 0.8, 1.0, 0.0, 1));

        futures.add(save(cache, "mangrove_bundle", "mangrove_bundle", List.of(
                "mangrove_planks",
                "mangrove_stairs",
                "mangrove_slab"
        ), 0.8, 1.0, 0.0, 1));

        futures.add(save(cache, "cherry_bundle", "cherry_bundle", List.of(
                "cherry_planks",
                "cherry_stairs",
                "cherry_slab"
        ), 0.8, 1.0, 0.0, 1));

        futures.add(save(cache, "nether_wood_bundle", "nether_wood_bundle", List.of(
                "crimson_planks",
                "crimson_stairs",
                "crimson_slab",
                "warped_planks",
                "warped_stairs",
                "warped_slab"
        ), 0.8, 1.0, 0.0, 1));

        futures.add(save(cache, "nether_wood_bundle_turnover", "nether_wood_bundle_turnover", List.of(
                "crimson_planks",
                "crimson_stairs",
                "crimson_slab",
                "warped_planks",
                "warped_stairs",
                "warped_slab"
        ), 0.8, 0.1, 0.06, 1));

        futures.add(save(cache, "cobblestone_bundle", "cobblestone_bundle", List.of(
                "cobblestone_stairs",
                "cobblestone_slab",
                "cobblestone_wall"
        ), 0.8, 1.0, 0.0, 1));

        futures.add(save(cache, "stone_bundle", "stone_bundle", List.of(
                "stone_stairs",
                "stone_slab",
                "stone_wall",
                "stone_bricks",
                "stone_brick_stairs",
                "stone_brick_slab",
                "stone_brick_wall"
        ), 0.8, 0.1, 0.05, 1));

        futures.add(save(cache, "small_andesite_bundle", "small_andesite_bundle", List.of(
                "andesite_stairs",
                "andesite_slab",
                "andesite_wall"
        ), 0.8, 1.0, 0.0, 1));

        futures.add(save(cache, "large_andesite_bundle", "large_andesite_bundle", List.of(
                "andesite_stairs",
                "andesite_slab",
                "andesite_wall",
                "polished_andesite",
                "polished_andesite_stairs",
                "polished_andesite_slab",
                "polished_andesite_wall",
                "create:andesite_alloy"
        ), 0.8, 0.15, 0.05, 1));

        futures.add(save(cache, "basic_mechanical_bundle", "basic_mechanical_bundle", List.of(
                "create:andesite_alloy",
                "create:shaft",
                "create:cogwheel"
        ), 0.8, 1.0, 0.0, 2));

        futures.add(save(cache, "metal_bundle_1", "metal_bundle_1", List.of(
                "iron_ingot",
                "copper_ingot",
                "create:zinc_ingot",
                "create:brass_ingot"
        ), 0.8, 1.0, 0.0, 2));

        futures.add(save(cache, "metal_bundle_2", "metal_bundle_2", List.of(
                "netherite_ingot"
        ), 0.8, 0.15, 0.125, 3));

        futures.add(save(cache, "sheet_metal_bundle", "sheet_metal_bundle", List.of(
                "create:iron_sheet",
                "create:brass_sheet",
                "create:copper_sheet"
        ), 0.8, 1.0, 0.0, 2));


        futures.add(save(cache, "casing_bundle", "casing_bundle", List.of(
                "create:andesite_casing",
                "create:copper_casing",
                "create:brass_casing"
        ), 0.8, 1.0, 0.0, 3));

        futures.add(save(cache, "dye_bundle_1", "dye_bundle_1", List.of(
                "orange_dye",
                "yellow_dye",
                "white_dye",
                "gray_dye",
                "blue_dye",
                "green_dye",
                "red_dye",
                "black_dye"
        ), 0.8, 1.0, 0.0, 2));

        futures.add(save(cache, "dye_bundle_2", "dye_bundle_2", List.of(
                "magenta_dye",
                "light_blue_dye",
                "lime_dye",
                "light_gray_dye",
                "cyan_dye",
                "purple_dye",
                "brown_dye"
        ), 0.9, 0.15, 0.08, 2));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(CachedOutput cache,
                                      String fileName,
                                      String id,
                                      List<String> markets,
                                      double feeFactor,
                                      double dailyFraction,
                                      double revenueShare,
                                      int minLegalLevel
    ) {
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
