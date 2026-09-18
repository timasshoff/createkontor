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

public class GroupDefDataProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;

    public GroupDefDataProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "kontor/group_definitions");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(save(cache, "basic_goods", "basic_goods", 4.0, 0.3, 0.1, 0.12, 0.06, 0.3));
        futures.add(save(cache, "building", "building", 3.0, 0.5, 0.12, 0.1, 0.06, 1.2));
        futures.add(save(cache, "metal", "metal", 3.5, 0.5, 0.12, 0.1, 0.05, 0.8));
        futures.add(save(cache, "mechanical", "mechanical", 2.5, 1.0, 0.15, 0.8, 0.04, 1.0));
        futures.add(save(cache, "food", "food", 2.0, 0.8, 0.11, 0.1, 0.05, 0.2));
        futures.add(save(cache, "luxury", "luxury", 1.5, 0.9, 0.3, 0.08, 0.04, 1.3));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(CachedOutput cache,
                                      String fileName,
                                      String id,
                                      double priceSensitivity,
                                      double reputationWeight,
                                      double targetMargin,
                                      double priceResponse,
                                      double capacityResponse,
                                      double cycleSensitivity
                                      ) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("price_sensitivity", priceSensitivity);
        json.addProperty("reputation_weight", reputationWeight);
        json.addProperty("target_margin", targetMargin);
        json.addProperty("price_response", priceResponse);
        json.addProperty("capacity_response", capacityResponse);
        json.addProperty("cycle_sensitivity", cycleSensitivity);

        ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, fileName);
        return DataProvider.saveStable(cache, json, pathProvider.json(fileId));
    }

    @Override
    public String getName() {
        return "Kontor Group Definitions";
    }
}
