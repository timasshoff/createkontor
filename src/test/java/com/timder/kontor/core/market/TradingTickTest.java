package com.timder.kontor.core.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TradingTickTest {

    private static final MarketParams IRON_SHEET = new MarketParams(8.06, 720.0, 0.80, GroupDef.metal());

    private static final double DAILY_DEMAND = 1800.0;
    private static final double TICK_DEMAND = DAILY_DEMAND / MarketRules.TRADING_TICKS_PER_DAY;

    /**
     * The dynamics these tests were written for, stated here so they do not move when the defaults move.
     * Order: deviationDecay, deviationStrength, deviationBound, maxPriceFactor, minCompetitors, maxCompetitors,
     * costPassThrough, costPassThroughDown, trustThreshold (0 = off), trustWidth, maxShare (1 = off).
     */
    private static final MarketDynamicsParams DYNAMICS =
            new MarketDynamicsParams(0.94, 0.02, 0.5, 2.0, 1.0, 8.0, 1.0, 0.0, 0.0, 0.2, 1.0);

    /**
     * One trading tick without purchases. Arguments of the rule: delivered, purchased, expected delivered.
     */
    private static void tick(MarketState state, double delivered, double expected) {
        MarketRules.advanceTradingTick(state, delivered, 0.0, expected, TICK_DEMAND, DYNAMICS);
    }

    @Test
    @DisplayName("Delivering exactly the expected amount leaves the deviation at zero")
    void exactDeliveryMeansNoDeviation() {
        MarketState state = MarketState.fresh(IRON_SHEET);

        for (int tick = 0; tick < 60; tick++) {
            double expected = TICK_DEMAND * 0.25;
            state.recordDelivery(expected);
            tick(state, expected, expected);
        }

        assertEquals(0.0, state.getDeviation(), 1e-9);
        assertEquals(state.getPriceLevel(), state.getDisplayedPrice(), 1e-9);
    }

    @Test
    @DisplayName("A shortfall instead of a dump pushes the deviation up, not down")
    void shortfallPushesUp() {
        MarketState state = MarketState.fresh(IRON_SHEET);

        state.recordDelivery(50.0);
        tick(state, 50.0, 200.0);

        assertTrue(state.getDeviation() > 0, "a shortfall should raise the displayed price");
    }

    @Test
    @DisplayName("Displayed price matches the concept formula exactly")
    void displayedPriceMatchesFormula() {
        MarketState state = new MarketState(9.03, 3.0);
        state.setDeviation(-0.04);

        assertEquals(9.03 * 0.96, state.getDisplayedPrice(), 1e-9);
    }

    @Test
    @DisplayName("The tick counter is cleared every tick, the daily counter is not")
    void countersHaveSeparateLifecycles() {
        MarketState state = MarketState.fresh(IRON_SHEET);
        state.recordDelivery(120.0);

        tick(state, 120.0, 100.0);

        assertEquals(0.0, state.getDeliveredThisTick(), 1e-9, "the tick counter should be cleared");
        assertEquals(
                120.0,
                state.getDeliveredToday(),
                1e-9,
                "the daily counter should still hold everything delivered so far");
    }

    @Test
    @DisplayName("AdvanceDay never touches the deviation, only the structural price")
    void dailyRuleLeavesDeviationAlone() {
        MarketState state = MarketState.fresh(IRON_SHEET);
        state.setDeviation(-0.04);

        MarketRules.advanceDay(state, IRON_SHEET, DAILY_DEMAND, DYNAMICS);

        assertEquals(-0.04, state.getDeviation(), 1e-9);
    }
}
