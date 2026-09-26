package com.timder.kontor.data;

import com.google.gson.JsonObject;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.core.company.LegalFormDef;
import com.timder.kontor.core.company.LegalForms;
import com.timder.kontor.core.company.financial.Money;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LegalFormDefDataProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;

    public LegalFormDefDataProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "kontor/legal_form_definitions");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(save(cache, "sole_proprietorship", 1, "sole_proprietorship", 2, 2, 2, 5, 3, 0, 64, 1.5, 1024, 0, Money.ofDollars(1000), Money.ZERO, false, false, Money.ZERO, true, 1));
        futures.add(save(cache, "partnership", 2, "partnership", 4, 6, 3, 12, 10, 0, 256, 1.2, 4096, 1, Money.ofDollars(5000), Money.ofDollars(20000), true, false, Money.ZERO, false, 2));
        futures.add(save(cache, "limited_company", 3, "limited_company", 6, 15, 5, 30, 40, 6, 1024, 1.0, 16384, 2, Money.ofDollars(20000), Money.ofDollars(100000), true, true, Money.ofDollars(20000), false,3));
        futures.add(save(cache, "public_company", 4, "public_company", 10, LegalFormDef.UNLIMITED, 8, 80, 150, 20, 4096, 1.0, 65536, 3, Money.ofDollars(100000), Money.ofDollars(500000), true, true, Money.ofDollars(40000), false, 6));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(CachedOutput cache,
                                      String fileName,
                                      int level,
                                      String id,
                                      int maxEmployees,
                                      int maxProductLicenses,
                                      int maxOpenRequestsPerProduct,
                                      int maxOpenRequestsTotal,
                                      int maxOpenOrders,
                                      int maxOpenContracts,
                                      int maxOrderQuantity,
                                      double deadlineFactor,
                                      int maxGridConnection,
                                      int feedInLicenseTier,
                                      Money overdraftLimit,
                                      Money bankloanLimit,
                                      boolean autoAcceptRequests,
                                      boolean logisticsNetwork,
                                      Money freeStorage,
                                      boolean founderProtection,
                                      int maxShippingExits
    ) {
        JsonObject json = new JsonObject();
        json.addProperty("level", level);
        json.addProperty("id", id);
        json.addProperty("max_employees", maxEmployees);
        json.addProperty("max_product_licenses", maxProductLicenses);
        json.addProperty("max_open_requests_per_product", maxOpenRequestsPerProduct);
        json.addProperty("max_open_requests_total", maxOpenRequestsTotal);
        json.addProperty("max_open_orders", maxOpenOrders);
        json.addProperty("max_open_contracts", maxOpenContracts);
        json.addProperty("max_order_quantity", maxOrderQuantity);
        json.addProperty("deadline_factor", deadlineFactor);
        json.addProperty("max_grid_connection", maxGridConnection);
        json.addProperty("feed_in_license_tier", feedInLicenseTier);
        json.addProperty("overdraft_limit_in_dollars", Math.round(overdraftLimit.toDollars()));
        json.addProperty("bankloan_limit_in_dollars", Math.round(bankloanLimit.toDollars()));
        json.addProperty("auto_accept_requests", autoAcceptRequests);
        json.addProperty("logistics_network", logisticsNetwork);
        json.addProperty("free_storage_value_in_dollars", Math.round(freeStorage.toDollars()));
        json.addProperty("founder_protection", founderProtection);
        json.addProperty("max_shipping_exits", maxShippingExits);

        ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, fileName);
        return DataProvider.saveStable(cache, json, pathProvider.json(fileId));
    }

    @Override
    public String getName() {
        return "Kontor Legal Form Definitions";
    }
}
