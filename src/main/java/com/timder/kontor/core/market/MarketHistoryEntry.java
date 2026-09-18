package com.timder.kontor.core.market;

import com.timder.kontor.core.value.ItemId;

/**
 * One closed day worth of history for a market
 */
public record MarketHistoryEntry(
        ItemId id,
        long tick,
        long day,
        double priceLevel,
        double deviation,
        double displayedPrice,
        double companies,
        double deliveredThisTick
) { }
