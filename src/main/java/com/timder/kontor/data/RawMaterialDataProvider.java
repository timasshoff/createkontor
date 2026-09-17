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

        futures.add(save(cache, "raw_iron", "minecraft:raw_iron", 6.20, 0.025, 3000.0));

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
