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
     * Neither cost nor revenue
     */
    FINANCING
}
