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

public class BankLoanOfferDefDataProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;

    public BankLoanOfferDefDataProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "kontor/bank_loan_offers");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(save(cache, "standard", 28, 0, 0.00_02));
        futures.add(save(cache, "express", 7, 0, 0.00_00));
        futures.add(save(cache, "bridging", 3, 14, 0.00_20));
        futures.add(save(cache, "short_investment", 56, 0, 0.00_04));
        futures.add(save(cache, "long_investment", 84, 0, 0.00_04));
        futures.add(save(cache, "pioneer", 70, 28, 0.00_04));
        futures.add(save(cache, "expansion", 91, 35, 0.00_05));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(CachedOutput cache, String id, int termDays, int freeDays, double termSurcharge) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("term_days", termDays);
        json.addProperty("free_days", freeDays);
        json.addProperty("term_surcharge", termSurcharge);

        ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, id);
        return DataProvider.saveStable(cache, json, pathProvider.json(fileId));
    }

    @Override
    public String getName() {
        return "Kontor Bank Loan Offers";
    }
}
