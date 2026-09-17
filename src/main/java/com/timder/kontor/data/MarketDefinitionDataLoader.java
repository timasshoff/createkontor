package com.timder.kontor.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.config.EconomyConfig;
import com.timder.kontor.core.market.GroupDef;
import com.timder.kontor.core.market.MarketDefinition;
import com.timder.kontor.core.market.MarketParams;
import com.timder.kontor.core.value.ItemId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MarketDefinitionDataLoader extends SimpleJsonResourceReloadListener {

    private static final double PLACEHOLDER_REFERENCE_COST = 1.0;

    public MarketDefinitionDataLoader() {
        super(new Gson(), "kontor/market_definitions");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<String, GroupDef> groups = KontorData.getGroupDefinitions();
        Map<ItemId, MarketDefinition> byItem = new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceList.entrySet()) {
            ResourceLocation file = entry.getKey();
            try {
                MarketDefinition definition = parse(entry.getValue(), groups);
                if (byItem.containsKey(definition.id())) {
                    CreateKontor.LOGGER.warn("Market definition {} in {} overrides another already loaded definition for the same item.",
                            definition.id(), file);
                }
                byItem.put(definition.id(), definition);
            } catch (RuntimeException e) {
                CreateKontor.LOGGER.warn("Market definition {} could not be loaded, skipped: {}", file, e.getMessage());
            }
        }

        KontorData.setMarketDefinitions(List.copyOf(byItem.values()));
        CreateKontor.LOGGER.info("Loaded a total of {} market definitions.", byItem.size());
    }

    private static MarketDefinition parse(JsonElement json, Map<String, GroupDef> groups) {
        JsonObject object = GsonHelper.convertToJsonObject(json, "product");

        String itemString = GsonHelper.getAsString(object, "item");
        ResourceLocation location = ResourceLocation.tryParse(itemString);
        if (location == null) throw new IllegalArgumentException("'item' is not a valid ressource location: " + itemString);
        if (!BuiltInRegistries.ITEM.containsKey(location)) throw new IllegalArgumentException("'item' is not a registered item " + itemString);


        String groupId = GsonHelper.getAsString(object, "group");
        GroupDef group = groups.get(groupId);
        if (group == null) throw new IllegalArgumentException("Unknown group definition '" + groupId + "'.");


        double baseDemand = GsonHelper.getAsDouble(object, "base_demand");
        double targetUtilisation = GsonHelper.getAsDouble(object, "target_utilisation", 0.8); // TODO Check if this is okay

        double plantSize = 0.4 * baseDemand;
        MarketParams params = new MarketParams(PLACEHOLDER_REFERENCE_COST, plantSize, targetUtilisation, group);

        ItemId itemId = new ItemId(location.toString());
        return new MarketDefinition(itemId, params, baseDemand);
    }
}
