package com.timder.kontor.config;

import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.license.LicenseParams;
import com.timder.kontor.data.KontorData;
import net.neoforged.neoforge.common.ModConfigSpec;

public class CompanyConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue START_DEPOSIT_IN_DOLLARS;
    public static final ModConfigSpec.IntValue FOUNDER_LOAN_IN_DOLLARS;
    public static final ModConfigSpec.IntValue FOUNDER_LOAN_FREE_DAYS;
    public static final ModConfigSpec.IntValue FOUNDER_LOAN_TERM_DAYS;
    public static final ModConfigSpec.DoubleValue FOUNDER_LOAN_SPREAD;
    public static final ModConfigSpec.DoubleValue OVERDRAFT_SPREAD;
    public static final ModConfigSpec.IntValue BUSINESS_LICENSE_FEE_IN_DOLLARS;
    public static final ModConfigSpec.IntValue INSOLVENCY_DAYS;
    public static final ModConfigSpec.IntValue BOOKING_RETENTION_DAYS;
    public static final ModConfigSpec.IntValue HISTORY_LENGTH_DAYS;
    public static final ModConfigSpec.DoubleValue LICENSE_REFERENCE_FEE_RATE;
    public static final ModConfigSpec.IntValue LICENSE_APPLICATION_FEE_MULTIPLIER;

    public static final ModConfigSpec.DoubleValue HIRE_BONUS_SALARY_MULTIPLE;
    public static final ModConfigSpec.IntValue EMPLOYEE_ORPHAN_DAYS;
    public static final ModConfigSpec.IntValue CONTRACT_CHECK_MIN_ACTIVE_TICKS;
    public static final ModConfigSpec.IntValue LAWYER_SALARY_IN_DOLLARS;
    public static final ModConfigSpec.IntValue CALCULATOR_SALARY_IN_DOLLARS;
    public static final ModConfigSpec.IntValue MARKET_ANALYST_SALARY_IN_DOLLARS;

    public static final ModConfigSpec.DoubleValue PURCHASE_MARKUP;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Company account and founding").push("company");

        START_DEPOSIT_IN_DOLLARS = builder.comment("Initial deposit on a company's account when it is founded")
                .defineInRange("startDepositInDollars", 1_000, 0, Integer.MAX_VALUE);
        FOUNDER_LOAN_IN_DOLLARS = builder.comment("Amount of the optional founder loan")
                .defineInRange("founderLoanInDollars", 5_000, 0, Integer.MAX_VALUE);
        FOUNDER_LOAN_FREE_DAYS = builder.comment("Days without interest and repayment after taking the founder loan")
                .defineInRange("founderLoanFreeDays", 7, 0, Integer.MAX_VALUE);
        FOUNDER_LOAN_TERM_DAYS = builder.comment("Days over which the founder loan is repaid after the free days")
                .defineInRange("founderLoanTermDays", 21, 1, Integer.MAX_VALUE);
        FOUNDER_LOAN_SPREAD = builder.comment("Daily surcharge on the policy rate for the founder loan, as a fraction (e.g. 0.0010 is 0.10 %)")
                .defineInRange("founderLoanSpread", 0.0010, 0.0, Double.MAX_VALUE);
        OVERDRAFT_SPREAD = builder.comment("Daily surcharge on the policy rate for a negative balance, as a fraction (e.g. 0.0050 is 0.50 %)")
                .defineInRange("overdraftSpread", 0.0050, 0.0, Double.MAX_VALUE);
        BUSINESS_LICENSE_FEE_IN_DOLLARS = builder.comment("Daily fee of the business license")
                .defineInRange("businessLicenseFeeInDollars", 20, 0, Integer.MAX_VALUE);
        INSOLVENCY_DAYS = builder.comment("Days in payment difficulties that end in insolvency. 0 = no insolvency.")
                .defineInRange("insolvencyDays", 5, 0, Integer.MAX_VALUE);
        BOOKING_RETENTION_DAYS = builder.comment("Days that individual bookings are kept")
                .defineInRange("bookingRetentionDays", 30, 1, Integer.MAX_VALUE);
        HISTORY_LENGTH_DAYS = builder.comment("Days that the daily history is kept per company")
                .defineInRange("historyLengthDays", 360, 1, Integer.MAX_VALUE);

        builder.pop();

        builder.comment("License fees").push("licenses");

        LICENSE_REFERENCE_FEE_RATE = builder.comment("Reference fee rate: The daily reference fee of a market is this rate times its base demand times its reference cost")
                .defineInRange("referenceFeeRate", 0.009, 0.000001, 1.0);
        LICENSE_APPLICATION_FEE_MULTIPLIER = builder.comment("The application fee of a license is this many times its reference fee")
                .defineInRange("applicationFeeMultiplier", 3, 1, 1_000);

        builder.pop();

        builder.comment("Employees. The salaries are base salaries per day, the salary factor of the legal form is applied on top.").push("employees");

        HIRE_BONUS_SALARY_MULTIPLE = builder.comment("One-time bonus a company pays when it hires an employee, as a multiple of the employee's daily salary (base salary times the salary factor of the legal form). 0 = no bonus")
                .defineInRange("hireBonusSalaryMultiple", 3.0, 0.0, 100.0);
        EMPLOYEE_ORPHAN_DAYS = builder.comment("An employee whose desk has not reported for this many days loses the contract (e.g. the desk sits in an unloaded chunk)")
                .defineInRange("orphanDays", 3, 1, Integer.MAX_VALUE);
        CONTRACT_CHECK_MIN_ACTIVE_TICKS = builder.comment("Contracts are only checked at the end of a day on which the company was active for at least this many ticks, so its desks had time to report")
                .defineInRange("contractCheckMinActiveTicks", 200, 0, 24_000);
        LAWYER_SALARY_IN_DOLLARS = builder.comment("Base salary of a lawyer per day. Fixed when the lawyer is hired")
                .defineInRange("lawyerSalaryInDollars", 80, 0, Integer.MAX_VALUE);
        CALCULATOR_SALARY_IN_DOLLARS = builder.comment("Base salary of a calculator per day. Fixed when the calculator is hired")
                .defineInRange("calculatorSalaryInDollars", 60, 0, Integer.MAX_VALUE);
        MARKET_ANALYST_SALARY_IN_DOLLARS = builder.comment("Base salary of a market analyst per day. Fixed when the market analyst is hired")
                .defineInRange("marketAnalystSalaryInDollars", 90, 0, Integer.MAX_VALUE);

        builder.pop();

        builder.comment("Purchasing").push("purchasing");
        PURCHASE_MARKUP = builder.comment("Surcharge a company pays on the market price when it buys goods, as a fraction (0.10 = 10 %). Raise it to make buying products and reselling them to customers less attractive")
                .defineInRange("purchaseMarkup", 0.10, 0.0, 10.0);

        builder.pop();

        SPEC = builder.build();
    }

    public static CompanyParams toCompanyParams(LegalForms legalForms) {
        LicenseParams licenseParams = new LicenseParams(
                LICENSE_REFERENCE_FEE_RATE.get(),
                LICENSE_APPLICATION_FEE_MULTIPLIER.get()
        );
        return new CompanyParams(
                legalForms,
                KontorData.getLicenseCatalog(legalForms, licenseParams),
                Money.ofDollars(START_DEPOSIT_IN_DOLLARS.get()),
                Money.ofDollars(FOUNDER_LOAN_IN_DOLLARS.get()),
                FOUNDER_LOAN_FREE_DAYS.get(),
                FOUNDER_LOAN_TERM_DAYS.get(),
                FOUNDER_LOAN_SPREAD.get(),
                OVERDRAFT_SPREAD.get(),
                Money.ofDollars(BUSINESS_LICENSE_FEE_IN_DOLLARS.get()),
                INSOLVENCY_DAYS.get(),
                BOOKING_RETENTION_DAYS.get(),
                HISTORY_LENGTH_DAYS.get(),
                HIRE_BONUS_SALARY_MULTIPLE.get(),
                PURCHASE_MARKUP.get()
        );
    }
}
