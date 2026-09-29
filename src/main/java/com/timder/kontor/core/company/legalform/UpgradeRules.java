package com.timder.kontor.core.company.legalform;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.BookingKind;
import com.timder.kontor.core.company.financial.Money;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    public static ApplyResult apply(Company company, double reputationStars, long day, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        if (company.upgradeApplication().isPresent()) {
            return new ApplyResult(ApplyResult.Status.ALREADY_PENDING, null);
        }
        if (!params.legalForms().hasNext(company.legalLevel())) {
            return new ApplyResult(ApplyResult.Status.NO_NEXT_FORM, null);
        }

        LegalFormDef target = params.legalForms().next(company.legalLevel());
        UpgradeChecklist checklist = checkApplication(company, target, reputationStars);
        if (!checklist.allMet()) {
            return new ApplyResult(ApplyResult.Status.REQUIREMENTS_NOT_MET, checklist);
        }

        Money fee = target.entryRequirements().fee();
        if (fee.isPositive() && !company.trySpend(day, BookingKind.UPGRADE_FEE, fee, target.id(), params)) {
            throw new IllegalStateException("Every requirement is met, but the company cannot pay the fee of " + fee + ".");
        }

        company.startUpgrade(UpgradeApplication.start(target));
        return new ApplyResult(ApplyResult.Status.STARTED, checklist);
    }

    /**
     * Lets this application process time.
     * @param company The company
     * @param ticks The ticks to pass
     * @param processingAllowed Whether the application may be worked on right now
     * @param params The company parameters
     * @return What happened. Empty, if the application only moved closer to finishing
     */
    public static Optional<UpgradeEvent> advance(Company company, long ticks, boolean processingAllowed, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        if (ticks < 1) throw new IllegalArgumentException("ticks must be at least 1.");

        UpgradeApplication application = company.upgradeApplication().orElse(null);
        if (application == null || !application.isProcessing() || !processingAllowed) {
            return Optional.empty();
        }

        Optional<UpgradeApplication> remaining = application.afterTicks(ticks);
        if (remaining.isPresent()) {
            company.replaceUpgrade(remaining.get());
            return Optional.empty();
        }

        LegalFormDef target = targetOf(company, application, params);
        if (checkCompletion(company, target).allMet()) {
            return Optional.of(complete(company, target, params));
        }
        int restingDays = target.entryRequirements().restingDays();
        if (restingDays == 0) {
            return Optional.of(reject(company, target));
        }
        company.replaceUpgrade(UpgradeApplication.resting(target.level(), restingDays));
        return Optional.of(new UpgradeEvent.Resting(company.id(), target.level(), restingDays));
    }

    /**
     * To be called at every day change after the day has been settled.
     * Will only affect a resting application.
     * @param company The company
     * @param params The company parameters
     * @return What happened. Empty, if nothing happened or the application just keeps resting.
     */
    public static Optional<UpgradeEvent> advanceDay(Company company, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        UpgradeApplication application = company.upgradeApplication().orElse(null);
        if (application == null || !application.isResting()) {
            return Optional.empty();
        }

        LegalFormDef target = targetOf(company, application, params);
        if (checkCompletion(company, target).allMet()) {
            return Optional.of(complete(company, target, params));
        }
        Optional<UpgradeApplication> next = application.afterRestingDay();
        if (next.isPresent()) {
            company.replaceUpgrade(next.get());
            return Optional.empty();
        }
        return Optional.of(reject(company, target));
    }

    private static UpgradeEvent complete(Company company, LegalFormDef target, CompanyParams params) {
        company.setLegalLevel(target.level(), params.legalForms());
        return new UpgradeEvent.Completed(company.id(), target.level());
    }

    private static UpgradeEvent reject(Company company, LegalFormDef target) {
        company.discardUpgrade();
        return new UpgradeEvent.Rejected(company.id(), target.level(), target.entryRequirements().fee());
    }

    private static UpgradeRequirements requirementsFor(Company company, LegalFormDef target) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(target, "target must not be null.");
        if (target.level() != company.legalLevel() + 1) {
            throw new IllegalArgumentException("A company of level " + company.legalLevel() + " can only advance into level " + (company.legalLevel() + 1) + ", not " + target.level() + ".");
        }
        return target.entryRequirements();
    }

    private static LegalFormDef targetOf(Company company, UpgradeApplication application, CompanyParams params) {
        if (!params.legalForms().hasNext(application.targetLevel() - 1)) {
            throw new IllegalStateException("The application of " + company.name() + " aims at level " + application.targetLevel() + ", which does not exist any more.");
        }
        return params.legalForms().get(application.targetLevel());
    }
}
