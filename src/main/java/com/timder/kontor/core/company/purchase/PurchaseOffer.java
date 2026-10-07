package com.timder.kontor.core.company.purchase;

import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.value.ItemId;

import java.util.Objects;

public record PurchaseOffer(
        ItemId item,
        PurchaseKind kind,
        double marketPrice,
        Money unitPrice
) {
    public PurchaseOffer {
        Objects.requireNonNull(item, "item must not be null.");
        Objects.requireNonNull(kind, "kind must not be null.");
        Objects.requireNonNull(unitPrice, "unitPrice must not be null.");
        if (!(marketPrice > 0.0) || Double.isInfinite(marketPrice)) throw new IllegalArgumentException("marketPrice must be a finite, positive number.");
        if (!unitPrice.isPositive()) throw new IllegalArgumentException("unitPrice must be positive.");
    }

    public PurchaseQuote.Line lineFor(int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("quantity must be at least 1.");
        return new PurchaseQuote.Line(item, kind, quantity, unitPrice, unitPrice.multipliedBy(quantity));
    }
}
