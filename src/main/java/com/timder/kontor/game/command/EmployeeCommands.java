package com.timder.kontor.game.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.employee.Employee;
import com.timder.kontor.core.company.employee.EmployeeRules;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.EmployeeContracts;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/*
The following code is AI generated.
 */
public class EmployeeCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kontor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("company")
                                .then(Commands.literal("employee")
                                        .then(Commands.literal("list")
                                                .then(Commands.argument("company", StringArgumentType.string())
                                                        .executes(EmployeeCommands::list)))
                                        .then(Commands.literal("dismiss")
                                                .then(Commands.argument("company", StringArgumentType.string())
                                                        .then(Commands.argument("number", LongArgumentType.longArg(1))
                                                                .executes(EmployeeCommands::dismiss)))))));
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Company company = CommandSupport.findCompany(context, "company");
        if (company == null) {
            return 0;
        }
        CompanyParams params = CommandSupport.companyParams(source);
        if (params == null) {
            return 0;
        }

        StringBuilder text = new StringBuilder("=== Employees: " + company.name() + " ===\n");
        text.append("Slots: ").append(company.employeeCount()).append(" of ").append(company.legalForm(params).maxEmployees())
                .append(", lawyer: ").append(EmployeeContracts.hasLawyer(company) ? "yes" : "no").append('\n');
        if (company.employees().isEmpty()) {
            text.append("No employees.");
        }
        for (Employee employee : company.employees()) {
            text.append('#').append(employee.number()).append(' ').append(employee.role().value())
                    .append(", base salary ").append(employee.baseSalary())
                    .append(", today ").append(EmployeeRules.dailySalary(employee.baseSalary(), company.legalForm(params)))
                    .append(", hired on day ").append(employee.hiredDay())
                    .append(", last report on day ").append(employee.lastReportDay())
                    .append(employee.present() ? " (present)" : " (absent)")
                    .append('\n');
        }
        if (!company.departures().isEmpty()) {
            text.append("Left today or earlier, not yet settled: ").append(company.departures().size());
        }
        String result = text.toString().strip();
        source.sendSuccess(() -> Component.literal(result), false);
        return 1;
    }

    private static int dismiss(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Company company = CommandSupport.findCompany(context, "company");
        if (company == null) {
            return 0;
        }
        CompanyParams params = CommandSupport.companyParams(source);
        if (params == null) {
            return 0;
        }

        long number = LongArgumentType.getLong(context, "number");
        long day = EconomySavedData.get(source.getServer()).getEconomy().currentDay();
        Employee employee;
        try {
            employee = EmployeeRules.dismiss(company, number, day);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
        EmployeeContracts.ended(source.getServer(), company, employee, day, params);
        CompanySavedData.get(source.getServer()).setDirty();

        source.sendSuccess(() -> Component.literal("Dismissed #" + number + " (" + employee.role().value() + ") of " + company.name()
                + ". If a villager still sits at the desk, it is hired again within a second."), false);
        return 1;
    }
}
