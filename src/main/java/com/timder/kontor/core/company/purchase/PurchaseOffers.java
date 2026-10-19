package com.timder.kontor.core.company.purchase;

import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.value.ItemId;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PurchaseOffers {

    public static final Money MINIMUM_UNIT_PRICE = Money.ofCents(1L);

    public static List<PurchaseOffer> list(Economy economy, CompanyParams params, boolean automaticPurchase) {
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        List<PurchaseOffer> offers = new ArrayList<>();
        for (ItemId market : economy.marketIds()) {
            offers.add(productOffer(economy, params, market, automaticPurchase));
        }
        for (ItemId material : economy.rawMaterialIds()) {
            offers.add(rawMaterialOffer(economy, params, material, automaticPurchase));
        }
        return List.copyOf(offers);
    }

    public static PurchaseOffer offerFor(
            Economy economy, CompanyParams params, ItemId item, boolean automaticPurchase) {
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        Objects.requireNonNull(item, "item must not be null.");

        if (economy.isMarket(item)) {
            return productOffer(economy, params, item, automaticPurchase);
        }
        if (economy.isRawMaterial(item)) {
            return rawMaterialOffer(economy, params, item, automaticPurchase);
        }
        throw new IllegalArgumentException(item + " cannot be purchased.");
    }

    public static Money unitPrice(double marketPrice, double markup) {
        if (!(marketPrice > 0.0) || Double.isInfinite(marketPrice))
            throw new IllegalArgumentException("marketPrice must be a finite, positive number.");
        if (!(markup >= 0.0) || Double.isInfinite(markup))
            throw new IllegalArgumentException("markup must be a finite number, not negative.");

        BigDecimal factor = BigDecimal.valueOf(markup);
        BigDecimal cents = BigDecimal.valueOf(marketPrice)
                .multiply(factor)
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP);
        return Money.max(Money.ofCents(cents.longValueExact()), MINIMUM_UNIT_PRICE);
    }

    private static PurchaseOffer productOffer(
            Economy economy, CompanyParams params, ItemId market, boolean automaticPurchase) {
        double price = economy.marketSnapshot(market).displayedPrice();
        return new PurchaseOffer(
                market,
                PurchaseKind.PRODUCT,
                price,
                unitPrice(price, automaticPurchase ? params.autoPurchaseMarkup() : params.purchaseMarkup()));
    }

    private static PurchaseOffer rawMaterialOffer(
            Economy economy, CompanyParams params, ItemId material, boolean automaticPurchase) {
        double price = economy.rawMaterialSnapshot(material).price();
        return new PurchaseOffer(
                material,
                PurchaseKind.RAW_MATERIAL,
                price,
                unitPrice(price, automaticPurchase ? params.autoPurchaseMarkup() : params.purchaseMarkup()));
    }
}
