package com.timder.kontor.core.company.legalform;

import com.timder.kontor.core.company.Company;

import java.util.List;
import java.util.Objects;

public final class UpgradeRules {

    public static UpgradeChecklist checkApplication(Company company, LegalFormDef target, double reputationStars) {
        UpgradeRequirements requirements = requirementsFor(company, target);

        if (Double.isNaN(reputationStars)
                || reputationStars < UpgradeRequirements.MIN_STARS - UpgradeChecklist.StarsRow.TOLERANCE
                || reputationStars > UpgradeRequirements.MAX_STARS + UpgradeChecklist.StarsRow.TOLERANCE) {
            throw new IllegalArgumentException("reputationStars must be between " + UpgradeRequirements.MIN_STARS + " and " + UpgradeRequirements.MAX_STARS + ".");
        }

        List<UpgradeChecklist.Row> rows = List.of(
                new UpgradeChecklist.MoneyRow(UpgradeCriterion.NET_WORTH, company.netWorth(), requirements.moneyNeededToApply()),
                new UpgradeChecklist.CountRow(UpgradeCriterion.FULFILLED_ORDERS, (int) company.fulfilledOrders(), requirements.minFulfilledOrders()),
                new UpgradeChecklist.StarsRow(UpgradeCriterion.REPUTATION, reputationStars, requirements.minReputationStars()),
                new UpgradeChecklist.FlagRow(UpgradeCriterion.LIQUIDITY, company.isOperational())
        );

        return new UpgradeChecklist(UpgradeCheckMode.APPLICATION, target.level(), rows);
    }

    public static UpgradeChecklist checkCompletion(Company company, LegalFormDef target) {
        UpgradeRequirements requirements = requirementsFor(company, target);
        List<UpgradeChecklist.Row> rows = List.of(new UpgradeChecklist.MoneyRow(UpgradeCriterion.NET_WORTH, company.netWorth(), requirements.minNetWorth()));
        return new UpgradeChecklist(UpgradeCheckMode.COMPLETION, target.level(), rows);
    }

    private static UpgradeRequirements requirementsFor(Company company, LegalFormDef target) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(target, "target must not be null.");
        if (target.level() != company.legalLevel() + 1) {
            throw new IllegalArgumentException("A company of level " + company.legalLevel() + " can only advance into level " + (company.legalLevel() + 1) + ", not " + target.level() + ".");
        }
        return target.entryRequirements();
    }

}
