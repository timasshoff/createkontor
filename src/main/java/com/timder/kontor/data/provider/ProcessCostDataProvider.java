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

        entries.put("minecraft:crafting", 2.50);
        entries.put("minecraft:smelting", 3.75);
        entries.put("minecraft:blasting", 4.10);
        entries.put("minecraft:smoking", 3.95);
        entries.put("minecraft:campfire_cooking", 3.30);
        entries.put("minecraft:stonecutting", 1.75);
        entries.put("minecraft:smithing", 1.75);
        entries.put("create:pressing", 2.10);
        entries.put("create:crushing", 3.25);
        entries.put("create:milling", 2.75);
        entries.put("create:mixing", 2.00);
        entries.put("create:cutting", 2.10);
        entries.put("create:deploying", 2.75);
        entries.put("create:mechanical_crafting", 2.50);
        entries.put("create:splashing", 3.50);
        entries.put("create:haunting", 4.00);
        entries.put("create:filling", 3.25);
        entries.put("create:emptying", 1.75);
        entries.put("create:sandpaper_polishing", 2.60);
        entries.put("create:compacting", 2.40);
        entries.put("create:item_application", 3.25);
        entries.put("create:sequenced_assembly", 5.50);

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
