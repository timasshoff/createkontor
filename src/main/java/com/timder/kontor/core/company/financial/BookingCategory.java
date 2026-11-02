package com.timder.kontor.core.company.financial;

/**
 * How a booking result counts in day results, statistics etc.
 */
public enum BookingCategory {
    /**
     * Reduces the result of a day.
     */
    COST,

    /**
     * Increases the result of a day.
     */
    REVENUE,

    /**
     * Financing (e.g. loans). Neither cost nor revenue.
     */
    FINANCING,

    /**
     * Purchasing / Selling of and asset. Neither cost nor revenue.
     */
    INVESTMENT
}
