package com.timder.kontor.core.company.license;

import com.timder.kontor.core.value.ItemId;

import java.util.Objects;
import java.util.regex.Pattern;

public sealed interface LicenseKey permits LicenseKey.Defined, LicenseKey.Generated {

    static LicenseKey defined(String id) {
        return new Defined(id);
    }

    record Defined(String id) implements LicenseKey {

        private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_]+");
        private static final String TRANSLATION_PREFIX = "license.createkontor.";

        public Defined {
            if (id == null || !VALID_ID.matcher(id).matches()) {
                throw new IllegalArgumentException("id must consist of a-z, 0-9 and _ but was \"" + id + "\".");
            }
        }

        /**
         * @return The translation key of this license
         */
        public String translationKey() {
            return TRANSLATION_PREFIX + id;
        }

        @Override
        public String toString() {
            return id;
        }
    }

    record Generated(ItemId market) implements LicenseKey {

        public Generated {
            Objects.requireNonNull(market, "market must not be null.");
        }

        @Override
        public String toString() {
            return "generated[" + market + "]";
        }
    }

}
