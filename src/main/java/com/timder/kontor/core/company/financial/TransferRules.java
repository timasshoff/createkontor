package com.timder.kontor.core.company.financial;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import java.util.Objects;

public final class TransferRules {

    public static TransferStatus preview(Company payer, Company receiver, Money amount, CompanyParams params) {
        Objects.requireNonNull(payer, "payer must not be null.");
        Objects.requireNonNull(receiver, "receiver must not be null.");
        Objects.requireNonNull(amount, "amount must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        if (!amount.isPositive()) {
            return TransferStatus.INVALID_AMOUNT;
        }
        if (payer.id().equals(receiver.id())) {
            return TransferStatus.SAME_COMPANY;
        }
        if (!payer.isOperational()) {
            return TransferStatus.NOT_OPERATIONAL;
        }
        if (!payer.canSpend(amount, params)) {
            return TransferStatus.CANNOT_AFFORD;
        }
        return TransferStatus.OK;
    }

    public static void transfer(
            Company payer, Company receiver, Money amount, TransferBookings bookings, long day, CompanyParams params) {
        Objects.requireNonNull(bookings, "bookings must not be null.");
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");

        TransferStatus status = preview(payer, receiver, amount, params);
        if (status != TransferStatus.OK) {
            throw new IllegalStateException(payer.id() + " cannot transfer to " + receiver.id() + ": " + status + ".");
        }

        boolean paid = payer.trySpend(day, bookings.payerKind(), amount, bookings.payerReference(), params);
        if (!paid) {
            throw new IllegalStateException("The transfer of " + amount + " failed.");
        }
        receiver.account().book(day, bookings.receiverKind(), amount, bookings.receiverReference());
    }
}
