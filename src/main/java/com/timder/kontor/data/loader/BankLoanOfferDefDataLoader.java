package com.timder.kontor.data.loader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.core.company.financial.BankLoanOfferDef;
import com.timder.kontor.data.KontorData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

public class BankLoanOfferDefDataLoader extends SimpleJsonResourceReloadListener {

    public BankLoanOfferDefDataLoader() {
        super(new Gson(), "kontor/bank_loan_offers");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> resourceList,
            ResourceManager resourceManager,
            ProfilerFiller profilerFiller) {
        Map<String, BankLoanOfferDef> byId = new TreeMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : new TreeMap<>(resourceList).entrySet()) {
            ResourceLocation file = entry.getKey();
            try {
                BankLoanOfferDef definition = parse(entry.getValue());
                if (byId.containsKey(definition.id())) {
                    CreateKontor.LOGGER.warn(
                            "Bank loan offer {} in {} overrides another already loaded offer.", definition.id(), file);
                }
                byId.put(definition.id(), definition);
            } catch (RuntimeException e) {
                CreateKontor.LOGGER.warn("Bank loan offer {} could not be loaded, skipped: {}", file, e.getMessage());
            }
        }

        List<BankLoanOfferDef> offers = new ArrayList<>(byId.values());
        if (offers.isEmpty()) {
            CreateKontor.LOGGER.warn("There are no bank loan offers, companies cannot take bank loans.");
        }

        KontorData.setBankLoanOffers(offers);
        CreateKontor.LOGGER.info("Loaded a total of {} bank loan offers.", offers.size());
    }

    private static BankLoanOfferDef parse(JsonElement json) {
        JsonObject object = GsonHelper.convertToJsonObject(json, "bank loan offer");

        String id = GsonHelper.getAsString(object, "id");
        int termDays = GsonHelper.getAsInt(object, "term_days");
        int freeDays = GsonHelper.getAsInt(object, "free_days");
        double termSurcharge = GsonHelper.getAsDouble(object, "term_surcharge");

        return new BankLoanOfferDef(id, termDays, freeDays, termSurcharge);
    }
}
