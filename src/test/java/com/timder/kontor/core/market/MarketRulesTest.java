package com.timder.kontor.core.market;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarketRulesTest {

    private static final MarketParams IRON_SHEET = new MarketParams(
        8.06,
        720.0,
        0.80,
        GroupDef.metal()
    );

    private static final double DEMAND = 1800.0;

    private static MarketState runDays(MarketParams params, double demand, int days) {
        MarketState state = MarketState.fresh(params);
        for (int i = 0; i < days; i++) {
            MarketRules.advanceDay(state, params, demand);
        }
        return state;
    }

    @Test
    @DisplayName("Price after 60 Days should be at reference cost * target margin")
    void priceIsStable() {
        MarketState state = runDays(IRON_SHEET, DEMAND, 60);
        double expected = IRON_SHEET.equilibriumPrice();
        assertEquals(expected, state.getPriceLevel(), expected * 0.02);
    }

    @Test
    @DisplayName("Amount of companies adapts to demand")
    void companiesAreStable() {
        MarketState state = runDays(IRON_SHEET, DEMAND, 60);

        double expected = IRON_SHEET.equilibriumCompanies(DEMAND);
        assertEquals(expected, state.getCompanies(), expected * 0.05);
    }

    @Test
    @DisplayName("Deliveries by companies hurt competitors")
    void deliveriesHurtCompetitors() {
        MarketState state = MarketState.fresh(IRON_SHEET);
        state.recordDelivery(500);

        DayResult result = MarketRules.advanceDay(state, IRON_SHEET, DEMAND);

        assertEquals(500.0, result.soldByCompanies());
        assertEquals(1300.0, result.soldByCompetitors());
        assertEquals(0.0, result.overflow());
    }

    @Test
    @DisplayName("Overflow, when capacity is not sufficient")
    void overflowWhenShortage() {
        MarketState state = new MarketState(IRON_SHEET.equilibriumPrice(), 1.0);

        DayResult result = MarketRules.advanceDay(state, IRON_SHEET, DEMAND);

        assertEquals(720.0, result.soldByCompetitors(), 0.001);
        assertEquals(1080.0, result.overflow(), 0.001);
        assertEquals(1.0, result.utilisation(), 0.001);
    }

    @Test
    @DisplayName("Utilisation influences price")
    void priceFollowsUtilitsation() {
        MarketState high = new MarketState(9.50, 2.0);
        double before = high.getPriceLevel();
        MarketRules.advanceDay(high, IRON_SHEET, DEMAND);
        assertTrue(high.getPriceLevel() > before);

        MarketState low = new MarketState(9.50, 6.0);
        before = low.getPriceLevel();
        MarketRules.advanceDay(low, IRON_SHEET, DEMAND);
        assertTrue(low.getPriceLevel() < before);
    }
}
