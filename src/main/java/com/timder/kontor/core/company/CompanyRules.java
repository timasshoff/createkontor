package com.timder.kontor.core.company;

import com.timder.kontor.core.company.financial.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class CompanyRules {

    /**
     * Settles one day for a company.
     *
     * @param company The company to settle. Is being mutated.
     * @param day The current day being settled
     * @param policyRate The current policy rate
     * @param otherCosts Extra costs of that day.
     *                   Must not contain {@link BookingKind#BUSINESS_LICENSE} or {@link BookingKind#INTEREST} as these are already being computed here.
     * @param params The company parameters
     * @return The history entry of that day
     */
    public static CompanyHistoryEntry settleDay(Company company, long day, double policyRate, Map<BookingKind, Money> otherCosts, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(otherCosts, "otherCosts must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        Account account = company.account();

        // Step 1: Fixed and mixed costs (only business license is actually computed)
        if (params.businessLicenseFee().isPositive()) {
            account.book(day, BookingKind.BUSINESS_LICENSE, params.businessLicenseFee().negate(), "");
        }
        for (Map.Entry<BookingKind, Money> entry : otherCosts.entrySet()) {
            account.book(day, entry.getKey(), entry.getValue().negate(), "");
        }

        // Step 2: Loan interest and repayment
        for (Loan loan : company.loans()) {
            Loan.Installment installment = loan.settleDay();
            if (installment.interest().isPositive()) {
                account.book(day, BookingKind.INTEREST, installment.interest().negate(), referenceOf(loan));
            }
            if (installment.repayment().isPositive()) {
                account.book(day, BookingKind.LOAN_REPAYMENT, installment.repayment().negate(), referenceOf(loan));
            }
            if (loan.isRepaid()) {
                company.removeLoan(loan);
            }
        }

        // Step 3: Overdraft interest on negative balance
        if (account.getBalance().isNegative()) {
            Money overdraftInterest = account.getBalance().abs().scaled(policyRate + params.overdraftSpread());
            if (overdraftInterest.isPositive()) {
                account.book(day, BookingKind.INTEREST, overdraftInterest.negate(), "");
            }
        }

        // Step 4: Liquidity status
        Money overdraftLimit = company.overdraftLimit(params);
        boolean inTrouble = account.getBalance().compareTo(overdraftLimit.negate()) < 0;
        company.setLiquidity(inTrouble ? Liquidity.ILLIQUIDITY : Liquidity.NORMAL);
        company.setDaysInTrouble(inTrouble ? company.daysInTrouble() + 1 : 0);

        // Step 5: Day Result
        CompanyHistoryEntry historyEntry = buildHistoryEntry(account, day);
        company.appendHistory(historyEntry, params.historyLengthDays());

        // Step 6: Clean booking history
        account.prune(day, params.bookingRetentionDays());

        return historyEntry;
    }

    private static void validateOtherCosts(Map<BookingKind, Money> otherCosts) {
        for (Map.Entry<BookingKind, Money> entry : otherCosts.entrySet()) {
            BookingKind kind = entry.getKey();
            Money amount = entry.getValue();
            if (kind == BookingKind.BUSINESS_LICENSE || kind == BookingKind.INTEREST) {
                throw new IllegalArgumentException(kind + " is booked by settleDay itself, it must not be in otherCosts.");
            }
            if (!kind.isCost()) {
                throw new IllegalArgumentException(kind + " is not a cost kind.");
            }
            if (amount == null || !amount.isPositive()) {
                throw new IllegalArgumentException("The amount for " + kind + " must be positive.");
            }
        }
    }

    private static CompanyHistoryEntry buildHistoryEntry(Account account, long day) {
        Map<BookingKind, Money> sums = account.sumsByKind(day);
        Money revenue = Money.ZERO;
        Map<BookingKind, Money> costsByKind = new EnumMap<>(BookingKind.class);
        for (Map.Entry<BookingKind, Money> entry : sums.entrySet()) {
            if (entry.getKey().isRevenue()) {
                revenue = revenue.plus(entry.getValue());
            } else if (entry.getKey().isCost()) {
                costsByKind.put(entry.getKey(), entry.getValue());
            }
        }
        return new CompanyHistoryEntry(day, account.resultOfDay(day), revenue, costsByKind);
    }

    private static String referenceOf(Loan loan) {
        return switch (loan.kind()) {
            case FOUNDER -> Company.FOUNDER_LOAN_REFERENCE;
            case BANK -> "bank_loan";
        };
    }
}
