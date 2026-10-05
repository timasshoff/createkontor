package com.timder.kontor.game;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.employee.Employee;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.UpgradeApplication;
import com.timder.kontor.core.company.legalform.UpgradeRules;
import com.timder.kontor.game.block.LawyerDeskBlock;
import net.minecraft.server.MinecraftServer;

import java.util.Objects;
import java.util.Optional;

public class EmployeeContracts {

    /**
     * Called when an employee was removed from a company.
     * @param server The server
     * @param company The company
     * @param employee The removed employee
     * @param day The day
     * @param params The company parameters
     */
    public static void ended(MinecraftServer server, Company company, Employee employee, long day, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(employee, "employee must not be null.");
        if (!employee.role().equals(LawyerDeskBlock.ROLE) || hasLawyer(company)) {
            return;
        }

        Optional<UpgradeApplication> application = company.upgradeApplication();
        Optional<Money> refund = UpgradeRules.cancel(company, day, params);
        if (refund.isPresent() && application.isPresent()) {
            PlayerNotifications.sendUpgradeCancelledNotification(server, company, application.get().targetLevel(), refund.get(), params);
        }
    }

    public static boolean hasLawyer(Company company) {
        for (Employee employee : company.employees()) {
            if (employee.role().equals(LawyerDeskBlock.ROLE)) {
                return true;
            }
        }
        return false;
    }
}
