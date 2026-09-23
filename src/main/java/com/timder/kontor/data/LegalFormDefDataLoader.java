package com.timder.kontor.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.core.company.LegalFormDef;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.market.GroupDef;
import com.timder.kontor.core.market.MarketDefinition;
import com.timder.kontor.core.value.ItemId;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LegalFormDefDataLoader extends SimpleJsonResourceReloadListener {

    public LegalFormDefDataLoader() {
        super(new Gson(), "kontor/legal_form_definitions");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<Integer, LegalFormDef> byLevel = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceList.entrySet()) {
            ResourceLocation file = entry.getKey();
            try {
                LegalFormDef definition = parse(entry.getValue());
                if (byLevel.containsKey(definition.level())) {
                    CreateKontor.LOGGER.warn("Legal form definition {} in {} overrides another already loaded definition.", definition.id(), file);
                }
                byLevel.put(definition.level(), definition);
            } catch (RuntimeException e) {
                CreateKontor.LOGGER.warn("Legal form definition {} could not be loaded, skipped: {}", file, e.getMessage());
            }
        }

        KontorData.setLegalFormDefinitions(List.copyOf(byLevel.values()));
        CreateKontor.LOGGER.info("Loaded a total of {} legal form definitions.", byLevel.size());
    }

    private static LegalFormDef parse(JsonElement json) {
        JsonObject object = GsonHelper.convertToJsonObject(json, "product");

        int level = GsonHelper.getAsInt(object, "level");
        String id = GsonHelper.getAsString(object, "id");
        int maxEmployees = GsonHelper.getAsInt(object, "max_employees");
        int maxProductLicenses = GsonHelper.getAsInt(object, "max_product_licenses");
        int maxOpenRequestsPerProduct = GsonHelper.getAsInt(object, "max_open_requests_per_product");
        int maxOpenRequestsTotal = GsonHelper.getAsInt(object, "max_open_requests_total");
        int maxOpenOrders = GsonHelper.getAsInt(object, "max_open_orders");
        int maxOpenContracts = GsonHelper.getAsInt(object, "max_open_contracts");
        int maxOrderQuantity = GsonHelper.getAsInt(object, "max_order_quantity");
        double deadlineFactor = GsonHelper.getAsDouble(object, "deadline_factor");
        int maxGridConnection = GsonHelper.getAsInt(object, "max_grid_connection");
        int feedInLicenseTier = GsonHelper.getAsInt(object, "feed_in_license_tier");
        Money overdraftLimit = Money.ofDollars(GsonHelper.getAsLong(object, "overdraft_limit_in_dollars"));
        Money bankloanLimit = Money.ofDollars(GsonHelper.getAsLong(object, "bankloan_limit_in_dollars"));
        boolean autoAcceptRequests = GsonHelper.getAsBoolean(object, "auto_accept_requests");
        boolean logisticsNetwork = GsonHelper.getAsBoolean(object, "logistics_network");
        Money freeStorage = Money.ofDollars(GsonHelper.getAsLong(object, "free_storage_value_in_dollars"));
        boolean founderProtection = GsonHelper.getAsBoolean(object, "founder_protection");

        return new LegalFormDef(
                level,
                id,
                maxEmployees,
                maxProductLicenses,
                maxOpenRequestsPerProduct,
                maxOpenRequestsTotal,
                maxOpenOrders,
                maxOpenContracts,
                maxOrderQuantity,
                deadlineFactor,
                maxGridConnection,
                feedInLicenseTier,
                overdraftLimit,
                bankloanLimit,
                autoAcceptRequests,
                logisticsNetwork,
                freeStorage,
                founderProtection
        );
    }
}
