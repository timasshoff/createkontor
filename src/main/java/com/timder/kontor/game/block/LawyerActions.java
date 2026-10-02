package com.timder.kontor.game.block;

import com.timder.kontor.CreateKontor;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.legalform.UpgradeRules;
import com.timder.kontor.core.company.license.*;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.block.employee.EmployeeDeskBlockEntity;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.util.ComponentFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

public final class LawyerActions {

    private final EmployeeDeskBlockEntity desk;
    private boolean isServer = false;
    private Company company;
    private CompanySavedData comapnyData;
    private Economy economy;
    private EconomySavedData economyData;

    public LawyerActions(EmployeeDeskBlockEntity desk) {
        this.desk = Objects.requireNonNull(desk, "desk must not be null.");

        if (desk.getLevel() instanceof ServerLevel) {
            isServer = true;
            this.comapnyData = CompanySavedData.get(desk.getLevel().getServer());
            this.company = comapnyData.getRegistry().get(desk.getCompanyId()).orElse(null);
            this.economyData = EconomySavedData.get(desk.getLevel().getServer());
            this.economy = economyData.getEconomy();
        }
    }

    public Component applyForUpgrade() {
        if (!isServer) return Component.empty();
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));

        try {
            UpgradeRules.apply(company, economy.overallReputationInStars(company.id()), economy.currentDay(), params);
        } catch (Exception e) {
            return ComponentFormatting.error("Failed: " + e.getMessage());
        }

        return Component.empty();
    }

    public Component buyLicense(LicenseKey key) {
        if (!isServer) return Component.empty();
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        AcquireResult result = LicenseOffers.acquire(company, economy, key, economy.currentDay(), params);
        return Component.empty();
    }

    public Component cancelLicense(LicenseKey key) {
        if (!isServer) return Component.empty();
        LicenseHoldingRules.CancelResult result = LicenseHoldingRules.cancel(company, key);
        if (result != LicenseHoldingRules.CancelResult.CANCELLED) {
            return Component.translatable("enum.createkontor.license_cancel_result." + result.toString().toLowerCase());
        }
        return Component.empty();
    }

}
