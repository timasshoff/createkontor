package com.timder.kontor.game.block;

import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.BankLoanRules;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.block.employee.EmployeeActions;
import com.timder.kontor.game.block.employee.EmployeeDeskBlockEntity;
import com.timder.kontor.game.network.S2CActionResult;
import com.timder.kontor.util.ComponentFormatting;
import java.util.Optional;
import net.minecraft.network.chat.Component;

public class FinancesManagerActions extends EmployeeActions {

    public FinancesManagerActions(EmployeeDeskBlockEntity desk) {
        super(desk);
    }

    public static Money requestedAmount(Company company, CompanyParams params, double requestedDollars) {
        return requestedDollars > 0 ? Money.fromDollar(requestedDollars) : BankLoanRules.minimumAmount(company, params);
    }

    public Optional<S2CActionResult> takeLoan(String offerId, double requestedDollars) {
        if (!isServer) return S2CActionResult.illegalEnvironment();
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        Money amount = requestedAmount(company, params, requestedDollars);

        try {
            BankLoanRules.take(
                    company,
                    economy.currentDay(),
                    offerId,
                    amount,
                    economy.policyRate(),
                    economy.overallReputationInStars(company.id()),
                    params);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return S2CActionResult.error(ComponentFormatting.error("Failed: " + e.getMessage()));
        }

        CompanySavedData.get(serverLevel.getServer()).setDirty();
        return S2CActionResult.success(Component.translatable(
                "message.createkontor.finances_manager.loan_taken", ComponentFormatting.moneyColored(amount)));
    }
}
