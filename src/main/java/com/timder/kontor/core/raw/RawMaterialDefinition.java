package com.timder.kontor.core.raw;

import com.timder.kontor.core.value.ItemId;

/**
 * Ties an item / id to the parameters of this raw material.
 * @param id Identity of this raw material
 * @param params Parameters of this raw material
 */
public record RawMaterialDefinition(
        ItemId id,
        RawMaterialParams params
) {

    public RawMaterialDefinition {
        if (id == null) throw new IllegalArgumentException("id must not be null.");
        if (params == null) throw new IllegalArgumentException("params must not be null.");
    }

}
