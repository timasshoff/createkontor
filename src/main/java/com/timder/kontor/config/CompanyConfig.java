package com.timder.kontor.config;

import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.LegalForms;
import com.timder.kontor.core.company.financial.Money;
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

        SPEC = builder.build();
    }

    public static CompanyParams toCompanyParams(LegalForms legalForms) {
        return new CompanyParams(
                legalForms,
                Money.ofDollars(START_DEPOSIT_IN_DOLLARS.get()),
                Money.ofDollars(FOUNDER_LOAN_IN_DOLLARS.get()),
                FOUNDER_LOAN_FREE_DAYS.get(),
                FOUNDER_LOAN_TERM_DAYS.get(),
                FOUNDER_LOAN_SPREAD.get(),
                OVERDRAFT_SPREAD.get(),
                Money.ofDollars(BUSINESS_LICENSE_FEE_IN_DOLLARS.get()),
                INSOLVENCY_DAYS.get(),
                BOOKING_RETENTION_DAYS.get(),
                HISTORY_LENGTH_DAYS.get()
        );
    }
}
