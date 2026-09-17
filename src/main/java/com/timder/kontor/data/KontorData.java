package com.timder.kontor.data;

import com.timder.kontor.core.market.GroupDef;
import com.timder.kontor.core.market.MarketDefinition;
import com.timder.kontor.core.raw.RawMaterialDefinition;

import java.util.List;
import java.util.Map;

public class KontorData {

    private static volatile List<RawMaterialDefinition> rawMaterials = List.of();
    private static volatile Map<String, GroupDef> groupDefinitions = Map.of();
    private static volatile List<MarketDefinition> marketDefinitions = List.of();
    private static volatile Map<String, Double> processCosts = Map.of();

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
}
