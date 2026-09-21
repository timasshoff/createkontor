package com.timder.kontor.core.value;

import com.timder.kontor.core.port.FakeRecipeGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ValueRulesTest {

    private static final ItemId RAW_IRON = new ItemId("raw_iron");
    private static final ItemId IRON_INGOT = new ItemId("iron_ingot");
    private static final ItemId IRON_SHEET = new ItemId("iron_sheet");
    private static final ItemId RAW_COPPER = new ItemId("raw_copper");
    private static final ItemId COPPER_INGOT = new ItemId("copper_ingot");
    private static final ItemId RAW_ZINC = new ItemId("raw_zinc");
    private static final ItemId ZINC_INGOT = new ItemId("zinc_ingot");
    private static final ItemId BRASS_INGOT = new ItemId("brass_ingot");

    private FakeRecipeGraph ironAndBrassChain() {
        return new FakeRecipeGraph()
                .add(IRON_INGOT.value(), 1.0, RAW_IRON.value(), 1.0, "smelting")
                .add(IRON_SHEET.value(), 1.0, IRON_INGOT.value(), 1.0, "pressing")
                .add(COPPER_INGOT.value(), 1.0, RAW_COPPER.value(), 1.0, "smelting")
                .add(ZINC_INGOT.value(), 1.0, RAW_ZINC.value(), 1.0, "smelting")
                .add(new RecipeNode(BRASS_INGOT, 2.0,
                        List.of(new RecipeInput(List.of(COPPER_INGOT), 1.0), new RecipeInput(List.of(ZINC_INGOT), 1.0)), "mixing_heated"));
    }

    private static final Map<ItemId, Double> LEAVES = Map.of(RAW_IRON, 6.20, RAW_COPPER, 2.00, RAW_ZINC, 3.00);
    private static final Map<ItemId, Double> MARKET_PRICES = Map.of(IRON_INGOT, 7.26, COPPER_INGOT, 2.80, ZINC_INGOT, 4.00);

    @Test
    @DisplayName("Test for Iron ingot: 6.60, smelted from raw iron")
    void ironIngot() {
        ValueResult result = compute(ironAndBrassChain());
        assertEquals(6.60, result.referenceCost().get(IRON_INGOT), 0.001);
    }

    @Test
    @DisplayName("Test for iron sheet: 8.06, pressed from iron ingot at its market price, not its own reference cost")
    void ironSheet() {
        ValueResult result = compute(ironAndBrassChain());
        assertEquals(8.06, result.referenceCost().get(IRON_SHEET), 0.001);

        // The whole point: 8.06 uses 7.26 (the market price), not 6.60 (the reference
        // cost computed for the very same ingot one line above).
        assertNotEquals(result.referenceCost().get(IRON_INGOT) + 0.80, result.referenceCost().get(IRON_SHEET), 0.001);
    }

    @Test
    @DisplayName("Technical progress discounts every processing step in the chain, never the leaves")
    void progressAppliesThroughTheChain() {
        ValueResult full = compute(ironAndBrassChain(), 1.0);
        ValueResult half = compute(ironAndBrassChain(), 0.5);

        assertEquals(6.40, half.referenceCost().get(IRON_INGOT), 0.001, "6.20 + 0.40*0.5");
        assertEquals(7.66, half.referenceCost().get(IRON_SHEET), 0.001, "the market price 7.26 stays untouched, only the 0.80 pressing step is discounted");
        assertTrue(half.referenceCost().get(IRON_SHEET) < full.referenceCost().get(IRON_SHEET));
    }

    @Test
    @DisplayName("A good that only ever refers to itself through zero-progress recipes stays unvalued, not stuck in a loop")
    void pureCycleWithNoLeafStaysUnvalued() {
        ItemId a = new ItemId("a");
        ItemId b = new ItemId("b");
        FakeRecipeGraph graph = new FakeRecipeGraph()
                .add(a.value(), 1.0, b.value(), 1.0, "crafting")
                .add(b.value(), 1.0, a.value(), 1.0, "crafting");

        ValueResult result = ValueRules.computeValues(Set.of(a, b), graph,
                ProcessCosts.standard(), Map.of(), Map.of(), 1.0);

        assertEquals(Set.of(a, b), result.unvalued());
        assertTrue(result.referenceCost().isEmpty());
    }

    private ValueResult compute(FakeRecipeGraph graph) {
        return compute(graph, 1.0);
    }

    private ValueResult compute(FakeRecipeGraph graph, double technicalProgress) {
        Set<ItemId> scope = ValueRules.discover(Set.of(IRON_SHEET, BRASS_INGOT), graph);
        return ValueRules.computeValues(scope, graph, ProcessCosts.standard(), LEAVES, MARKET_PRICES, technicalProgress);
    }
}
