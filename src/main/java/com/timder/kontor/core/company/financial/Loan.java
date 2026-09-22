package com.timder.kontor.core.company.financial;

import com.timder.kontor.core.company.CompanyParams;

import java.math.BigDecimal;
import java.util.Objects;

public final class Loan {

    public enum Kind {
        FOUNDER,
        BANK
    }

    public record Installment(Money interest, Money repayment) {
        public static Installment NONE = new Installment(Money.ZERO, Money.ZERO);

        public Installment {
            Objects.requireNonNull(interest, "interest must not be null.");
            Objects.requireNonNull(repayment, "repayment must not be null.");
        }

        public Money total() {
            return interest.plus(repayment);
        }
    }

    private final Kind kind;
    private final Money principal;
    private final double rate;
    private final int termDays;
    private final int freeDays;
    private final Money repayment;

    private Money outstanding;
    private int elapsedDays;

    private Loan(Kind kind, Money principal, Money outstanding, double rate, int termDays, int freeDays, Money repayment, int elapsedDays) {
        Objects.requireNonNull(kind, "kind must not be null.");
        Objects.requireNonNull(principal, "principal must not be null.");
        Objects.requireNonNull(outstanding, "outstanding must not be null.");
        Objects.requireNonNull(repayment, "repayment must not be null.");
        if (!principal.isPositive()) throw new IllegalArgumentException("principal must be positive.");
        if (outstanding.isNegative() || outstanding.compareTo(principal) > 0) throw new IllegalArgumentException("outstanding must be between zero and the principal.");
        if (!(rate >= 0.0)) throw new IllegalArgumentException("rate must not be negative.");
        if (termDays < 1) throw new IllegalArgumentException("termDays must be at least 1.");
        if (freeDays < 0) throw new IllegalArgumentException("freeDays must not be negative.");
        if (!repayment.isPositive()) throw new IllegalArgumentException("repayment must be positive.");
        if (elapsedDays < 0) throw new IllegalArgumentException("elapsedDays must not be negative.");

        this.kind = kind;
        this.principal = principal;
        this.outstanding = outstanding;
        this.rate = rate;
        this.termDays = termDays;
        this.freeDays = freeDays;
        this.repayment = repayment;
        this.elapsedDays = elapsedDays;
    }

    /**
     * Creates a new loan that starts with its first day.
     * @param kind The loan kind
     * @param principal The amount that was paid out
     * @param rate The fixed interest rate
     * @param termDays How many days of repayment after the free days
     * @param freeDays Days without interest and repayment
     * @return The loan
     */
    public static Loan create(Kind kind, Money principal, double rate, int termDays, int freeDays) {
        if (termDays < 1) throw new IllegalArgumentException("termDays must be at least 1.");
        Money repayment = principal.dividedBy(termDays);
        if (!repayment.isPositive()) {
            repayment = Money.ofCents(1); // Repayment is never less than 1 cent
        }
        return new Loan(kind, principal, principal, rate, termDays, freeDays, repayment, 0);
    }

    public static Loan founder(Money amount, double policyRate, CompanyParams params) {
        double rate = BigDecimal.valueOf(policyRate).add(BigDecimal.valueOf(params.founderLoanSpread())).doubleValue();
        return create(Kind.FOUNDER, amount, rate, params.founderLoanTermDays(), params.founderLoanFreeDays());
    }

    public Installment settleDay() {
        if (isRepaid()) throw new IllegalStateException("The loan is already repaid.");

        elapsedDays++;
        if (elapsedDays <= freeDays) {
            return Installment.NONE;
        }

        Money interest = outstanding.scaled(rate);
        boolean lastDay = elapsedDays - freeDays >= termDays;
        Money paid = lastDay ? outstanding : Money.min(repayment, outstanding);
        outstanding = outstanding.minus(paid);
        return  new Installment(interest, paid);
    }

    public boolean isRepaid() {
        return outstanding.isZero();
    }

    public Kind kind() {
        return kind;
    }

    /**
     * @return The amount that was paid out
     */
    public Money principal() {
        return principal;
    }

    /**
     * @return The debt that is left
     */
    public Money outstanding() {
        return outstanding;
    }

    /**
     * @return The fixed interest rate per day as a fraction
     */
    public double rate() {
        return rate;
    }

    public int termDays() {
        return termDays;
    }

    public int freeDays() {
        return freeDays;
    }

    /**
     * @return The regular daily repayment, rounded to 0.01$
     */
    public Money repayment() {
        return repayment;
    }

    /**
     * @return The days that were settled so far, free days included
     */
    public int elapsedDays() {
        return elapsedDays;
    }

    @Override
    public String toString() {
        return "Loan[%s, principal=%s, outstanding=%s, rate=%s, day=%d]".formatted(kind, principal, outstanding, rate, elapsedDays);
    }

    public record SaveState(
            Kind kind,
            Money principal,
            Money outstanding,
            double rate,
            int termDays,
            int freeDays,
            Money repayment,
            int elapsedDays) { }

    public SaveState getSaveState() {
        return new SaveState(kind, principal, outstanding, rate, termDays, freeDays, repayment, elapsedDays);
    }

    public static Loan restore(SaveState saveState) {
        return new Loan(
                saveState.kind(),
                saveState.principal(),
                saveState.outstanding(),
                saveState.rate(),
                saveState.termDays(),
                saveState.freeDays(),
                saveState.repayment(),
                saveState.elapsedDays());
    }

}
