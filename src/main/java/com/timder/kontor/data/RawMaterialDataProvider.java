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

public class RawMaterialDataProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;

    public RawMaterialDataProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "kontor/raw_materials");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        /*
        Seeds & Food-related
         */
        futures.add(save(cache, "wheat", "minecraft:wheat", 1.0, 0.02, 6000.0));

        /*
        Gems & Ores
         */
        futures.add(save(cache, "coal", "minecraft:coal", 2.0, 0.02, 7000.0));
        futures.add(save(cache, "raw_copper", "minecraft:raw_copper", 4.5, 0.025, 3500.0));
        futures.add(save(cache, "raw_iron", "minecraft:raw_iron", 6.2, 0.025, 3000.0));
        futures.add(save(cache, "raw_zinc", "create:raw_zinc", 5.5, 0.03, 2800.0));
        futures.add(save(cache, "redstone", "minecraft:redstone", 3.0, 0.03, 4000.0));
        futures.add(save(cache, "quartz", "minecraft:quartz", 3.5, 0.03, 3500.0));
        futures.add(save(cache, "raw_gold", "minecraft:raw_gold", 9.5, 0.035, 1800));
        futures.add(save(cache, "lapis_lazuli", "minecraft:lapis_lazuli", 7.0, 0.035, 2200));
        futures.add(save(cache, "emerald", "minecraft:emerald", 24.0, 0.045, 400));
        futures.add(save(cache, "diamond", "minecraft:diamond", 28.0, 0.045, 200));
        futures.add(save(cache, "ancient_debris", "minecraft:ancient_debris", 180.0, 0.06, 100));

        /*
        Wood
         */
        futures.add(save(cache, "oak_log", "minecraft:oak_log", 1.0, 0.01, 7000.0));
        futures.add(save(cache, "spruce_log", "minecraft:spruce_log", 1.0, 0.01, 7000.0));
        futures.add(save(cache, "birch_log", "minecraft:birch_log", 1.0, 0.01, 7000.0));
        futures.add(save(cache, "jungle_log", "minecraft:jungle_log", 1.0, 0.01, 7000.0));
        futures.add(save(cache, "acacia_log", "minecraft:acacia_log", 1.0, 0.01, 7000.0));
        futures.add(save(cache, "dark_oak_log", "minecraft:dark_oak_log", 1.0, 0.01, 7000.0));
        futures.add(save(cache, "mangrove_log", "minecraft:mangrove_log", 1.5, 0.02, 4000.0));
        futures.add(save(cache, "cherry_log", "minecraft:cherry_log", 1.0, 0.01, 7000.0));
        futures.add(save(cache, "crimsom_stem", "minecraft:crimsom_stem", 2.5, 0.035, 3000.0));
        futures.add(save(cache, "warped_stem", "minecraft:warped_stem", 2.5, 0.035, 3000.0));

        /*
        Misc
         */
        futures.add(save(cache, "cobblestone", "minecraft:cobblestone", 0.7, 0.01, 9000.0));
        futures.add(save(cache, "dirt", "minecraft:dirt", 0.6, 0.01, 9000.0));
        futures.add(save(cache, "sand", "minecraft:sand", 0.6, 0.01, 9000.0));
        futures.add(save(cache, "gravel", "minecraft:gravel", 0.7, 0.01, 9000.0));
        futures.add(save(cache, "clay_ball", "minecraft:clay_ball", 0.9, 0.015, 6000.0));
        futures.add(save(cache, "flint", "minecraft:flint", 1.2, 0.02, 5000.0));
        futures.add(save(cache, "andesite", "minecraft:andesite", 1.2, 0.02, 5000.0));
        futures.add(save(cache, "obsidian", "minecraft:obsidian", 9.0, 0.035, 1500.0));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(CachedOutput cache, String fileName, String item, double baseValue, double dailyVolatility, double referenceVolume) {
        JsonObject json = new JsonObject();
        json.addProperty("item", item);
        json.addProperty("base_value", baseValue);
        json.addProperty("daily_volatility", dailyVolatility);
        json.addProperty("reference_volume", referenceVolume);

        ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, fileName);
        return DataProvider.saveStable(cache, json, pathProvider.json(fileId));
    }

    @Override
    public String getName() {
        return "Kontor Raw Materials";
    }
}
