package com.timder.kontor.core.company;

import java.util.Objects;

/**
 * The limits and unlocks of one legal form.
 * Immutable.
 *
 * @param level The level of this legal form
 * @param id The id for this legal form
 * @param maxEmployees The max employees a company using this form can have
 * @param maxProductLicenses The max product licenses a company using this form can have
 * @param maxOpenRequestsPerProduct The max open requests per product a company using this form can have
 * @param maxOpenRequestsTotal The max open requests in total a company using this form can have
 * @param maxOpenOrders The max open orders a company using this form can have simultaneously
 * @param maxOpenContracts The max running contracts a company using this form can have simultaneously
 * @param maxOrderQuantity The highest amount of items per order a company using this form can get
 * @param deadlineFactor The deadline factor for open orders
 * @param maxGridConnection The largest grid connection tier in SU a company using this form can have
 * @param feedInLicenseTier The feed in licenses up to this tier a company using this form can have
 * @param overdraftLimit The overdraft limit a company using this form has
 * @param bankLoanLimit The highest bank loan a company using this form can get
 * @param autoAcceptRequests Whether auto acceptance of orders is unlocked
 * @param logisticsNetwork Whether the company can connect to a logistics network
 * @param freeStorage Value of stock that costs no storage fee
 * @param founderProtection Whether the founder protection applies
 */
public record LegalFormDef(
        int level,
        String id,
        int maxEmployees,
        int maxProductLicenses,
        int maxOpenRequestsPerProduct,
        int maxOpenRequestsTotal,
        int maxOpenOrders,
        int maxOpenContracts,
        int maxOrderQuantity,
        double deadlineFactor,
        int maxGridConnection, // Unused for now (no SU integration)
        int feedInLicenseTier, // Unused for now (no SU integration)
        Money overdraftLimit,
        Money bankLoanLimit,
        boolean autoAcceptRequests,
        boolean logisticsNetwork, // Unused for now (no logistics integration)
        Money freeStorage,
        boolean founderProtection
) {

    public static final int UNLIMITED = Integer.MAX_VALUE;

    public LegalFormDef {
        if (level < 1) throw new IllegalArgumentException("level must be at least 1.");
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank.");
        if (maxEmployees < 0) throw new IllegalArgumentException("maxEmployees must not be negative.");
        if (maxProductLicenses < 0) throw new IllegalArgumentException("maxProductLicenses must not be negative.");
        if (maxOpenRequestsPerProduct < 0) throw new IllegalArgumentException("maxOpenRequestsPerProduct must not be negative.");
        if (maxOpenRequestsTotal < 0) throw new IllegalArgumentException("maxOpenRequestsTotal must not be negative.");
        if (maxOpenOrders < 0) throw new IllegalArgumentException("maxOpenOrders must not be negative.");
        if (maxOpenContracts < 0) throw new IllegalArgumentException("maxOpenContracts must not be negative.");
        if (maxOrderQuantity <= 0) throw new IllegalArgumentException("maxOrderQuantity must be positive.");
        if (!(deadlineFactor > 0.0)) throw new IllegalArgumentException("deadlineFactor must be positive.");
        if (maxGridConnection < 0) throw new IllegalArgumentException("maxGridConnection must not be negative.");
        if (feedInLicenseTier < 0) throw new IllegalArgumentException("feedInLicenseTier must not be negative.");
        Objects.requireNonNull(overdraftLimit, "overdraftLimit must not be null.");
        Objects.requireNonNull(bankLoanLimit, "bankLoanLimit must not be null.");
        Objects.requireNonNull(freeStorage, "freeStorage must not be null.");
        if (overdraftLimit.isNegative()) throw new IllegalArgumentException("overdraftLimit must not be negative.");
        if (bankLoanLimit.isNegative()) throw new IllegalArgumentException("bankLoanLimit must not be negative.");
        if (freeStorage.isNegative()) throw new IllegalArgumentException("freeStorage must not be negative.");
    }

    public boolean bankLoanAllowed() {
        return bankLoanLimit.isPositive();
    }
}
