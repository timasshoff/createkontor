package com.timder.kontor.core.company.license;

import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.value.ItemId;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record HeldLicense(LicenseHolding holding, Optional<LicenseDef> definition) {
    public HeldLicense {
        Objects.requireNonNull(holding, "holding must not be null.");
        Objects.requireNonNull(definition, "definition must not be null.");
        if (definition.isPresent() && !definition.get().key().equals(holding.key())) {
            throw new IllegalArgumentException("The definition is not the one of the holding.");
        }
    }

    public LicenseKey key() {
        return holding.key();
    }

    public long acquiredDay() {
        return holding.acquiredDay();
    }

    public Money dailyFee() {
        return holding.dailyFee();
    }

    public boolean active() {
        return holding.isActive();
    }

    public boolean cancelled() {
        return holding.cancelled();
    }

    /**
     * @return true if the catalog has a definition for this license
     */
    public boolean known() {
        return definition.isPresent();
    }

    public boolean billed() {
        return known() && holding.dailyFee().isPositive();
    }

    /**
     * @return The markets this license covers
     */
    public Set<ItemId> markets() {
        return definition.map(LicenseDef::markets).orElse(Set.of());
    }

    public boolean hasRevenueShare() {
        return definition.map(LicenseDef::hasRevenueShare).orElse(false);
    }
}
