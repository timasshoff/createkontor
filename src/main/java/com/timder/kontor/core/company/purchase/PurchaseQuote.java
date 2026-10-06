package com.timder.kontor.core.company.purchase;

import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.value.ItemId;

import java.util.List;
import java.util.Objects;

public record PurchaseQuote(List<Line> lines) {

    public PurchaseQuote {
        Objects.requireNonNull(lines, "lines must not be null.");
        if (lines.isEmpty()) throw new IllegalArgumentException("lines must not be empty.");
        lines = List.copyOf(lines);
    }

    public Money total() {
        Money total = Money.ZERO;
        for (Line line : lines) {
            total = total.plus(line.lineTotal());
        }
        return total;
    }

    public record Line(ItemId item, PurchaseKind kind, int quantity, Money unitPrice, Money lineTotal) {

        public Line {
            Objects.requireNonNull(item, "item must not be null.");
            Objects.requireNonNull(kind, "kind must not be null.");
            Objects.requireNonNull(unitPrice, "unitPrice must not be null.");
            Objects.requireNonNull(lineTotal, "lineTotal must not be null.");
            if (quantity < 1) throw new IllegalArgumentException("quantity must be at least 1.");
            if (!lineTotal.equals(unitPrice.multipliedBy(quantity)))
                throw new IllegalArgumentException("lineTotal must be unitPrice times quantity.");
        }
    }
}
