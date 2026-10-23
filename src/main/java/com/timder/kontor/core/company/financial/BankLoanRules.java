package com.timder.kontor.core.company.financial;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BankLoanRules {

    private static final long PROBE_NUMBER = 1;
    private static final BigDecimal FOUR = BigDecimal.valueOf(4);

    /**
     * @param company The company
     * @param params The company params
     * @return True, if the legal form of the company allows bank loans
     */
    public static boolean isAllowed(Company company, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        return company.legalForm(params).bankLoanAllowed();
    }

    public static int bankLoanCount(Company company) {
        Objects.requireNonNull(company, "company must not be null.");
        int count = 0;
        for (Loan loan : company.loans()) {
            if (loan.kind() == Loan.Kind.BANK) count++;
        }
        return count;
    }

    /**
     * The most a company can borrow
     * @param company The company
     * @param params The company parameters
     * @return The credit Limit
     */
    public static Money getCreditLimit(Company company, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        BankLoanParams bank = params.bankLoans();

        Money byLegalForm = company.legalForm(params).bankLoanLimit();
        Money byRevenue =
                company.averageDailyRevenue(bank.creditLimitRevenueDays()).scaled(bank.creditLimitRevenueMultiple());

        Money owed = Money.ZERO;
        for (Loan loan : company.loans()) {
            if (loan.kind() == Loan.Kind.BANK) owed = owed.plus(loan.outstanding());
        }
        return Money.max(Money.min(byLegalForm, byRevenue).minus(owed), Money.ZERO);
    }

    /**
     * @param company The company
     * @param params The company parameters
     * @return The smallest possible bank loan
     */
    public static Money minimumAmount(Company company, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        return company.legalForm(params)
                .bankLoanLimit()
                .scaled(params.bankLoans().minimumAmountShare());
    }

    /**
     * The surcharge for the risk of a company
     *
     * @param stars The reputation of the company in stars
     * @param bank The bank loan parameters
     * @return The daily surcharge as a fraction
     */
    public static double riskSurcharge(double stars, BankLoanParams bank) {
        Objects.requireNonNull(bank, "bank must not be null.");
        return riskSurchargeExact(stars, bank).doubleValue();
    }

    /**
     * The daily interest rate of an offer: policy rate, plus risk surcharge, plus the term surcharge of the offer,
     * plus the grace surcharge for every free day
     *
     * @param offer The offer
     * @param policyRate The current policy rate
     * @param stars The reputation of the company in stars
     * @param bank The bank loan parameters
     * @return The daily interest rate as a fraction
     */
    public static double interestRate(BankLoanOfferDef offer, double policyRate, double stars, BankLoanParams bank) {
        Objects.requireNonNull(offer, "offer must not be null.");
        Objects.requireNonNull(bank, "bank must not be null.");
        if (!Double.isFinite(policyRate) || policyRate < 0.0)
            throw new IllegalArgumentException("policyRate must be finite and not negative.");

        return BigDecimal.valueOf(policyRate)
                .add(riskSurchargeExact(stars, bank))
                .add(BigDecimal.valueOf(offer.termSurcharge()))
                .add(BigDecimal.valueOf(bank.graceDaySurcharge()).multiply(BigDecimal.valueOf(offer.freeDays())))
                .doubleValue();
    }

    public static List<BankLoanQuote> quotes(
            Company company, Money amount, double policyRate, double stars, CompanyParams params) {
        requireTakable(company, amount, params);
        BankLoanParams bank = params.bankLoans();

        List<BankLoanQuote> quotes = new ArrayList<>(bank.offers().size());
        for (BankLoanOfferDef offer : bank.offers()) {
            quotes.add(quote(offer, amount, policyRate, stars, bank));
        }
        return quotes;
    }

    public static Loan take(
            Company company,
            long day,
            String offerId,
            Money amount,
            double policyRate,
            double stars,
            CompanyParams params) {
        Objects.requireNonNull(offerId, "offerId must not be null.");
        requireTakable(company, amount, params);
        BankLoanParams bank = params.bankLoans();

        BankLoanOfferDef offer = null;
        for (BankLoanOfferDef candidate : bank.offers()) {
            if (candidate.id().equals(offerId)) offer = candidate;
        }
        if (offer == null) throw new IllegalArgumentException("There is no bank loan offer " + offerId + ".");

        double rate = interestRate(offer, policyRate, stars, bank);

        Loan loan = newLoan(company.issueLoanNumber(), offer, amount, rate);
        company.account().book(day, BookingKind.LOAN_PAYOUT, amount, Company.BANK_LOAN_REFERENCE + loan.number());
        company.addLoan(loan);
        return loan;
    }

    private static void requireTakable(Company company, Money amount, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(amount, "amount must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        if (!isAllowed(company, params))
            throw new IllegalStateException("The legal form of the company does not allow bank loans.");
        int highest = params.bankLoans().maxLoans();
        if (bankLoanCount(company) >= highest)
            throw new IllegalStateException(
                    "The company already has the highest number of bank loans (" + highest + ").");

        if (!amount.isPositive()) throw new IllegalArgumentException("amount must be positive.");
        Money minimum = minimumAmount(company, params);
        if (amount.compareTo(minimum) < 0)
            throw new IllegalArgumentException("The amount " + amount + " is below the minimum of " + minimum + ".");
        Money limit = getCreditLimit(company, params);
        if (amount.compareTo(limit) > 0)
            throw new IllegalArgumentException("The amount " + amount + " is above the credit limit of " + limit + ".");
    }

    private static BankLoanQuote quote(
            BankLoanOfferDef offer, Money amount, double policyRate, double stars, BankLoanParams bank) {
        double rate = interestRate(offer, policyRate, stars, bank);

        Loan probe = newLoan(PROBE_NUMBER, offer, amount, rate);
        Money firstPayment = null;
        Money totalInterest = Money.ZERO;
        while (!probe.isRepaid()) {
            Loan.Installment installment = probe.settleDay();
            totalInterest = totalInterest.plus(installment.interest());
            if (firstPayment == null && installment.total().isPositive()) firstPayment = installment.total();
        }
        return new BankLoanQuote(offer, rate, firstPayment, totalInterest, probe.elapsedDays());
    }

    private static Loan newLoan(long number, BankLoanOfferDef offer, Money amount, double rate) {
        return Loan.create(number, Loan.Kind.BANK, amount, rate, offer.termDays(), offer.freeDays());
    }

    private static BigDecimal riskSurchargeExact(double stars, BankLoanParams bank) {
        if (!(stars >= 1.0 && stars <= 5.0)) throw new IllegalArgumentException("stars must be between 1 and 5.");
        BigDecimal one = BigDecimal.valueOf(bank.riskSurchargeAtOneStar());
        BigDecimal five = BigDecimal.valueOf(bank.riskSurchargeAtFiveStars());
        BigDecimal fraction = BigDecimal.valueOf(stars).subtract(BigDecimal.ONE).divide(FOUR);
        return one.subtract(one.subtract(five).multiply(fraction));
    }
}
