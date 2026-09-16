package com.timder.kontor.core.market;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TradingTickTest {

    private static final MarketParams IRON_SHEET = new MarketParams(8.06, 720.0, 0.80, GroupDef.metal());

    private static final double DAILY_DEMAND = 1800.0;
    private static final double TICK_DEMAND = DAILY_DEMAND / MarketRules.TRADING_TICKS_PER_DAY;

    @Test
    @DisplayName("Delivering exactly the expected amount leaves the deviation at zero")
    void exactDeliveryMeansNoDeviation() {
        MarketState state = MarketState.fresh(IRON_SHEET);

        for (int tick = 0; tick < 60; tick++) {
            double expected = TICK_DEMAND * 0.25;
            state.recordDelivery(expected);
            MarketRules.advanceTradingTick(state, expected, expected, TICK_DEMAND);
        }

        assertEquals(0.0, state.getDeviation(), 1e-9);
        assertEquals(state.getPriceLevel(), state.getDisplayedPrice(), 1e-9);
    }

    @Test
    @DisplayName("Dumping 300 extra units drops the deviation by exactly four percent")
    void dumpMatchesConceptExample() {
        MarketState state = MarketState.fresh(IRON_SHEET);

        state.recordDelivery(300.0);
        MarketRules.advanceTradingTick(state, 300.0, 0.0, TICK_DEMAND);

        assertEquals(-0.0400, state.getDeviation(), 0.0005);
    }

    @Test
    @DisplayName("A shortfall instead of a dump pushes the deviation up, not down")
    void shortfallPushesUp() {
        MarketState state = MarketState.fresh(IRON_SHEET);

        state.recordDelivery(50.0);
        MarketRules.advanceTradingTick(state, 50.0, 200.0, TICK_DEMAND);

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
    @DisplayName("Dumping is self-braking")
    void dumpingBrakesItself() {
        MarketState state = MarketState.fresh(IRON_SHEET);
        double fixedPrice = state.getDisplayedPrice();

        double shareBefore = MarketRules.share(fixedPrice,
                MarketParams.NEUTRAL_REPUTATION, state, IRON_SHEET);
        assertEquals(0.25, shareBefore, 0.001);

        state.recordDelivery(300.0);
        MarketRules.advanceTradingTick(state, 300.0, 0.0, TICK_DEMAND);

        double shareAfter = MarketRules.share(fixedPrice,
                MarketParams.NEUTRAL_REPUTATION, state, IRON_SHEET);

        assertTrue(shareAfter < shareBefore, "holding the price fixed after your own dump should cost you share");
        assertEquals(0.2242, shareAfter, 0.001);
    }

    @Test
    @DisplayName("The tick counter is cleared every tick, the daily counter is not")
    void countersHaveSeparateLifecycles() {
        MarketState state = MarketState.fresh(IRON_SHEET);
        state.recordDelivery(120.0);

        MarketRules.advanceTradingTick(state, 120.0, 100.0, TICK_DEMAND);

        assertEquals(0.0, state.getDeliveredThisTick(), 1e-9, "the tick counter should be cleared");
        assertEquals(120.0, state.getDeliveredToday(), 1e-9, "the daily counter should still hold everything delivered so far");
    }

    @Test
    @DisplayName("AdvanceDay never touches the deviation, only the structural price")
    void dailyRuleLeavesDeviationAlone() {
        MarketState state = MarketState.fresh(IRON_SHEET);
        state.setDeviation(-0.04);

        MarketRules.advanceDay(state, IRON_SHEET, DAILY_DEMAND);

        assertEquals(-0.04, state.getDeviation(), 1e-9);
    }
}
