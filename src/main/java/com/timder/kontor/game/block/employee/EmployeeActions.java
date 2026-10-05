package com.timder.kontor.game.block.employee;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

public abstract class EmployeeActions {
    protected final EmployeeDeskBlockEntity desk;
    protected boolean isServer = false;
    protected Company company;
    protected CompanySavedData comapnyData;
    protected Economy economy;
    protected EconomySavedData economyData;

    public EmployeeActions(EmployeeDeskBlockEntity desk) {
        this.desk = Objects.requireNonNull(desk, "desk must not be null.");

        if (desk.getLevel() instanceof ServerLevel) {
            isServer = true;
            this.comapnyData = CompanySavedData.get(desk.getLevel().getServer());
            this.company = comapnyData.getRegistry().get(desk.getCompanyId()).orElse(null);
            this.economyData = EconomySavedData.get(desk.getLevel().getServer());
            this.economy = economyData.getEconomy();
        }
    }
}
