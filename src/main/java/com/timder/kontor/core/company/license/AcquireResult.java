package com.timder.kontor.core.company.license;

import com.timder.kontor.core.company.financial.Money;

import java.util.Objects;

public record AcquireResult(Status status, Money fee) {
    public enum Status {
        /** The fee was paid and the license belongs to the company now. */
        ACQUIRED,
        /** The company had cancelled the license, but still owned it. It is active again. Nothing was paid. */
        REACTIVATED,
        /** The company already owns the license and has not cancelled it. Nothing was paid. */
        ALREADY_HELD,
        /** The license does not exist in the catalog. Nothing was paid. */
        UNKNOWN_LICENSE,
        /** The legal form of the company is below the minimum level of the license. Nothing was paid. */
        LEGAL_LEVEL_TOO_LOW,
        /** The company already has as many active licenses as its legal form allows. Nothing was paid. */
        LIMIT_REACHED,
        /** The company is in payment difficulties and cannot buy anything. Nothing was paid. */
        NOT_OPERATIONAL,
        /** The fee exceeds balance plus overdraft. Nothing was paid. */
        CANNOT_AFFORD
    }

    public AcquireResult {
        Objects.requireNonNull(status, "status must not be null.");
        Objects.requireNonNull(fee, "fee must not be null.");
        if (fee.isNegative()) throw new IllegalArgumentException("fee must not be negative.");
        boolean mayCarryFee = status == Status.ACQUIRED || status == Status.NOT_OPERATIONAL || status == Status.CANNOT_AFFORD;
        if (!mayCarryFee && !fee.isZero()) throw new IllegalArgumentException(status + " must not carry a fee.");
    }

    /**
     * @return True if the company owns and may use the license now
     */
    public boolean success() {
        return status == Status.ACQUIRED || status == Status.REACTIVATED;
    }
}
