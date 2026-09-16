package com.timder.kontor.core.economy.event;

import com.timder.kontor.core.value.ItemId;

/**
 * A competitor exited the market.
 * @param market The market
 */
public record CompetitorExitedEvent(ItemId market) implements EconomyEvent {
}
