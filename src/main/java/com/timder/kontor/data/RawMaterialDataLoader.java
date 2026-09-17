package com.timder.kontor.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.core.raw.RawMaterialDefinition;
import com.timder.kontor.core.raw.RawMaterialParams;
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

public class RawMaterialDataLoader extends SimpleJsonResourceReloadListener {

    public RawMaterialDataLoader() {
        super(new Gson(), "kontor/raw_materials");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ItemId, RawMaterialDefinition> byItem = new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceList.entrySet()) {
            ResourceLocation file = entry.getKey();
            try {
                RawMaterialDefinition definition = parse(entry.getValue());
                if (byItem.containsKey(definition.id())) {
                    CreateKontor.LOGGER.warn("Raw material {} in {} overrides another already loaded definition for this material.", definition.id(), file);
                }
                byItem.put(definition.id(), definition);
            } catch (RuntimeException e) {
                CreateKontor.LOGGER.warn("Raw material {} could not be loaded, skipped: {}", file, e.getMessage());
            }
        }

        List<RawMaterialDefinition> definitions = List.copyOf(byItem.values());
        KontorData.setRawMaterials(definitions);
        CreateKontor.LOGGER.info("Loaded a total of {} raw material definitions.", definitions.size());
    }

    private static RawMaterialDefinition parse(JsonElement json) {
        JsonObject object = GsonHelper.convertToJsonObject(json, "raw_material");

        String itemString = GsonHelper.getAsString(object, "item");
        ResourceLocation location = ResourceLocation.tryParse(itemString);
        if (location == null) throw new IllegalArgumentException("'item' is not a valid resource location: " + itemString);
        if (!BuiltInRegistries.ITEM.containsKey(location)) throw new IllegalArgumentException("'item' is not a registered item: " + itemString);

        double baseValue = GsonHelper.getAsDouble(object, "base_value");
        double dailyVolatility = GsonHelper.getAsDouble(object, "daily_volatility");
        double referenceVolume = GsonHelper.getAsDouble(object, "reference_volume");

        ItemId itemId = new ItemId(location.toString());
        RawMaterialParams params = new RawMaterialParams(baseValue, dailyVolatility, referenceVolume);
        return new RawMaterialDefinition(itemId, params);
    }
}
