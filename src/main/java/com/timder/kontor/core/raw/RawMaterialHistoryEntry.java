package com.timder.kontor.core.raw;

import com.timder.kontor.core.value.ItemId;

/**
 * One recorded state of raw material history
 * @param id
 * @param day
 * @param price
 */
public record RawMaterialHistoryEntry(
        ItemId id,
        long day,
        double price
) {
}
