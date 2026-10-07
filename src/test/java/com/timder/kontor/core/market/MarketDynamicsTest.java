package com.timder.kontor.core.market;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*
The following code is AI generated
 */
public class MarketDynamicsTest {
    private static final MarketParams IRON_SHEET = new MarketParams(8.06, 720.0, 0.80, GroupDef.metal());

    private static final double DAILY_DEMAND = 1800.0;
    private static final double TICK_DEMAND = DAILY_DEMAND / MarketRules.TRADING_TICKS_PER_DAY; // 150

    private static MarketDynamicsParams withPurchaseImpact(double impact) {
        MarketDynamicsParams d = MarketDynamicsParams.standard();
        return new MarketDynamicsParams(d.deviationDecay(), d.deviationStrength(), d.deviationBound(), impact,
                d.maxPriceFactor(), d.minCompetitors(), d.maxCompetitors());
    }

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
        assertEquals(1.0, standard.purchaseImpact(), 1e-12);
        assertEquals(2.0, standard.maxPriceFactor(), 1e-12);
        assertEquals(1.0, standard.minCompetitors(), 1e-12);
        assertEquals(8.0, standard.maxCompetitors(), 1e-12);
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

    @Test
    @DisplayName("With purchase impact 2.5, buying 150 units raises the deviation by five percent")
    void purchaseWithHigherWeight() {
        assertEquals(0.05, deviationAfterPurchase(150.0, withPurchaseImpact(2.5)), 1e-12);
    }

    @Test
    @DisplayName("With purchase impact 0, a purchase does not move the deviation at all")
    void purchaseWithZeroWeight() {
        assertEquals(0.0, deviationAfterPurchase(150.0, withPurchaseImpact(0.0)), 1e-12);
    }

    @Test
    @DisplayName("The purchase impact does not weigh deliveries")
    void purchaseImpactLeavesDeliveriesAlone() {
        MarketState state = MarketState.fresh(IRON_SHEET);

        MarketRules.advanceTradingTick(state, 300.0, 0.0, 0.0, TICK_DEMAND, withPurchaseImpact(2.5));

        assertEquals(-0.04, state.getDeviation(), 1e-12);
    }

    @Test
    @DisplayName("A huge purchase is capped by the deviation bound")
    void purchaseIsClamped() {
        assertEquals(0.5, deviationAfterPurchase(10000.0, withPurchaseImpact(2.5)), 1e-12);
    }

    @Test
    @DisplayName("After a purchase of 150 units at weight 2.5, an empty tick decays 0.05 to 0.047")
    void deviationDecaysAfterPurchase() {
        MarketDynamicsParams dynamics = withPurchaseImpact(2.5);
        MarketState state = MarketState.fresh(IRON_SHEET);

        MarketRules.advanceTradingTick(state, 0.0, 150.0, 0.0, TICK_DEMAND, dynamics);
        MarketRules.advanceTradingTick(state, 0.0, 0.0, 0.0, TICK_DEMAND, dynamics);

        assertEquals(0.047, state.getDeviation(), 1e-12);
    }

    @Test
    @DisplayName("Decay, strength and bound are taken from the parameters")
    void deviationConstantsAreConfigurable() {
        MarketDynamicsParams custom = new MarketDynamicsParams(0.5, 0.1, 0.2, 1.0, 2.0, 1.0, 8.0);
        MarketState state = MarketState.fresh(IRON_SHEET);
        state.setDeviation(0.1);

        // 0.5 * 0.1 - 0.1 * 30 / 150 = 0.03
        MarketRules.advanceTradingTick(state, 30.0, 0.0, 0.0, TICK_DEMAND, custom);
        assertEquals(0.03, state.getDeviation(), 1e-12);

        // 0.5 * 0.03 - 0.1 * (-3000) / 150 = 2.015 -> clamped to the bound
        MarketRules.advanceTradingTick(state, -3000.0, 0.0, 0.0, TICK_DEMAND, custom);
        assertEquals(0.2, state.getDeviation(), 1e-12);
    }

