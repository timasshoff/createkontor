package com.timder.kontor.core.economy.event;

import com.timder.kontor.core.value.ItemId;

/**
 * A new competitor entered the market.
 * @param market The market
 */
public record CompetitorEnteredEvent(ItemId market) implements EconomyEvent {
}
