package com.timder.kontor.core.company.logistics;

import java.util.Objects;
import java.util.UUID;

/**
 * @param networkId The id of the connected Create Logistics network
 * @param unbilledItemTicks Stored item ticks (items * ticks) that no storage booking has covered yet
 * @param lastSampleItems The amount of stored items at the last sample
 * @param lastSampleTick The economy tick of the last sample
 */
public record LogisticsState(UUID networkId, long unbilledItemTicks, long lastSampleItems, long lastSampleTick) {
    public static final long NO_SAMPLE = -1L;

    public LogisticsState {
        if (unbilledItemTicks < 0) throw new IllegalArgumentException("unbilledItemTicks must not be negative.");
        if (lastSampleItems < 0) throw new IllegalArgumentException("lastSampleItems must not be negative.");
        if (lastSampleTick < NO_SAMPLE)
            throw new IllegalArgumentException("lastSampleTick must be at least " + NO_SAMPLE + ".");

        if (lastSampleTick == NO_SAMPLE && (lastSampleItems != 0 || unbilledItemTicks != 0)) {
            throw new IllegalArgumentException("Without a sample there can be no sampled items and nothing unbilled.");
        }
        if (networkId == null && (lastSampleTick != NO_SAMPLE || unbilledItemTicks != 0)) {
            throw new IllegalArgumentException("A disconnected state must have no sample and nothing unbilled.");
        }
    }

    public boolean isConnected() {
        return networkId != null;
    }

    public boolean hasSample() {
        return lastSampleTick != NO_SAMPLE;
    }

    /**
     * @param network The id of the network
     * @return A connected state with fresh accounting and no sample
     */
    public static LogisticsState connectedTo(UUID network) {
        Objects.requireNonNull(network, "network must not be null.");
        return new LogisticsState(network, 0, 0, NO_SAMPLE);
    }

    /**
     * @return The state of a company that has no logistics connection
     */
    public static LogisticsState disconnected() {
        return new LogisticsState(null, 0, 0, NO_SAMPLE);
    }
}
