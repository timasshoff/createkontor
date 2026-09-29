package com.timder.kontor.game.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.legalform.*;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.KontorTickHandler;
import com.timder.kontor.game.PlayerNotifications;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/*
The following code has been AI generated
 */
public class UpgradeCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kontor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("company")
                                .then(Commands.literal("upgrade")
                                        .then(Commands.literal("check")
                                                .then(Commands.argument("company", StringArgumentType.string())
                                                        .executes(UpgradeCommands::check)))
                                        .then(Commands.literal("apply")
                                                .then(Commands.argument("company", StringArgumentType.string())
                                                        .executes(UpgradeCommands::apply)))
                                        .then(Commands.literal("info")
                                                .then(Commands.argument("company", StringArgumentType.string())
                                                        .executes(UpgradeCommands::info)))
                                        .then(Commands.literal("fastforward")
                                                .then(Commands.argument("company", StringArgumentType.string())
                                                        .then(Commands.argument("ticks", LongArgumentType.longArg(1))
                                                                .executes(UpgradeCommands::fastForward)))))));
    }

    private static int check(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Company company = CommandSupport.findCompany(context, "company");
        if (company == null) {
            return 0;
        }
        CompanyParams params = CommandSupport.companyParams(source);
        if (params == null) {
            return 0;
        }
        LegalForms forms = params.legalForms();
        if (!forms.hasNext(company.legalLevel())) {
            source.sendFailure(Component.literal(company.name() + " is already in the highest legal form."));
            return 0;
        }

        Economy economy = EconomySavedData.get(source.getServer()).getEconomy();
        String target = forms.next(company.legalLevel()).id();
        UpgradeChecklist checklist = UpgradeRules.checkApplication(company, forms.next(company.legalLevel()), economy.overallReputationInStars(company.id()));

        source.sendSuccess(() -> Component.literal(formatChecklist(company, target, checklist)), false);
        return 1;
    }

    private static int apply(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Company company = CommandSupport.findCompany(context, "company");
        if (company == null) {
            return 0;
        }
        CompanyParams params = CommandSupport.companyParams(source);
        if (params == null) {
            return 0;
        }

        CompanySavedData data = CompanySavedData.get(source.getServer());
        Economy economy = EconomySavedData.get(source.getServer()).getEconomy();
        ApplyResult result = UpgradeRules.apply(company, economy.overallReputationInStars(company.id()), economy.currentDay(), params);

        switch (result.status()) {
            case STARTED -> {
                data.setDirty();
                UpgradeApplication application = company.upgradeApplication().orElseThrow();
                String target = params.legalForms().get(application.targetLevel()).id();
                source.sendSuccess(() -> Component.literal(company.name() + " applied for " + target + ". Fee paid: "
                        + params.legalForms().get(application.targetLevel()).entryRequirements().fee()
                        + ". Processing takes " + CommandSupport.formatTicks(application.ticksLeft()) + "."), false);
                return 1;
            }
            case ALREADY_PENDING -> {
                source.sendFailure(Component.literal(company.name() + " already has an application: " + formatApplication(company, params)));
                return 0;
            }
            case NO_NEXT_FORM -> {
                source.sendFailure(Component.literal(company.name() + " is already in the highest legal form."));
                return 0;
            }
            case REQUIREMENTS_NOT_MET -> {
                String target = params.legalForms().next(company.legalLevel()).id();
                source.sendFailure(Component.literal("Requirements not met. Nothing was paid.\n" + formatChecklist(company, target, result.checklist())));
                return 0;
            }
        }
        return 0;
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Company company = CommandSupport.findCompany(context, "company");
        if (company == null) {
            return 0;
        }
        CompanyParams params = CommandSupport.companyParams(source);
        if (params == null) {
            return 0;
        }

        LegalFormDef legalForm = CommandSupport.legalForm(source, company, params);
        if (legalForm == null) {
            return 0;
        }

        String text = "=== Upgrade: " + company.name() + " ===\n"
                + "Legal form: level " + company.legalLevel() + " " + legalForm.id() + "\n"
                + "Fulfilled orders: " + company.fulfilledOrders() + "\n"
                + "Application: " + formatApplication(company, params);
        source.sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static int fastForward(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Company company = CommandSupport.findCompany(context, "company");
        if (company == null) {
            return 0;
        }
        CompanyParams params = CommandSupport.companyParams(source);
        if (params == null) {
            return 0;
        }
        if (company.upgradeApplication().isEmpty()) {
            source.sendFailure(Component.literal(company.name() + " has no application."));
            return 0;
        }

        long ticks = LongArgumentType.getLong(context, "ticks");
        CompanySavedData data = CompanySavedData.get(source.getServer());
        Optional<UpgradeEvent> event;
        try {
            event = UpgradeRules.advance(company, ticks, KontorTickHandler.canProcessUpgrade(company), params);
        } catch (IllegalStateException e) {
            source.sendFailure(Component.literal("Cannot advance the application: " + e.getMessage()));
            return 0;
        }
        data.setDirty();

        if (event.isPresent()) {
            PlayerNotifications.sendUpgradeEventNotifications(source.getServer(), data.getRegistry(), List.of(event.get()), params);
            source.sendSuccess(() -> Component.literal(formatEvent(event.get(), company, params)), false);
        } else {
            source.sendSuccess(() -> Component.literal("Advanced by " + ticks + " ticks. " + formatApplication(company, params)), false);
        }
        return 1;
    }

    private static String formatEvent(UpgradeEvent event, Company company, CompanyParams params) {
        return switch (event) {
            case UpgradeEvent.Completed completed -> company.name() + " advanced to level " + completed.newLevel()
                    + " " + params.legalForms().get(completed.newLevel()).id() + ".";
            case UpgradeEvent.Resting resting -> "Processing finished, but the net worth of " + company.name()
                    + " is too low. The application for " + params.legalForms().get(resting.targetLevel()).id()
                    + " rests for up to " + resting.restingDays() + " day(s).";
            case UpgradeEvent.Rejected rejected -> "The application of " + company.name() + " for "
                    + params.legalForms().get(rejected.targetLevel()).id() + " was rejected. The fee of "
                    + rejected.feeLost() + " is lost.";
        };
    }

    private static String formatApplication(Company company, CompanyParams params) {
        Optional<UpgradeApplication> application = company.upgradeApplication();
        if (application.isEmpty()) {
            return "none";
        }
        UpgradeApplication a = application.get();
        String target = "level " + a.targetLevel() + " " + params.legalForms().get(a.targetLevel()).id();
        if (a.isProcessing()) {
            return "processing for " + target + ", " + CommandSupport.formatTicks(a.ticksLeft()) + " left";
        }
        return "resting for " + target + ", " + a.restingDaysLeft() + " day" + (a.restingDaysLeft() == 1 ? "" : "s") + " left";
    }

    private static String formatChecklist(Company company, String target, UpgradeChecklist checklist) {
        StringBuilder text = new StringBuilder("=== ").append(company.name()).append(" -> ").append(target)
                .append(" (").append(checklist.mode().name().toLowerCase(Locale.ROOT)).append(") ===");
        for (UpgradeChecklist.Row row : checklist.rows()) {
            text.append("\n  ").append(formatRow(checklist.mode(), row));
        }
        int missing = checklist.unmetRows().size();
        text.append("\n").append(missing == 0 ? "All requirements met." : missing + " requirement(s) missing.");
        return text.toString();
    }

    private static String formatRow(UpgradeCheckMode mode, UpgradeChecklist.Row row) {
        String label = switch (row.criterion()) {
            case NET_WORTH -> mode == UpgradeCheckMode.APPLICATION ? "Net worth (minimum plus fee)" : "Net worth (minimum)";
            case FULFILLED_ORDERS -> "Fulfilled orders";
            case REPUTATION -> "Reputation";
            case LIQUIDITY -> "No payment difficulties";
        };
        String values = switch (row) {
            case UpgradeChecklist.MoneyRow money -> money.actual() + " / " + money.required();
            case UpgradeChecklist.CountRow count -> count.actual() + " / " + count.required();
            case UpgradeChecklist.StarsRow stars -> String.format(Locale.ROOT, "%.2f / %.2f stars", stars.actual(), stars.required());
            case UpgradeChecklist.FlagRow flag -> flag.fulfilled() ? "yes" : "no";
        };
        return label + ": " + values + (row.met() ? "  [OK]" : "  [MISSING]");
    }
}
