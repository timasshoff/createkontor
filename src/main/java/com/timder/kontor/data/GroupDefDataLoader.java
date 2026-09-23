package com.timder.kontor.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.core.market.GroupDef;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.Map;

public class GroupDefDataLoader extends SimpleJsonResourceReloadListener {

    public GroupDefDataLoader() {
        super(new Gson(), "kontor/group_definitions");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<String, GroupDef> byId = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceList.entrySet()) {
            ResourceLocation file = entry.getKey();
            try {
                IdentifiableGroupDef definition = parse(entry.getValue());
                if (byId.containsKey(definition.id())) {
                    CreateKontor.LOGGER.warn("Group definition {} in {} overrides another already loaded definition for this group.", definition.id(), file);
                }
                byId.put(definition.id(), definition.def());
            } catch (RuntimeException e) {
                CreateKontor.LOGGER.warn("Group definition {} could not be loaded, skipped: {}", file, e.getMessage());
            }
        }

        KontorData.setGroupDefinitions(Map.copyOf(byId));
        CreateKontor.LOGGER.info("Loaded a total of {} group definitions.", byId.size());
    }

    private static IdentifiableGroupDef parse(JsonElement json) {
        JsonObject object = GsonHelper.convertToJsonObject(json, "group_definition");

        String id = GsonHelper.getAsString(object, "id");
        double priceSensitivity = GsonHelper.getAsDouble(object, "price_sensitivity");
        double reputationWeight = GsonHelper.getAsDouble(object, "reputation_weight");
        double targetMargin = GsonHelper.getAsDouble(object, "target_margin");
        double priceResponse = GsonHelper.getAsDouble(object, "price_response");
        double capacityResponse = GsonHelper.getAsDouble(object, "capacity_response");
        double cycleSensitivity = GsonHelper.getAsDouble(object, "cycle_sensitivity");

        GroupDef def = new GroupDef(
                priceSensitivity,
                reputationWeight,
                targetMargin,
                priceResponse,
                capacityResponse,
                cycleSensitivity);

        return new IdentifiableGroupDef(id, def);
    }

    public record IdentifiableGroupDef(String id, GroupDef def) {}
}
