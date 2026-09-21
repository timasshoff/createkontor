package com.timder.kontor.core.company;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * An amount of money in whole hundredths of a dollar (cents).
 * @param cents The amount of money in cents
 */
public record Money(long cents) implements Comparable<Money> {

    public static final Money ZERO = new Money(0L);

    /**
     * @param cents The amount of money in cents
     * @return The money
     */
    public static Money ofCents(long cents) {
        return new Money(cents);
    }

    /**
     * @param dollar The amount of money in dollars
     * @return The money
     */
    public static Money ofDollars(long dollar) {
        return new Money(Math.multiplyExact(dollar, 100L));
    }

    /**
     * Converts a decimal amount of dollars and rounds it to 0.01$
     * @param dollar The amount in dollars
     * @return The money
     */
    public static Money fromDollar(double dollar) {
        BigDecimal exact = BigDecimal.valueOf(dollar);
        return new Money(exact.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact());
    }

    public Money plus(Money other) {
        return new Money(Math.addExact(cents, other.cents));
    }

    public Money minus(Money other) {
        return new Money(Math.subtractExact(cents, other.cents));
    }

    public Money negate() {
        return new Money(Math.negateExact(cents));
    }

    public Money abs() {
        return cents < 0 ? negate() : this;
    }

    /**
     * Exact multiplication with no rounding.
     * @param factor A whole factor
     * @return The multiplied money
     */
    public Money multipliedBy(long factor) {
        return new Money(Math.multiplyExact(cents, factor));
    }

    /**
     * Multiplies with a decimal factor and rounds result to 0.01$.
     * @param factor A decimal factor
     * @return The multiplied money
     */
    public Money scaled(double factor) {
        BigDecimal exact = BigDecimal.valueOf(cents).multiply(BigDecimal.valueOf(factor));
        return new Money(exact.setScale(0, RoundingMode.HALF_UP).longValueExact());
    }

    public Money dividedBy(long divisor) {
        BigDecimal exact = BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(divisor), 0, RoundingMode.HALF_UP);
        return new Money(exact.longValueExact());
    }

    public boolean isNegative() {
        return cents < 0;
    }

    public boolean isPositive() {
        return cents > 0;
    }

    public boolean isZero() {
        return cents == 0;
    }

    /**
     * The amount of money as full dollars
     * @return The money as double
     */
    public double toDollars() {
        return cents / 100.0;
    }

    public static Money min(Money a, Money b) {
        return a.cents <= b.cents ? a : b;
    }

    public static Money max(Money a, Money b) {
        return a.cents >= b.cents ? a : b;
    }

    @Override
    public int compareTo(Money other) {
        return Long.compare(cents, other.cents);
    }

    @Override
    public String toString() {
        return BigDecimal.valueOf(cents, 2).toPlainString() + "$";
    }
}
