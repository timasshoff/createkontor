package com.timder.kontor.core.macro;

public record MacroHistoryEntry(
        long day,
        double index,
        double policyRate
) {
}
