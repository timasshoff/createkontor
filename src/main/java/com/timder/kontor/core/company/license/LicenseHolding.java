package com.timder.kontor.core.company.license;

import com.timder.kontor.core.company.financial.Money;

import java.util.Objects;

public record LicenseHolding(LicenseKey key, long acquiredDay, Money dailyFee, boolean cancelled) {
    public LicenseHolding {
        Objects.requireNonNull(key, "key must not be null.");
        if (acquiredDay < 0) throw new IllegalArgumentException("acquiredDay must not be negative.");
    }

    /**
     * @param key The bought license
     * @param day The day of the purchase
     * @return A new, active holding
     */
    public static LicenseHolding acquire(LicenseKey key, long day, Money dailyFee) {
        return new LicenseHolding(key, day, dailyFee, false);
    }

    public boolean isActive() {
        return !cancelled;
    }

    /**
     * @return The same holding, cancelled
     */
    public LicenseHolding cancel() {
        return new LicenseHolding(key, acquiredDay, dailyFee, true);
    }

    /**
     * @return The same holding, active again
     */
    public LicenseHolding reactivate() {
        return new LicenseHolding(key, acquiredDay, dailyFee, false);
    }
}
