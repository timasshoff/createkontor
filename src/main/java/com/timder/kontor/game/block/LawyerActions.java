package com.timder.kontor.game.block;

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
import com.timder.kontor.game.block.employee.EmployeeActions;
import com.timder.kontor.game.block.employee.EmployeeDeskBlockEntity;
import com.timder.kontor.game.network.S2CActionResult;
import com.timder.kontor.util.ComponentFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;
import java.util.Optional;

public final class LawyerActions extends EmployeeActions {

    public LawyerActions(EmployeeDeskBlockEntity desk) {
        super(desk);
    }

    public Optional<S2CActionResult> applyForUpgrade() {
        if (!isServer) return S2CActionResult.illegalEnvironment();
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));

        try {
            UpgradeRules.apply(company, economy.overallReputationInStars(company.id()), economy.currentDay(), params);
        } catch (Exception e) {
            return S2CActionResult.error(ComponentFormatting.error("Failed: " + e.getMessage()));
        }

        CompanySavedData.get(serverLevel.getServer()).setDirty();
        return S2CActionResult.empty();
    }

    public Optional<S2CActionResult> buyLicense(LicenseKey key) {
        if (!isServer) return S2CActionResult.illegalEnvironment();
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        AcquireResult result = LicenseOffers.acquire(company, economy, key, economy.currentDay(), params);
        if (!result.success()) {
            return S2CActionResult.error(Component.translatable("enum.createkontor.acquire_result." + result.status().toString().toLowerCase()));
        }
        CompanySavedData.get(serverLevel.getServer()).setDirty();
        return S2CActionResult.empty();
    }

    public Optional<S2CActionResult> cancelLicense(LicenseKey key) {
        if (!isServer) return S2CActionResult.illegalEnvironment();
        LicenseHoldingRules.CancelResult result = LicenseHoldingRules.cancel(company, key);
        if (result != LicenseHoldingRules.CancelResult.CANCELLED) {
            return S2CActionResult.error(Component.translatable("enum.createkontor.license_cancel_result." + result.toString().toLowerCase()));
        }
        CompanySavedData.get(serverLevel.getServer()).setDirty();
        return S2CActionResult.empty();
    }

}
