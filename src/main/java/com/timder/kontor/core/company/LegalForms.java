package com.timder.kontor.core.company;

import com.timder.kontor.core.company.financial.Money;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.timder.kontor.core.company.LegalFormDef.UNLIMITED;

/**
 * All legal forms of this world, ordered by level
 */
public record LegalForms(List<LegalFormDef> forms) {
    public LegalForms {
        Objects.requireNonNull(forms, "forms must not be null.");
        forms = List.copyOf(forms);
        if (forms.isEmpty()) throw new IllegalArgumentException("forms must not be empty.");

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < forms.size(); i++) {
            LegalFormDef form = forms.get(i);
            if (form.level() != i + 1) {
                throw new IllegalArgumentException("levels must be consecutive starting at 1, but position " + (i + 1) + " has level " + form.level() + ".");
            }
            if (!ids.add(form.id())) {
                throw new IllegalArgumentException("duplicate legal form id " + form.id() + ".");
            }
        }
    }

    /**
     * Gets a legal form from its level
     * @param level The level
     * @return The legal form
     */
    public LegalFormDef get(int level) {
        if (level < 1 || level > forms.size()) {
            throw new IllegalArgumentException("no legal form with level " + level + ".");
        }
        return forms.get(level - 1);
    }

    /**
     * Gets the next legal form
     * @param level The current level
     * @return The legal form with the next level
     */
    public LegalFormDef next(int level) {
        if (!hasNext(level)) {
            throw new IllegalArgumentException("level " + level + " has no next legal form.");
        }
        return forms.get(level);
    }

    public LegalFormDef first() {
        return forms.get(0);
    }

    public int highestLevel() {
        return forms.size();
    }

    public boolean hasNext(int level) {
        return level >= 1 && level < forms.size();
    }

/*
    The following code is AI generated.
     */

    public static LegalForms standard() {
        return new LegalForms(List.of(
                new LegalFormDef(
                        1, "sole_proprietorship",
                        2,                   // employee slots
                        2,                   // product licenses
                        2,                   // open requests per product
                        5,                   // open requests total
                        3,                   // order book
                        0,                   // framework contracts
                        64,                  // max order quantity
                        1.5,                 // deadline factor f_S
                        1_024,               // max grid connection (SU)
                        0,                   // feed-in licence tier
                        Money.ofDollars(1_000),  // overdraft limit
                        Money.ZERO,          // bank loan limit
                        false,               // automatic acceptance
                        false,               // company network, dispatch, framework contracts
                        Money.ZERO,          // free storage
                        true,                // founder protection
                        1),                  // max shipping exits - see NOTE below
                new LegalFormDef(
                        2, "partnership",
                        4, 6, 3, 12, 10, 0, 256, 1.2, 4_096, 1,
                        Money.ofDollars(5_000), Money.ofDollars(20_000),
                        true, false, Money.ZERO, false,
                        2),
                new LegalFormDef(
                        3, "limited_company",
                        6, 15, 5, 30, 40, 6, 1_024, 1.0, 16_384, 2,
                        Money.ofDollars(20_000), Money.ofDollars(100_000),
                        true, true, Money.ofDollars(20_000), false,
                        4),
                new LegalFormDef(
                        4, "public_company",
                        10, UNLIMITED, 8, 80, 150, 20, 4_096, 1.0, 65_536, 3,
                        Money.ofDollars(100_000), Money.ofDollars(500_000),
                        true, true, Money.ofDollars(100_000), false,
                        8)
        ));
    }
}
