package com.timder.kontor.core.company.license;

import com.timder.kontor.core.value.ItemId;

import java.util.Objects;

public final class LicenseKeyCodec {

    private static final String DEFINED = "defined:";
    private static final String GENERATED = "generated:";

    public static String encode(LicenseKey key) {
        Objects.requireNonNull(key, "key must not be null.");
        return switch (key) {
            case LicenseKey.Defined defined -> DEFINED + defined.id();
            case LicenseKey.Generated generated -> GENERATED + generated.market().value();
        };
    }

    public static LicenseKey decode(String text) {
        Objects.requireNonNull(text, "text must not be null.");
        if (text.startsWith(DEFINED)) {
            return LicenseKey.defined(text.substring(DEFINED.length()));
        }
        if (text.startsWith(GENERATED)) {
            String market = text.substring(GENERATED.length());
            if (market.isBlank()) throw new IllegalArgumentException("A generated license key needs a market: " + text);
            return new LicenseKey.Generated(new ItemId(market));
        }
        throw new IllegalArgumentException("Not a license key: " + text);
    }
}
