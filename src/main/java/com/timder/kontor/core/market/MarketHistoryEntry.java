package com.timder.kontor.core.market;

import com.timder.kontor.core.value.ItemId;

/**
 * One trading tick worth of history for a market
 */
public record MarketHistoryEntry(
        ItemId id,
        long tick,
        long day,
        double priceLevel,
        double deviation,
        double displayedPrice,
        double competitors,
        double deliveredThisTick,
        double demand,
        double overflow
) {
    public MarketHistoryEntry withOverflow(double overflow) {
        return new MarketHistoryEntry(id, tick, day, priceLevel, deviation, displayedPrice, competitors, deliveredThisTick, demand, overflow);
    }
}
