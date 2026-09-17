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

        futures.add(save(cache, "iron_sheet", "create:iron_sheet", "metal", 1800, 0.8));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(CachedOutput cache,
                                      String fileName,
                                      String item,
                                      String group,
                                      double baseDemand,
                                      double targetUtilisation
    ) {
        JsonObject json = new JsonObject();
        json.addProperty("item", item);
        json.addProperty("group", group);
        json.addProperty("base_demand", baseDemand);
        json.addProperty("target_utilisation", targetUtilisation);

        ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, fileName);
        return DataProvider.saveStable(cache, json, pathProvider.json(fileId));
    }

    @Override
    public String getName() {
        return "Kontor Market Definitions";
    }
}
