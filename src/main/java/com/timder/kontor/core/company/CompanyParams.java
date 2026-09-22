package com.timder.kontor.core.company;

import com.timder.kontor.core.company.financial.Money;

import java.util.Objects;

/**
 * Immutable settings for every company.
 *
 * @param legalForms Every available legal form
 * @param startDeposit The initial deposit on an account when a company is created
 * @param founderLoan The amount of optional founder loan
 * @param founderLoanFreeDays Days without interest and repayment after taking the founder loan
 * @param founderLoanTermDays Days over which the founder loan is repaid after the free days
 * @param founderLoanSpread Daily surcharge on the policy rate for the founder loan, as a fraction (0.0010 is 0.10 %)
 * @param overdraftSpread Daily surcharge on the policy rate for a negative balance, as a fraction (0.0050 is 0.50 %)
 * @param businessLicenseFee Daily fee of the business license
 * @param insolvencyDays Amount of days in payment difficulties that end in insolvency. 0 = no insolvency.
 * @param bookingRetentionDays The amount of days that individual bookings are saved
 * @param historyLengthDays The amount fo days that a daily history is saved per company
 */
public record CompanyParams(
        LegalForms legalForms,
        Money startDeposit,
        Money founderLoan,
        int founderLoanFreeDays,
        int founderLoanTermDays,
        double founderLoanSpread,
        double overdraftSpread,
        Money businessLicenseFee,
        int insolvencyDays,
        int bookingRetentionDays,
        int historyLengthDays
) {

    public CompanyParams {
        Objects.requireNonNull(legalForms, "legalForms must not be null.");
        Objects.requireNonNull(startDeposit, "startDeposit must not be null.");
        Objects.requireNonNull(founderLoan, "founderLoan must not be null.");
        Objects.requireNonNull(businessLicenseFee, "businessLicenseFee must not be null.");
        if (startDeposit.isNegative()) throw new IllegalArgumentException("startDeposit must not be negative.");
        if (founderLoan.isNegative()) throw new IllegalArgumentException("founderLoan must not be negative.");
        if (founderLoanFreeDays < 0) throw new IllegalArgumentException("founderLoanFreeDays must not be negative.");
        if (founderLoanTermDays < 1) throw new IllegalArgumentException("founderLoanTermDays must be at least 1.");
        if (!(founderLoanSpread >= 0.0)) throw new IllegalArgumentException("founderLoanSpread must not be negative.");
        if (!(overdraftSpread >= 0.0)) throw new IllegalArgumentException("overdraftSpread must not be negative.");
        if (businessLicenseFee.isNegative()) throw new IllegalArgumentException("businessLicenseFee must not be negative.");
        if (insolvencyDays < 0) throw new IllegalArgumentException("insolvencyDays must not be negative.");
        if (bookingRetentionDays < 1) throw new IllegalArgumentException("bookingRetentionDays must be at least 1.");
        if (historyLengthDays < 1) throw new IllegalArgumentException("historyLengthDays must be at least 1.");
    }

    public boolean insolvencyEnabled() {
        return insolvencyDays > 0;
    }

    /*
    The following code is AI generated
     */

    public static CompanyParams standard() {
        return new CompanyParams(
                LegalForms.standard(),
                Money.ofDollars(1_000),
                Money.ofDollars(5_000),
                7,
                21,
                0.0010,
                0.0050,
                Money.ofDollars(20),
                5,
                30,
                360
        );
    }

}
