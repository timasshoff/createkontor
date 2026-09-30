package com.timder.kontor.core.company.license;

import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.value.ItemId;

import java.util.*;

public record LicenseOffer(
        LicenseDef license,
        Money dailyFee,
        Money applicationFee,
        Set<ItemId> alreadyCovered,
        Optional<Money> lockedDailyFee,
        AcquireResult.Status status
) {
    public LicenseOffer {
        Objects.requireNonNull(license, "license must not be null.");
        Objects.requireNonNull(dailyFee, "dailyFee must not be null.");
        Objects.requireNonNull(applicationFee, "applicationFee must not be null.");
        Objects.requireNonNull(alreadyCovered, "alreadyCovered must not be null.");
        Objects.requireNonNull(lockedDailyFee, "lockedDailyFee must not be null.");
        Objects.requireNonNull(status, "status must not be null.");
        alreadyCovered = Collections.unmodifiableSet(new LinkedHashSet<>(alreadyCovered));
        if (!license.markets().containsAll(alreadyCovered)) {
            throw new IllegalArgumentException("alreadyCovered must only hold markets of the license.");
        }
    }

    /**
     * @return True if buying the license would work right now
     */
    public boolean purchasable() {
        return status == AcquireResult.Status.ACQUIRED || status == AcquireResult.Status.REACTIVATED;
    }

    /**
     * @return True if every market of the license is already covered by an active license of the company
     */
    public boolean coversNothingNew() {
        return alreadyCovered.size() == license.markets().size();
    }
}