    @Test
    @DisplayName("The ceiling of the price level is taken from the parameters")
    void priceCeilingIsConfigurable() {
        MarketDynamicsParams d = MarketDynamicsParams.standard();
        MarketDynamicsParams lowCeiling = new MarketDynamicsParams(d.deviationDecay(), d.deviationStrength(),
                d.deviationBound(), d.purchaseImpact(), 1.5, d.minCompetitors(), d.maxCompetitors());

        // One competitor, demand 1800 against capacity 720: full utilisation.
        MarketState standard = new MarketState(15.0, 1.0);
        MarketRules.advanceDay(standard, IRON_SHEET, DAILY_DEMAND);
        assertEquals(15.3, standard.getPriceLevel(), 1e-9);

        MarketState capped = new MarketState(15.0, 1.0);
        MarketRules.advanceDay(capped, IRON_SHEET, DAILY_DEMAND, lowCeiling);
        assertEquals(8.06 * 1.5, capped.getPriceLevel(), 1e-9);
    }

    @Test
    @DisplayName("The limits of the competitor count are taken from the parameters")
    void competitorLimitsAreConfigurable() {
        MarketDynamicsParams d = MarketDynamicsParams.standard();

        // Profitable market: three competitors would grow to 3.15.
        MarketState growing = new MarketState(12.0, 3.0);
        MarketRules.advanceDay(growing, IRON_SHEET, DAILY_DEMAND);
        assertEquals(3.15, growing.getCompetitors(), 1e-9);

        MarketDynamicsParams maxThree = new MarketDynamicsParams(d.deviationDecay(), d.deviationStrength(),
                d.deviationBound(), d.purchaseImpact(), d.maxPriceFactor(), 1.0, 3.0);
        MarketState capped = new MarketState(12.0, 3.0);
        MarketRules.advanceDay(capped, IRON_SHEET, DAILY_DEMAND, maxThree);
        assertEquals(3.0, capped.getCompetitors(), 1e-9);

        // Unprofitable market: three competitors would shrink to 2.85.
        MarketState shrinking = new MarketState(8.06, 3.0);
        MarketRules.advanceDay(shrinking, IRON_SHEET, 100.0);
        assertEquals(2.85, shrinking.getCompetitors(), 1e-9);

        MarketDynamicsParams minThree = new MarketDynamicsParams(d.deviationDecay(), d.deviationStrength(),
                d.deviationBound(), d.purchaseImpact(), d.maxPriceFactor(), 3.0, 8.0);
        MarketState floored = new MarketState(8.06, 3.0);
        MarketRules.advanceDay(floored, IRON_SHEET, 100.0, minThree);
        assertEquals(3.0, floored.getCompetitors(), 1e-9);
    }

    @Test
    @DisplayName("Invalid settings are rejected")
    void invalidValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new MarketDynamicsParams(1.0, 0.02, 0.5, 1.0, 2.0, 1.0, 8.0));
        assertThrows(IllegalArgumentException.class, () -> new MarketDynamicsParams(0.94, -0.02, 0.5, 1.0, 2.0, 1.0, 8.0));
        assertThrows(IllegalArgumentException.class, () -> new MarketDynamicsParams(0.94, 0.02, 1.0, 1.0, 2.0, 1.0, 8.0));
        assertThrows(IllegalArgumentException.class, () -> new MarketDynamicsParams(0.94, 0.02, 0.5, -1.0, 2.0, 1.0, 8.0));
        assertThrows(IllegalArgumentException.class, () -> new MarketDynamicsParams(0.94, 0.02, 0.5, 1.0, 1.0, 1.0, 8.0));
        assertThrows(IllegalArgumentException.class, () -> new MarketDynamicsParams(0.94, 0.02, 0.5, 1.0, 2.0, 0.0, 8.0));
        assertThrows(IllegalArgumentException.class, () -> new MarketDynamicsParams(0.94, 0.02, 0.5, 1.0, 2.0, 5.0, 4.0));
    }
}
