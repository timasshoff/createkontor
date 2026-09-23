package com.timder.kontor.core.company;

import com.timder.kontor.core.company.request.RequestParams;
import com.timder.kontor.core.company.request.RequestRules;
import com.timder.kontor.core.port.Rng;
import com.timder.kontor.core.port.SeededRng;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestRulesTest {
    private static final RequestParams PARAMS = RequestParams.standard();

    @Test
    @DisplayName("Average request size matches iron_sheet, all four legal form stages")
    void averageRequestSizeMatchesIronPlateTable() {
        assertEquals(39.2, RequestRules.averageRequestSize(16, 64, PARAMS), 0.05);
        assertEquals(54.6, RequestRules.averageRequestSize(16, 256, PARAMS), 0.05);
        assertEquals(54.6, RequestRules.averageRequestSize(16, 1024, PARAMS), 0.05);
        assertEquals(54.6, RequestRules.averageRequestSize(16, 4096, PARAMS), 0.05);
    }

    @Test
    @DisplayName("Rate at list price = market price matches requests per day")
    void rateMatchesK1210Example() {
        double lambda = RequestRules.rate(450, 0, 0, 54.6, 9.03, 9.03, PARAMS);
        assertEquals(8.2418, lambda * RequestRules.TICKS_PER_DAY, 0.001);
    }

    @Test
    @DisplayName("Walk-in floor does NOT apply when the price is far above market")
    void walkInFloorDoesNotApplyFarAboveMarket() {
        double lambda = RequestRules.rate(10, 0, 0, 54.6, 20.0, 9.03, PARAMS);
        double rawLambda = 10.0 / (54.6 * RequestRules.TICKS_PER_DAY);
        assertEquals(rawLambda, lambda, 1e-12);
        assertTrue(lambda < 1.0 / RequestRules.TICKS_PER_DAY);
    }

    @Test
    @DisplayName("Framework contract quantity reduces the effective demand behind the rate")
    void frameworkContractQuantityReducesRate() {
        double withoutRv = RequestRules.rate(450, 0, 0, 54.6, 9.03, 9.03, PARAMS);
        double withRv = RequestRules.rate(450, 100, 0, 54.6, 9.03, 9.03, PARAMS);
        assertTrue(withRv < withoutRv);
        assertEquals(350.0 / (54.6 * RequestRules.TICKS_PER_DAY), withRv, 1e-9);
    }

    @Test
    @DisplayName("Countdown averages out to about 1/lambda over many draws")
    void countdownAveragesToExpectedInterval() {
        double lambda = 0.0003434066;
        Rng rng = new SeededRng(42);
        long sum = 0;
        int n = 50_000;
        for (int i = 0; i < n; i++) {
            sum += RequestRules.drawCountdown(lambda, rng);
        }
        double mean = sum / (double) n;
        double expected = 1.0 / lambda;
        assertEquals(expected, mean, expected * 0.03);
    }

    @Test
    @DisplayName("Countdown is always at least 1 tick")
    void countdownNeverZero() {
        Rng rng = new SeededRng(7);
        for (int i = 0; i < 10_000; i++) {
            assertTrue(RequestRules.drawCountdown(0.01, rng) >= 1);
        }
    }

    @Test
    @DisplayName("Deadline for a non-urgent order matches")
    void deadlineNonUrgent() {
        double tX = RequestRules.manufacturingTicksPerUnit(2);
        long deadline = RequestRules.deadlineTicks(32, 0.0, tX, 1.0, PARAMS);
        assertEquals(9651, deadline);
    }

    @Test
    @DisplayName("Deadline shrinks with urgency")
    void deadlineUrgent() {
        double tX = RequestRules.manufacturingTicksPerUnit(2);
        long deadline = RequestRules.deadlineTicks(32, 0.5, tX, 1.0, PARAMS);
        assertEquals(6756, deadline);
    }

    @Test
    @DisplayName("Unit price with urgency surcharge and a moderate quantity discount")
    void unitPriceMatchesReference() {
        double price = RequestRules.unitPrice(9.03, 0.5, 3, PARAMS);
        assertEquals(10.2807, price, 0.0001);
    }

    @Test
    @DisplayName("Unit price at the maximum k hits the quantity discount floor")
    void unitPriceHitsDiscountFloor() {
        double price = RequestRules.unitPrice(9.03, 0.0, 16, PARAMS);
        assertEquals(8.5785, price, 0.0001);
    }

    @Test
    @DisplayName("Offer duration shrinks with urgency and stretches with a sales rep")
    void offerDurationMatchesReference() {
        assertEquals(4800, RequestRules.offerDurationTicks(0.4, 1.0, PARAMS));
        assertEquals(7200, RequestRules.offerDurationTicks(0.4, 1.5, PARAMS));
    }
}

