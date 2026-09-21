package com.timder.kontor.core.company;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MoneyTest {
    @Test
    @DisplayName("Whole taler and cents describe the same amount")
    void wholeTalerAndCents() {
        assertEquals(Money.ofCents(100_000), Money.ofDollars(1_000));
        assertEquals(0, Money.ZERO.cents());
    }

    @Test
    @DisplayName("Converting from a double uses decimal rounding, half up")
    void fromTalerRoundsDecimal() {
        assertEquals(29, Money.fromDollar(0.285).cents());
        assertEquals(1, Money.fromDollar(0.005).cents());
        assertEquals(101, Money.fromDollar(1.005).cents());      // binary math would give 100
        assertEquals(268, Money.fromDollar(2.675).cents());      // binary math would give 267
        assertEquals(123_457, Money.fromDollar(1234.567).cents());
    }

    @Test
    @DisplayName("Rounding treats debts and credits the same way")
    void roundingIsSymmetric() {
        assertEquals(-1, Money.fromDollar(-0.005).cents());
        assertEquals(-100_641, Money.fromDollar(-1006.414).cents());
        assertEquals(-1, Money.ofCents(-1).scaled(0.5).cents());
        assertEquals(-3, Money.ofCents(-5).dividedBy(2).cents());
    }

    @Test
    @DisplayName("Not a number and infinity are rejected")
    void nonFiniteIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Money.fromDollar(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> Money.fromDollar(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> Money.ZERO.scaled(Double.NaN));
    }

    @Test
    @DisplayName("Scaling gives the interest examples of the concept")
    void scaledMatchesConceptExamples() {
        // K 17.3: 20,000 T at 0.4325 % is 86.50 T
        assertEquals(8_650, Money.ofDollars(20_000).scaled(0.004325).cents());
        // Founder loan of 5,000 T at 0.30 %: first interest 15.00 T
        assertEquals(1_500, Money.ofDollars(5_000).scaled(0.003).cents());
        // Overdraft of 860.00 T at 0.70 % is 6.02 T
        assertEquals(602, Money.ofDollars(860).scaled(0.007).cents());
        assertEquals(50, Money.ofDollars(1).scaled(0.5).cents());
        assertEquals(1, Money.ofCents(1).scaled(0.5).cents());
    }

    @Test
    @DisplayName("Dividing gives the founder loan rates, and the rates do not add up")
    void dividedByGivesRepaymentRates() {
        assertEquals(14_286, Money.ofDollars(3_000).dividedBy(21).cents());
        assertEquals(23_810, Money.ofDollars(5_000).dividedBy(21).cents());
        assertEquals(38_095, Money.ofDollars(8_000).dividedBy(21).cents());

        Money rate = Money.ofDollars(5_000).dividedBy(21);
        assertEquals(Money.ofCents(500_010), rate.multipliedBy(21), "21 rates overshoot by 0.10 T");
        assertEquals(3, Money.ofCents(5).dividedBy(2).cents());
    }

    @Test
    @DisplayName("Dividing by zero fails")
    void divideByZero() {
        assertThrows(ArithmeticException.class, () -> Money.ofDollars(1).dividedBy(0));
    }

    @Test
    @DisplayName("Adding, subtracting and comparing")
    void arithmetic() {
        Money a = Money.ofDollars(10);
        Money b = Money.fromDollar(2.50);

        assertEquals(Money.ofCents(1_250), a.plus(b));
        assertEquals(Money.ofCents(750), a.minus(b));
        assertEquals(Money.ofCents(-1_000), a.negate());
        assertEquals(a, a.negate().abs());
        assertTrue(a.compareTo(b) > 0);
        assertEquals(b, Money.min(a, b));
        assertEquals(a, Money.max(a, b));
        assertTrue(a.minus(a).isZero());
        assertTrue(b.minus(a).isNegative());
        assertTrue(a.isPositive());
    }

    @Test
    @DisplayName("The value never drifts, even after a thousand tiny bookings")
    void noDrift() {
        Money sum = Money.ZERO;
        for (int i = 0; i < 1_000; i++) {
            sum = sum.plus(Money.fromDollar(0.10));
        }
        assertEquals(Money.ofDollars(100), sum);
    }

    @Test
    @DisplayName("Overflow fails loudly instead of wrapping around")
    void overflowFails() {
        Money max = Money.ofCents(Long.MAX_VALUE);
        assertThrows(ArithmeticException.class, () -> max.plus(Money.ofCents(1)));
        assertThrows(ArithmeticException.class, () -> Money.ofCents(Long.MIN_VALUE).negate());
        assertThrows(ArithmeticException.class, () -> max.multipliedBy(2));
        assertThrows(ArithmeticException.class, () -> Money.ofDollars(Long.MAX_VALUE));
    }

    @Test
    @DisplayName("Converting back to taler for display")
    void toTaler() {
        assertEquals(1006.41, Money.ofCents(100_641).toDollars(), 1e-9);
        assertEquals(-0.05, Money.ofCents(-5).toDollars(), 1e-9);
    }
}
