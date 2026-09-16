package com.timder.kontor.core.raw;

import com.timder.kontor.core.port.Rng;
import com.timder.kontor.core.port.SeededRng;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RawMaterialRulesTest {

    private static final RawMaterialParams IRON = new RawMaterialParams(6.20, 0.025, 3000.0);
    private static final PriceProcessParams PROCESS = PriceProcessParams.standard();

    private static List<Double> runPrice(long seed, int days, double purchasePerDay) {
        Rng rng = new SeededRng(seed);
        RawMaterialState state = RawMaterialState.fresh(IRON);
        List<Double> prices = new ArrayList<>(days);
        for (int day = 0; day < days; day++) {
            if (purchasePerDay > 0) {
                state.recordPurchase(purchasePerDay);
            }
            RawMaterialRules.advanceDay(state, IRON, PROCESS, rng);
            prices.add(state.getPrice());
        }
        return prices;
    }

    private Rng zeroNoise() {
        return new Rng() {
            public double nextDouble() { return 0.0; }
            public double nextGaussian() { return 0.0; }
        };
    }

    @Test
    @DisplayName("Price wanders noticeably around its base value without a shock")
     void wandersAroundBaseValue() {
        List<Double> prices = runPrice(42, 300, 0.0);
        double min = prices.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
        double max = prices.stream().mapToDouble(Double::doubleValue).max().orElseThrow();

        assertTrue(min < IRON.baseValue() * 0.95, "expected a real dip, lowest was " + min);
        assertTrue(max > IRON.baseValue() * 1.05, "expected a real peak, highest was " + max);
    }

    @Test
    @DisplayName("Price never leaves 0.3 to 4 times the base value, over 500 days and ten seeds")
    void staysWithinBounds() {
        for (long seed = 0; seed < 10; seed++) {
            for (double price : runPrice(seed, 500, 0.0)) {
                assertTrue(price >= 0.3 * IRON.baseValue() - 1e-9, "price " + price + " below the floor for seed " + seed);
                assertTrue(price <= 4.0 * IRON.baseValue() + 1e-9, "price " + price + " above the ceiling for seed " + seed);
            }
        }
    }

    @Test
    @DisplayName("Half of a deviation caused by a shock is gone after about five days")
    void halfLifeIsAboutFiveDays() {
        RawMaterialState state = new RawMaterialState(IRON.baseValue() * 1.6);
        Rng noNoise = zeroNoise();
        double deviation = state.getPrice() - IRON.baseValue();

        for (int day = 0; day < 5; day++) {
            RawMaterialRules.advanceDay(state, IRON, PROCESS, noNoise);
        }

        double remaining = state.getPrice() - IRON.baseValue();
        assertTrue(remaining < deviation * 0.5,
                "expected less than half the deviation left, got " + remaining + " out of " + deviation);
        assertTrue(remaining > deviation * 0.15, "reversion looks too fast, only " + remaining + " left out of " + deviation);
    }

    @Test
    @DisplayName("The purchase counter is cleared after each day")
    void purchaseCounterIsCleared() {
        RawMaterialState state = RawMaterialState.fresh(IRON);
        state.recordPurchase(500);

        RawMaterialRules.advanceDay(state, IRON, PROCESS, new SeededRng(1));

        assertEquals(0.0, state.getPurchasedToday());
    }

    @Test
    @DisplayName("Buying every day for a while lifts the price noticeably above the base value")
    void sustainedBuyingLiftsPrice() {
        List<Double> withoutBuying = runPrice(5, 60, 0.0);
        List<Double> withBuying = runPrice(5, 60, IRON.referenceVolume() * 0.4);

        assertTrue(withBuying.get(59) > withoutBuying.get(59) * 1.10, "sustained buying should leave the price clearly higher");
    }

    @Test
    @DisplayName("Same seed produces exactly the same history")
    void sameSeedSameHistory() {
        assertEquals(runPrice(7, 300, 0.0), runPrice(7, 300, 0.0));
    }
}
