package com.timder.kontor.core.market;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/*
The following code is AI generated
 */
public class MarketDynamicsTest {
    private static final MarketParams IRON_SHEET = new MarketParams(8.06, 720.0, 0.80, GroupDef.metal());

    private static final double DAILY_DEMAND = 1800.0;
    private static final double TICK_DEMAND = DAILY_DEMAND / MarketRules.TRADING_TICKS_PER_DAY; // 150

    private static double deviationAfterPurchase(double purchased, MarketDynamicsParams dynamics) {
        MarketState state = MarketState.fresh(IRON_SHEET);
        state.recordPurchase(purchased);
        MarketRules.advanceTradingTick(state, 0.0, state.getPurchasedThisTick(), 0.0, TICK_DEMAND, dynamics);
        return state.getDeviation();
    }

    @Test
    @DisplayName("The standard values are the ones the simulation was balanced with")
    void standardValuesAreTheOldConstants() {
        MarketDynamicsParams standard = MarketDynamicsParams.standard();

        assertEquals(0.94, standard.deviationDecay(), 1e-12);
        assertEquals(0.02, standard.deviationStrength(), 1e-12);
        assertEquals(0.5, standard.deviationBound(), 1e-12);
        assertEquals(2.0, standard.maxPriceFactor(), 1e-12);
        assertEquals(1.0, standard.minCompetitors(), 1e-12);
        assertEquals(8.0, standard.maxCompetitors(), 1e-12);
        assertEquals(1.0, standard.costPassThrough(), 1e-12);
        assertEquals(0.0, standard.costPassThroughDown(), 1e-12);
    }

    @Test
    @DisplayName("The overloads without parameters behave like the standard values")
    void oldOverloadUsesStandard() {
        MarketState state = MarketState.fresh(IRON_SHEET);

        MarketRules.advanceTradingTick(state, 300.0, 0.0, TICK_DEMAND);

        assertEquals(-0.04, state.getDeviation(), 1e-12);
    }

    @Test
    @DisplayName("With the standard weight, buying 150 units raises the deviation by two percent")
    void purchaseWithStandardWeight() {
        assertEquals(0.02, deviationAfterPurchase(150.0, MarketDynamicsParams.standard()), 1e-12);
    }

    /**
     * Pass-through for rises only, falling costs are ignored
     */
    private static MarketDynamicsParams withPassThrough(double up) {
        return withPassThrough(up, 0.0);
    }

    private static MarketDynamicsParams withPassThrough(double up, double down) {
        MarketDynamicsParams d = MarketDynamicsParams.standard();
        return new MarketDynamicsParams(
                d.deviationDecay(),
                d.deviationStrength(),
                d.deviationBound(),
                d.maxPriceFactor(),
                d.minCompetitors(),
                d.maxCompetitors(),
                up,
                down,
                d.trustThreshold(),
                d.trustWidth(),
                d.maxShare());
    }

    @Test
    @DisplayName("A cost rise of two percent raises the price level by two percent and keeps the margin")
    void fullPassThroughKeepsTheMargin() {
        MarketState state = MarketState.fresh(IRON_SHEET); // price level 8.06 * 1.12 = 9.0272
        double newCost = 8.06 * 1.02;

        MarketRules.passCostChange(state, 8.06, newCost, withPassThrough(1.0));

        assertEquals(9.207744, state.getPriceLevel(), 1e-9);
        assertEquals(0.12, (state.getPriceLevel() - newCost) / newCost, 1e-9);
    }

    @Test
    @DisplayName("Half pass-through passes on half of the rise")
    void halfPassThrough() {
        MarketState state = MarketState.fresh(IRON_SHEET);

        MarketRules.passCostChange(state, 8.06, 8.06 * 1.02, withPassThrough(0.5));

        assertEquals(9.117472, state.getPriceLevel(), 1e-9);
    }

    @Test
    @DisplayName("With pass-through 0 the price level stays")
    void noPassThrough() {
        MarketState state = MarketState.fresh(IRON_SHEET);

        MarketRules.passCostChange(state, 8.06, 8.06 * 1.02, withPassThrough(0.0));

        assertEquals(9.0272, state.getPriceLevel(), 1e-9);
    }

    @Test
    @DisplayName("A falling cost is not passed on when the down factor is 0")
    void fallingCostIsIgnored() {
        MarketState state = MarketState.fresh(IRON_SHEET);

        MarketRules.passCostChange(state, 8.06, 7.9, withPassThrough(1.0));

        assertEquals(9.0272, state.getPriceLevel(), 1e-9);
    }

    @Test
    @DisplayName("An unchanged cost leaves the price level alone")
    void unchangedCostIsIgnored() {
        MarketState state = MarketState.fresh(IRON_SHEET);

        MarketRules.passCostChange(state, 8.06, 8.06, withPassThrough(1.0));

        assertEquals(9.0272, state.getPriceLevel(), 1e-9);
    }

    @Test
    @DisplayName("A cost rise from 10 to 12 at a price of 20 and half pass-through gives 22")
    void ratioIsRelativeToThePreviousCost() {
        MarketState state = new MarketState(20.0, 3.0);

        MarketRules.passCostChange(state, 10.0, 12.0, withPassThrough(0.5));

        assertEquals(22.0, state.getPriceLevel(), 1e-9);
    }

    @Test
    @DisplayName("A falling cost is passed on with its own factor")
    void fallingCostUsesTheDownFactor() {
        MarketState state = MarketState.fresh(IRON_SHEET); // 9.0272

        MarketRules.passCostChange(state, 8.06, 7.9, withPassThrough(0.6, 0.3));

        assertEquals(8.97344, state.getPriceLevel(), 1e-9);
    }

    @Test
    @DisplayName("Full pass-through downwards follows the cost ratio")
    void fullDownPassThrough() {
        MarketState state = MarketState.fresh(IRON_SHEET);

        MarketRules.passCostChange(state, 8.06, 7.9, withPassThrough(0.6, 1.0));

        assertEquals(8.848, state.getPriceLevel(), 1e-9);
    }

    @Test
    @DisplayName("The down factor does not affect a rise")
    void downFactorDoesNotAffectRises() {
        MarketState state = MarketState.fresh(IRON_SHEET);

        MarketRules.passCostChange(state, 8.06, 8.06 * 1.02, withPassThrough(0.6, 0.3));

        assertEquals(9.1355264, state.getPriceLevel(), 1e-9);
    }

    @Test
    @DisplayName("A cost fall from 10 to 8 at a price of 20 and half pass-through gives 18")
    void fallRatioIsRelativeToThePreviousCost() {
        MarketState state = new MarketState(20.0, 3.0);

        MarketRules.passCostChange(state, 10.0, 8.0, withPassThrough(0.6, 0.5));

        assertEquals(18.0, state.getPriceLevel(), 1e-9);
    }

    @Test
    @DisplayName("A cost of zero is ignored")
    void zeroCostIsIgnored() {
        MarketState state = MarketState.fresh(IRON_SHEET);

        MarketRules.passCostChange(state, 8.06, 0.0, withPassThrough(1.0, 1.0));

        assertEquals(9.0272, state.getPriceLevel(), 1e-9);
    }
}
