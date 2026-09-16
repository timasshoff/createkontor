package com.timder.kontor.core.market;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarketShareTest {

    private static final MarketParams IRON_SHEET = new MarketParams(
            8.06,
            720.0,
            0.80,
            GroupDef.metal()
    );

    private static final double DEMAND = 1800.0;
    private static final double NEUTRAL = MarketParams.NEUTRAL_REPUTATION;

    private DayResult tradeOneDay(MarketState state, double priceFactor, double stars) {
        double price = priceFactor * state.getDisplayedPrice();
        double share = MarketRules.share(price, stars, state, IRON_SHEET);
        state.recordDelivery(share * DEMAND);
        return MarketRules.advanceDay(state, IRON_SHEET, DEMAND);
    }

    @Test
    @DisplayName("Market price and three stars score exactly one")
    void neutralSupplierScoresOne() {
        MarketState state = MarketState.fresh(IRON_SHEET);
        double value = MarketRules.attractiveness(state.getDisplayedPrice(), NEUTRAL, state, IRON_SHEET);
        assertEquals(1.0, value, 1e-9);
    }

    @Test
    @DisplayName("A scandal lowers the attractiveness of the competition")
    void scandalWeakensCompetition() {
        MarketState state = MarketState.fresh(IRON_SHEET);
        double before = MarketRules.competitorAttractiveness(state, IRON_SHEET);
        state.setCompetitorReputation(1.5);
        assertTrue(MarketRules.competitorAttractiveness(state, IRON_SHEET) < before);
    }

    @Test
    @DisplayName("Three stars at market price against three companies gives a quarter")
    void neutralSupplierGetsAQuarter() {
        MarketState state = MarketState.fresh(IRON_SHEET);
        double share = MarketRules.share(state.getDisplayedPrice(), NEUTRAL, state, IRON_SHEET);
        assertEquals(0.25, share, 1e-9);
    }

    @Test
    @DisplayName("10 percent below market price raises the share to about a third")
    void undercuttingRaisesShare() {
        MarketState state = MarketState.fresh(IRON_SHEET);
        double share = MarketRules.share(0.90 * state.getDisplayedPrice(), NEUTRAL, state, IRON_SHEET);
        assertEquals(0.325, share, 0.005);
    }

    @Test
    @DisplayName("All shares of a market add up to one")
    void sharesAddUpToOne() {
        MarketState state = MarketState.fresh(IRON_SHEET);
        double market = state.getDisplayedPrice();

        double a = MarketRules.attractiveness(0.95 * market, 4.0, state, IRON_SHEET);
        double b = MarketRules.attractiveness(1.05 * market, 2.0, state, IRON_SHEET);

        double shareA = MarketRules.shareFrom(a, b, state, IRON_SHEET);
        double shareB = MarketRules.shareFrom(b, a, state, IRON_SHEET);
        double shareCompetition = MarketRules.competitorShare(a + b, state, IRON_SHEET);

        assertEquals(1.0, shareA + shareB + shareCompetition, 1e-9);
    }

    @Test
    @DisplayName("Undercutting by ten percent drives competitors out of the market")
    void priceWarPushesCompetitorsOut() {
        MarketState state = MarketState.fresh(IRON_SHEET);

        for (int day = 0; day < 25; day++) {
            tradeOneDay(state, 0.90, NEUTRAL);
        }

        assertTrue(state.getCompanies() < 1.5, "expected the competition to shrink, but it is " + state.getCompanies());
        assertTrue(MarketRules.share(0.90 * state.getDisplayedPrice(), NEUTRAL, state, IRON_SHEET) > 0.50, "expected the supplier to hold more than half the market");
    }
}
