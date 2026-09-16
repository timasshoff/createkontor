package com.timder.kontor.core.macro;

/**
 * The four phases of the cycle.
 */
public enum Phase {
    BOOM,
    UPSWING,
    DOWNSWING,
    RECESSION;

    public boolean isRising() {
        return this == BOOM || this == UPSWING;
    }
}
