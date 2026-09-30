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

        futures.add(save(cache, "iron_sheet", "iron_sheet", List.of(
                "create:iron_sheet"
        ), 1.0, 1.0, 0.0, 1));

        futures.add(save(cache, "iron_sheet_turnover", "iron_sheet_turnover", List.of(
                "create:iron_sheet"
        ), 1.0, 0.1, 0.05, 1));

        futures.add(save(cache, "brass_sheet", "brass_sheet", List.of(
                "create:brass_sheet"
        ), 1.0, 1.0, 0.0, 1));

        futures.add(save(cache, "brass_sheet_turnover", "brass_sheet_turnover", List.of(
                "create:brass_sheet"
        ), 1.0, 0.1, 0.05, 1));

        futures.add(save(cache, "cogwheel", "cogwheel", List.of(
                "create:cogwheel"
        ), 1.0, 1.0, 0.0, 1));

        futures.add(save(cache, "cogwheel_turnover", "cogwheel_turnover", List.of(
                "create:cogwheel"
        ), 1.0, 0.1, 0.05, 1));

        futures.add(save(cache, "precision_mechanism", "precision_mechanism", List.of(
                "create:precision_mechanism"
        ), 1.0, 1.0, 0.0, 1));

        futures.add(save(cache, "precision_mechanism_turnover", "precision_mechanism_turnover", List.of(
                "create:precision_mechanism"
        ), 1.0, 0.1, 0.05, 1));

        futures.add(save(cache, "sheet_metal_bundle", "sheet_metal_bundle", List.of(
                "create:iron_sheet",
                "create:brass_sheet"
        ), 0.8, 1.0, 0.0, 2));

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
