package com.timder.kontor.core.company;

import com.timder.kontor.core.company.financial.BookingKind;
import com.timder.kontor.core.company.financial.Money;

import java.util.Map;
import java.util.Objects;

public record CompanyHistoryEntry(
        long day,
        Money result,
        Money revenue,
        Map<BookingKind, Money> costsByKind
) {

    public CompanyHistoryEntry {
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");
        Objects.requireNonNull(result, "result must not be null.");
        Objects.requireNonNull(revenue, "revenue must not be null.");
        if (revenue.isNegative()) throw new IllegalArgumentException("revenue must not be negative.");
        costsByKind = Map.copyOf(costsByKind);
    }

}
