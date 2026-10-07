package com.timder.kontor.data.provider;

import com.google.gson.JsonObject;
import com.timder.kontor.CreateKontor;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ProcessCostDataProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;

    public ProcessCostDataProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "kontor/process_costs");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Map<String, Double> entries = new LinkedHashMap<>();
        entries.put("minecraft:crafting", 5.00);
        entries.put("minecraft:smelting", 7.50);
        entries.put("minecraft:blasting", 8.20);
        entries.put("minecraft:smoking", 7.90);
        entries.put("minecraft:campfire_cooking", 6.60);
        entries.put("minecraft:stonecutting", 3.50);
        entries.put("create:pressing", 4.25);
        entries.put("create:crushing", 6.50);
        entries.put("create:milling", 5.50);
        entries.put("create:mixing", 4.00);
        entries.put("create:cutting", 4.25);
        entries.put("create:deploying", 5.50);
        entries.put("create:mechanical_crafting", 5.00);
        entries.put("create:splashing", 7.00);
        entries.put("create:haunting", 8.00);
        entries.put("create:filling", 6.50);
        entries.put("create:emptying", 3.50);
        entries.put("create:sandpaper_polishing", 5.25);

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<String, Double> entry : entries.entrySet()) {
            futures.add(save(cache, entry.getKey(), entry.getValue()));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(CachedOutput cache, String recipeTypeId, double cost) {
        JsonObject json = new JsonObject();
        json.addProperty("id", recipeTypeId);
        json.addProperty("cost", cost);

        ResourceLocation type = ResourceLocation.parse(recipeTypeId);
        ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, type.getNamespace() + "_" + type.getPath());
        return DataProvider.saveStable(cache, json, pathProvider.json(fileId));
    }

    @Override
    public String getName() {
        return "Kontor Process Costs";
    }
}
