package com.timder.kontor.data.provider;

import com.google.gson.JsonObject;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.core.company.legalform.LegalFormDef;
import com.timder.kontor.core.company.legalform.UpgradeRequirements;
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

        futures.add(save(cache, "sole_proprietorship",
                1,
                "sole_proprietorship",
                2,
                2,
                2,
                5,
                3,
                0,
                64,
                1.5,
                1024,
                0,
                Money.ofDollars(1000), // Overdraft limit
                Money.ZERO, // Bank loan limit
                false,
                false,
                Money.ZERO, // Free storage
                true,
                1,
                null));

        futures.add(save(cache, "partnership",
                2,
                "partnership",
                4,
                6,
                3,
                12,
                10,
                0,
                256,
                1.2,
                4096,
                1,
                Money.ofDollars(5000), // Overdraft limit
                Money.ofDollars(20000), // Bank loan limit
                true,
                false,
                Money.ZERO, // Free storage
                false,
                2,
                new UpgradeRequirements(
                        Money.ofDollars(10_000), // Fee
                        Money.ofDollars(5000), // Net worth
                        30,
                        3,
                        24000,
                        7
                )));

        futures.add(save(cache, "limited_company",
                3,
                "limited_company",
                6,
                15,
                5,
                30,
                40,
                6,
                1024,
                1.0,
                16384,
                2,
                Money.ofDollars(20000),  // Overdraft limit
                Money.ofDollars(100000), // Bank loan limit
                true,
                true,
                Money.ofDollars(20000), // Free storage
                false,
                3,
                new UpgradeRequirements(
                        Money.ofDollars(25_000), // Fee
                        Money.ofDollars(20_000), // Net worth
                        75,
                        3.5,
                        24000,
                        7
                )));

        futures.add(save(cache, "public_company",
                4,
                "public_company",
                10,
                LegalFormDef.UNLIMITED,
                8,
                80,
                150,
                20,
                4096,
                1.0,
                65536,
                3,
                Money.ofDollars(100000),  // Overdraft limit
                Money.ofDollars(500000), // Bank loan limit
                true,
                true,
                Money.ofDollars(40000), // Free storage
                false,
                6,
                new UpgradeRequirements(
                        Money.ofDollars(50_000), // Fee
                        Money.ofDollars(100_000), // Net worth
                        150,
                        4.0,
                        24000,
                        7
                )));

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
                                      int maxShippingExits,
                                      UpgradeRequirements entryRequirements
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

        if (entryRequirements != null) {
            JsonObject requirements = new JsonObject();
            requirements.addProperty("fee_in_dollars", Math.round(entryRequirements.fee().toDollars()));
            requirements.addProperty("min_net_worth_in_dollars", Math.round(entryRequirements.minNetWorth().toDollars()));
            requirements.addProperty("min_fulfilled_orders", entryRequirements.minFulfilledOrders());
            requirements.addProperty("min_reputation_stars", entryRequirements.minReputationStars());
            requirements.addProperty("processing_ticks", entryRequirements.processingTicks());
            requirements.addProperty("resting_days", entryRequirements.restingDays());
            json.add("upgrade_requirements", requirements);
        }

        ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, fileName);
        return DataProvider.saveStable(cache, json, pathProvider.json(fileId));
    }

    @Override
    public String getName() {
        return "Kontor Legal Form Definitions";
    }
}
