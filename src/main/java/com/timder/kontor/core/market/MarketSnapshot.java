package com.timder.kontor.core.market;

import com.timder.kontor.core.value.ItemId;

/**
 * A read-only snapshot of one market.
 * @param id Identity of the product this market trades
 * @param priceLevel The structural price of this product
 * @param deviation The current deviation from the structural price
 * @param displayedPrice The displayed price (structural price + deviation)
 * @param companies The amount of simulated concurrents in this market
 * @param visibleCompanies Rounded number of simulated concurrents
 * @param referenceCost The cost per unit of the concurrents
 * @param demand The current demand for this product
 * @param utilisation Utilisation of the concurrents
 * @param overflow Demand that could not be served
 */
public record MarketSnapshot(
        ItemId id,
        double priceLevel,
        double deviation,
        double displayedPrice,
        double companies,
        int visibleCompanies,
        double referenceCost,
        double demand,
        double utilisation,
        double overflow
) {
}
