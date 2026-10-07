package com.timder.kontor.core.company.purchase;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.BookingKind;
import com.timder.kontor.core.economy.Economy;

import java.util.Objects;

public final class PurchaseRules {

    public static PurchaseStatus preview(Company company, PurchaseQuote quote, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(quote, "quote must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        if (!company.isOperational()) {
            return PurchaseStatus.NOT_OPERATIONAL;
        }
        if (!company.canSpend(quote.total(), params)) {
            return PurchaseStatus.INSUFFICIENT_FUNDS;
        }
        return PurchaseStatus.AFFORDABLE;
    }

    public static boolean isCurrent(Economy economy, PurchaseQuote quote, CompanyParams params) {
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(quote, "quote must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        for (PurchaseQuote.Line line : quote.lines()) {
            try {
                if (!PurchaseOffers.offerFor(economy, params, line.item()).lineFor(line.quantity()).equals(line)) {
                    return false;
                }
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return true;
    }

    public static void buy(Company company, Economy economy, PurchaseQuote quote, long day, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(quote, "quote must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        if (!isCurrent(economy, quote, params)) {
            throw new IllegalStateException("The quote is outdated: a price has changed since it was made.");
        }
        PurchaseStatus status = preview(company, quote, params);
        if (status != PurchaseStatus.AFFORDABLE) {
            throw new IllegalStateException(company.id() + " cannot buy: " + status + ".");
        }

        for (PurchaseQuote.Line line : quote.lines()) {
            boolean paid = company.trySpend(day, BookingKind.PURCHASE, line.lineTotal(), reference(line), params);
            if (!paid) {
                throw new IllegalStateException("Booking of " + reference(line) + " failed although the total was affordable.");
            }
        }

        for (PurchaseQuote.Line line : quote.lines()) {
            switch (line.kind()) {
                case PRODUCT -> economy.recordMarketPurchase(line.item(), line.quantity());
                case RAW_MATERIAL -> economy.recordPurchase(line.item(), line.quantity());
            }
        }
    }

    private static String reference(PurchaseQuote.Line line) {
        return line.item().value() + " x" + line.quantity();
    }
}
