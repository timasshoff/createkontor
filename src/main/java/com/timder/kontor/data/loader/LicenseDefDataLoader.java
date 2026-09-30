package com.timder.kontor.data.loader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.core.company.legalform.LegalFormDef;
import com.timder.kontor.core.company.license.LicenseDef;
import com.timder.kontor.core.market.MarketDefinition;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.data.KontorData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;

public class LicenseDefDataLoader extends SimpleJsonResourceReloadListener {

    public LicenseDefDataLoader() {
        super(new Gson(), "kontor/license_definitions");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<String, LicenseDef> byId = new TreeMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : new TreeMap<>(resourceList).entrySet()) {
            ResourceLocation file = entry.getKey();
            try {
                LicenseDef definition = parse(entry.getValue());
                String id = definition.key().toString();
                if (byId.containsKey(id)) {
                    CreateKontor.LOGGER.warn("License definition {} in {} overrides another already loaded definition.", id, file);
                }
                byId.put(id, definition);
            } catch (RuntimeException e) {
                CreateKontor.LOGGER.warn("License definition {} could not be loaded, skipped: {}", file, e.getMessage());
            }
        }

        Set<ItemId> markets = new LinkedHashSet<>();
        for (MarketDefinition market : KontorData.getMarketDefinitions()) {
            markets.add(market.id());
        }
        int highestLevel = 0;
        for (LegalFormDef legalForm : KontorData.getLegalFormDefinitions()) {
            highestLevel = Math.max(highestLevel, legalForm.level());
        }
        if (markets.isEmpty()) {
            CreateKontor.LOGGER.warn("There are no market definitions, the markets of the license definitions are not checked.");
        }
        for (LicenseDef definition : byId.values()) {
            for (String problem : problems(definition, markets, highestLevel)) {
                CreateKontor.LOGGER.warn("License definition {}: {}", definition.key(), problem);
            }
        }

        KontorData.setLicenseDefinitions(List.copyOf(byId.values()));
        CreateKontor.LOGGER.info("Loaded a total of {} license definitions.", byId.size());
    }

    private static LicenseDef parse(JsonElement json) {
        JsonObject object = GsonHelper.convertToJsonObject(json, "license definition");

        String id = GsonHelper.getAsString(object, "id");
        JsonArray marketArray = GsonHelper.getAsJsonArray(object, "markets");
        double feeFactor = GsonHelper.getAsDouble(object, "fee_factor");
        double dailyFraction = GsonHelper.getAsDouble(object, "daily_fraction");
        double revenueShare = GsonHelper.getAsDouble(object, "revenue_share");
        int minLegalLevel = GsonHelper.getAsInt(object, "min_legal_level");

        Set<ItemId> markets = new LinkedHashSet<>();
        for (int i = 0; i < marketArray.size(); i++) {
            String value = GsonHelper.convertToString(marketArray.get(i), "markets[" + i + "]");
            ResourceLocation location = ResourceLocation.tryParse(value);
            if (location == null) throw new IllegalArgumentException("'markets[" + i + "]' is not a valid resource location: " + value);
            if (!markets.add(new ItemId(location.toString()))) throw new IllegalArgumentException("'markets' lists " + location + " twice.");
        }

        return LicenseDef.defined(id, markets, feeFactor, dailyFraction, revenueShare, minLegalLevel);
    }

    private static List<String> problems(LicenseDef license, Set<ItemId> knownMarkets, int highestLegalLevel) {
        List<String> problems = new ArrayList<>();
        if (!knownMarkets.isEmpty()) {
            int missing = 0;
            for (ItemId market : license.markets()) {
                if (!knownMarkets.contains(market)) {
                    missing++;
                    problems.add("there is no market for " + market + ", the license does not open it.");
                }
            }
            if (missing == license.markets().size()) {
                problems.add("none of its markets exists, the license is ignored.");
            }
        }
        if (highestLegalLevel > 0 && license.minLegalLevel() > highestLegalLevel) {
            problems.add("min_legal_level " + license.minLegalLevel() + " is above the highest legal form (" + highestLegalLevel + "), no company can buy it.");
        }
        return problems;
    }
}
