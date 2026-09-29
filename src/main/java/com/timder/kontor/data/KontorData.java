package com.timder.kontor.data;

import com.timder.kontor.CreateKontor;
import com.timder.kontor.core.company.legalform.LegalFormDef;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.license.LicenseCatalog;
import com.timder.kontor.core.company.license.LicenseDef;
import com.timder.kontor.core.company.license.LicenseParams;
import com.timder.kontor.core.market.GroupDef;
import com.timder.kontor.core.market.MarketDefinition;
import com.timder.kontor.core.raw.RawMaterialDefinition;
import com.timder.kontor.core.value.ItemId;

import java.util.List;
import java.util.Map;

public class KontorData {

    private static volatile List<RawMaterialDefinition> rawMaterials = List.of();
    private static volatile Map<String, GroupDef> groupDefinitions = Map.of();
    private static volatile List<MarketDefinition> marketDefinitions = List.of();
    private static volatile Map<String, Double> processCosts = Map.of();
    private static volatile List<LegalFormDef> legalFormDefinitions = List.of();
    private static volatile List<LicenseDef> licenseDefinitions = List.of();
    private static volatile CachedLicenseCatalog cachedLicenseCatalog = null;

    public static List<RawMaterialDefinition> getRawMaterials() {
        return rawMaterials;
    }

    static void setRawMaterials(List<RawMaterialDefinition> value) {
        rawMaterials = List.copyOf(value);
    }

    public static Map<String, GroupDef> getGroupDefinitions() {
        return groupDefinitions;
    }

    static void setGroupDefinitions(Map<String, GroupDef> value) {
        groupDefinitions = Map.copyOf(value);
    }

    public static List<MarketDefinition> getMarketDefinitions() {
        return marketDefinitions;
    }

    static void setMarketDefinitions(List<MarketDefinition> value) {
        marketDefinitions = List.copyOf(value);
    }

    public static Map<String, Double> getProcessCosts() {
        return processCosts;
    }

    static void setProcessCosts(Map<String, Double> value) {
        processCosts = Map.copyOf(value);
    }

    public static List<LegalFormDef> getLegalFormDefinitions() {
        return legalFormDefinitions;
    }

    static void setLegalFormDefinitions(List<LegalFormDef> value) {
        legalFormDefinitions = List.copyOf(value);
    }

    public static List<LicenseDef> getLicenseDefinitions() {
        return licenseDefinitions;
    }

    static void setLicenseDefinitions(List<LicenseDef> value) {
        licenseDefinitions = List.copyOf(value);
    }

    public static LicenseCatalog getLicenseCatalog(LegalForms legalForms, LicenseParams params) {
        List<MarketDefinition> currentMarkets = marketDefinitions;
        List<LicenseDef> currentLicenses = licenseDefinitions;
        CachedLicenseCatalog cached = cachedLicenseCatalog;
        if (cached != null && cached.markets() == currentMarkets && cached.licenses() == currentLicenses
                && cached.legalForms().equals(legalForms) && cached.params().equals(params)) {
            return cached.catalog();
        }

        List<ItemId> marketIds = currentMarkets.stream().map(MarketDefinition::id).toList();
        LicenseCatalog catalog = LicenseCatalog.build(params, currentLicenses, marketIds, legalForms);

        cachedLicenseCatalog = new CachedLicenseCatalog(currentMarkets, currentLicenses, legalForms, params, catalog);
        return catalog;
    }

    private record CachedLicenseCatalog(
            List<MarketDefinition> markets,
            List<LicenseDef> licenses,
            LegalForms legalForms,
            LicenseParams params,
            LicenseCatalog catalog
    ) {
    }

}
