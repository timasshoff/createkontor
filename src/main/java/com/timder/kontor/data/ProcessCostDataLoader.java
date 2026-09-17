package com.timder.kontor.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.timder.kontor.CreateKontor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProcessCostDataLoader extends SimpleJsonResourceReloadListener {

    public ProcessCostDataLoader() {
        super(new Gson(), "kontor/process_costs");
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager ressourceList, ProfilerFiller profilerFiller) {
        Map<String, Double> byId = new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceList.entrySet()) {
            ResourceLocation file = entry.getKey();
            try {
                JsonObject object = GsonHelper.convertToJsonObject(entry.getValue(), "process_cost");
                String id = GsonHelper.getAsString(object, "id");
                ResourceLocation location = ResourceLocation.tryParse(id);
                if (location == null) throw new IllegalArgumentException("'id' is not a valid ressource location: " + id);
                if (!BuiltInRegistries.RECIPE_TYPE.containsKey(location)) throw new IllegalArgumentException("'id' is not a registered recipe type: " + id);

                double cost = GsonHelper.getAsDouble(object, "cost");
                if (cost < 0) throw new IllegalArgumentException("'cost' must not be negative.");

                String key = location.toString();
                if (byId.containsKey(key)) {
                    CreateKontor.LOGGER.warn("Process cost {} in {} overrides another already loaded definition for the same process.", key, file);
                }
                byId.put(key, cost);
            } catch (RuntimeException e) {
                CreateKontor.LOGGER.warn("Process cost {} could not be loaded, skipped: {}", file, e.getMessage());
            }
        }

        KontorData.setProcessCosts(Map.copyOf(byId));
        CreateKontor.LOGGER.info("Loaded a total of {} process cost definition(s).", byId.size());
    }
}
