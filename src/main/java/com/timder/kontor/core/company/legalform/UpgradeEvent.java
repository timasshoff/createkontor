package com.timder.kontor.core.company.legalform;

import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.financial.Money;

import java.util.Objects;

public sealed interface UpgradeEvent {

    CompanyId companyId();

    record Completed(CompanyId companyId, int newLevel) implements UpgradeEvent {
        public Completed {
            Objects.requireNonNull(companyId, "companyId must not be null.");
            if (newLevel < 2) throw new IllegalArgumentException("newLevel must be at least 2.");
        }
    }

    record Resting(CompanyId companyId, int targetLevel, int restingDays) implements UpgradeEvent {
        public Resting {
            Objects.requireNonNull(companyId, "companyId must not be null.");
            if (targetLevel < 2) throw new IllegalArgumentException("targetLevel must be at least 2.");
            if (restingDays < 1) throw new IllegalArgumentException("restingDays must be at least 1.");
        }
    }

    record Rejected(CompanyId companyId, int targetLevel, Money feeLost) implements UpgradeEvent {
        public Rejected {
            Objects.requireNonNull(companyId, "companyId must not be null.");
            Objects.requireNonNull(feeLost, "feeLost must not be null.");
            if (targetLevel < 2) throw new IllegalArgumentException("targetLevel must be at least 2.");
        }
    }
}
