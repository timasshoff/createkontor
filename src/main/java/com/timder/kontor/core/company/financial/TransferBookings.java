package com.timder.kontor.core.company.financial;

import java.util.Objects;

public record TransferBookings(
        BookingKind payerKind, String payerReference, BookingKind receiverKind, String receiverReference) {
    public TransferBookings {
        Objects.requireNonNull(payerKind, "payerKind must not be null.");
        Objects.requireNonNull(payerReference, "payerReference must not be null.");
        Objects.requireNonNull(receiverKind, "receiverKind must not be null.");
        Objects.requireNonNull(receiverReference, "receiverReference must not be null.");
    }
}
