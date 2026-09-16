package com.timder.kontor.core.economy.event;

/**
 * An event that the economy thought was worth actively reporting.
 */
public sealed interface EconomyEvent permits CompetitorEnteredEvent, CompetitorExitedEvent {
}
