package com.timder.kontor.core.raw;

import com.timder.kontor.core.value.ItemId;

/**
 * A read-only snapshot of one raw materials price.
 * @param id The identity of this raw material
 * @param price The current price
 */
public record RawMaterialSnapshot(
        ItemId id,
        double price
) {
}
