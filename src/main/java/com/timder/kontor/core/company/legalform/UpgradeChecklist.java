package com.timder.kontor.core.company.legalform;

import com.timder.kontor.core.company.financial.Money;

import java.util.*;

public record UpgradeChecklist(UpgradeCheckMode mode, int targetLevel, List<Row> rows) {
    public UpgradeChecklist {
        Objects.requireNonNull(mode, "mode must not be null.");
        if (targetLevel < 2) throw new IllegalArgumentException("targetLevel must be at least 2.");
        Objects.requireNonNull(rows, "rows must not be null.");
        rows = List.copyOf(rows); // also rejects null rows
        if (rows.isEmpty()) throw new IllegalArgumentException("rows must not be empty.");
        Set<UpgradeCriterion> seen = EnumSet.noneOf(UpgradeCriterion.class);
        for (Row row : rows) {
            if (!seen.add(row.criterion())) {
                throw new IllegalArgumentException("more than one row for " + row.criterion() + ".");
            }
        }
    }

    public boolean allMet() {
        return rows.stream().allMatch(Row::met);
    }

    public List<Row> unmetRows() {
        return rows.stream().filter(row -> !row.met()).toList();
    }

    public Optional<Row> row(UpgradeCriterion criterion) {
        Objects.requireNonNull(criterion, "criterion must not be null.");
        return rows.stream().filter(row -> row.criterion() == criterion).findFirst();
    }

    public sealed interface Row permits MoneyRow, CountRow, StarsRow, FlagRow {

        UpgradeCriterion criterion();

        boolean met();
    }

    public record MoneyRow(UpgradeCriterion criterion, Money actual, Money required) implements Row {
        public MoneyRow {
            Objects.requireNonNull(criterion, "criterion must not be null.");
            Objects.requireNonNull(actual, "actual must not be null.");
            Objects.requireNonNull(required, "required must not be null.");
        }

        @Override
        public boolean met() {
            return actual.compareTo(required) >= 0;
        }
    }

    public record CountRow(UpgradeCriterion criterion, int actual, int required) implements Row {
        public CountRow {
            Objects.requireNonNull(criterion, "criterion must not be null.");
        }

        @Override
        public boolean met() {
            return actual >= required;
        }
    }

    public record StarsRow(UpgradeCriterion criterion, double actual, double required) implements Row {
        public static final double TOLERANCE = 1e-9;

        public StarsRow {
            Objects.requireNonNull(criterion, "criterion must not be null.");
            if (Double.isNaN(actual) || Double.isNaN(required)) throw new IllegalArgumentException("stars must not be NaN.");
        }

        @Override
        public boolean met() {
            return actual + TOLERANCE >= required;
        }
    }

    public record FlagRow(UpgradeCriterion criterion, boolean fulfilled) implements Row {
        public FlagRow {
            Objects.requireNonNull(criterion, "criterion must not be null.");
        }

        @Override
        public boolean met() {
            return fulfilled;
        }
    }
}
