package com.timder.kontor.core.company.employee;

import com.timder.kontor.core.company.financial.Money;
import java.util.Objects;

public record RoleSpec(RoleId id, Money baseSalary, int minLegalLevel) {
    public RoleSpec {
        Objects.requireNonNull(id, "id must not be null.");
        Objects.requireNonNull(baseSalary, "baseSalary must not be null.");
        if (baseSalary.isNegative()) throw new IllegalArgumentException("baseSalary must not be negative.");
        if (minLegalLevel < 1) throw new IllegalArgumentException("minLegalLevel must be at least 1.");
    }

    public RoleSpec(RoleId id, Money baseSalary) {
        this(id, baseSalary, 1);
    }

    public boolean isUnlockedAt(int legalLevel) {
        return legalLevel >= minLegalLevel;
    }
}
