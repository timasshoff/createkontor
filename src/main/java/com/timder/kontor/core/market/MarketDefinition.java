package com.timder.kontor.core.market;

import com.timder.kontor.core.value.ItemId;

/**
 * Ties an item / id to the parameters of a market and the base demand.
 * @param id Identity of the product this market trades
 * @param params Parameters of this market
 * @param baseDemand Base demand before macroeconomic cycle and trend
 */
public record MarketDefinition(
        ItemId id,
        MarketParams params,
        double baseDemand
) {

    public MarketDefinition {
        if (id == null) throw new IllegalArgumentException("id must not be null.");
        if (params == null) throw new IllegalArgumentException("params must not be null.");
        if (baseDemand <= 0) throw new IllegalArgumentException("baseDemand must be positive.");
    }

}
