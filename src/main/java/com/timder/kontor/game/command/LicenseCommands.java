package com.timder.kontor.game.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.LegalFormDef;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.license.*;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.market.MarketParticipationRules;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.util.LicenseNames;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/*
The following code is AI generated.
 */
public class LicenseCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kontor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("license")
                                .then(Commands.literal("list")
                                        .then(Commands.argument("company", StringArgumentType.string())
                                                .executes(LicenseCommands::list)))
                                .then(Commands.literal("held")
                                        .then(Commands.argument("company", StringArgumentType.string())
                                                .executes(LicenseCommands::held)))
                                .then(Commands.literal("buy")
                                        .then(Commands.argument("company", StringArgumentType.string())
                                                .then(Commands.argument("license", StringArgumentType.greedyString())
                                                        .suggests(LicenseCommands::suggestCatalog)
                                                        .executes(LicenseCommands::buy))))
                                .then(Commands.literal("cancel")
                                        .then(Commands.argument("company", StringArgumentType.string())
                                                .then(Commands.argument("license", StringArgumentType.greedyString())
                                                        .suggests(LicenseCommands::suggestHeld)
                                                        .executes(LicenseCommands::cancel))))));
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
        Economy economy = EconomySavedData.get(source.getServer()).getEconomy();

        List<LicenseOffer> offers;
        try {
            offers = LicenseOffers.list(company, economy, params);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("The license list is unavailable: " + e.getMessage()));
            return 0;
        }

        MutableComponent text = Component.literal("=== Licenses for " + company.name() + " ===");
        if (offers.isEmpty()) {
            text.append(Component.literal("\nThe catalog is empty."));
        }
        for (LicenseOffer offer : offers) {
            LicenseDef license = offer.license();
            String share = license.hasRevenueShare() ? String.format(Locale.ROOT, " | Revenue share: %.1f%%", license.revenueShare() * 100.0) : "";
            text.append(Component.literal("\n"))
                    .append(LicenseNames.of(license.key()))
                    .append(Component.literal(String.format(Locale.ROOT,
                            " [%s]\n    Markets: %s\n    Daily fee: %s%s | Application fee: %s | Legal level: %d\n    %s",
                            keyText(license.key()), marketsText(license.markets()),
                            offer.dailyFee(), share, offer.applicationFee(), license.minLegalLevel(),
                            statusText(offer))));
        }

        source.sendSuccess(() -> text, false);
        return 1;
    }

    private static int held(CommandContext<CommandSourceStack> context) {
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
        LicenseCatalog catalog = params.licenses();

        List<HeldLicense> held = LicenseHoldingRules.holdings(company, catalog);
        MutableComponent text = Component.literal("=== Licenses of " + company.name() + " ===");
        if (held.isEmpty()) {
            text.append(Component.literal("\nNone."));
        }
        for (HeldLicense license : held) {
            String flags = "";
            if (license.cancelled()) {
                flags += license.billed() ? ", cancelled, billed one last time" : ", cancelled";
            }
            if (!license.known()) {
                flags += ", no longer in the catalog (no fee, no trading)";
            }
            text.append(Component.literal("\n"))
                    .append(LicenseNames.of(license.key()))
                    .append(Component.literal(String.format(Locale.ROOT, " [%s]\n    Since day %d | Daily fee (locked at purchase): %s%s",
                            keyText(license.key()), license.acquiredDay(), license.dailyFee(), flags)));
        }
        text.append(Component.literal(String.format(Locale.ROOT, "\nActive licenses: %d of %s | Daily license fees: %s (revenue shares not included)",
                LicenseHoldingRules.activeCount(company, catalog), limitText(legalForm),
                LicenseHoldingRules.totalDailyFee(company, catalog))));

        source.sendSuccess(() -> text, false);
        return 1;
    }

    private static int buy(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        Company company = CommandSupport.findCompany(context, "company");
        if (company == null) {
            return 0;
        }
        CompanyParams params = CommandSupport.companyParams(source);
        if (params == null) {
            return 0;
        }
        LicenseKey key = findKey(context, company, params);
        if (key == null) {
            return 0;
        }
        Economy economy = EconomySavedData.get(server).getEconomy();

        AcquireResult result;
        try {
            result = LicenseOffers.acquire(company, economy, key, economy.currentDay(), params);
        } catch (IllegalArgumentException | IllegalStateException e) {
            source.sendFailure(Component.literal("The license could not be bought: " + e.getMessage()));
            return 0;
        }

        MutableComponent name = LicenseNames.of(key);
        if (!result.success()) {
            String fee = result.fee().isPositive() ? " (application fee " + result.fee() + ")" : "";
            source.sendFailure(Component.literal(company.name() + " cannot get ").append(name)
                    .append(Component.literal(": " + describe(result.status(), params.licenses().find(key)) + fee + ".")));
            return 0;
        }

        CompanySavedData.get(server).setDirty();
        Money lockedFee = company.license(key).orElseThrow().dailyFee();
        MutableComponent message = Component.literal(company.name() + (result.status() == AcquireResult.Status.REACTIVATED ? " reactivated " : " bought "))
                .append(name);
        if (result.status() == AcquireResult.Status.REACTIVATED) {
            message.append(Component.literal(". Nothing was paid, the daily fee stays " + lockedFee + "."));
        } else {
            message.append(Component.literal(". Application fee: " + result.fee() + ", daily fee (locked): " + lockedFee
                    + ". Use /kontor market join to start trading."));
        }
        source.sendSuccess(() -> message, false);
        return 1;
    }

    private static int cancel(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        Company company = CommandSupport.findCompany(context, "company");
        if (company == null) {
            return 0;
        }
        CompanyParams params = CommandSupport.companyParams(source);
        if (params == null) {
            return 0;
        }
        LicenseKey key = findKey(context, company, params);
        if (key == null) {
            return 0;
        }
        EconomySavedData economyData = EconomySavedData.get(server);

        MarketParticipationRules.CancelOutcome outcome;
        try {
            outcome = MarketParticipationRules.cancelLicense(company, economyData.getEconomy(), key, params);
        } catch (IllegalArgumentException | IllegalStateException e) {
            source.sendFailure(Component.literal("The license could not be cancelled: " + e.getMessage()));
            return 0;
        }

        MutableComponent name = LicenseNames.of(key);
        switch (outcome.result()) {
            case NOT_HELD -> {
                source.sendFailure(Component.literal(company.name() + " does not own ").append(name).append(Component.literal(".")));
                return 0;
            }
            case ALREADY_CANCELLED -> {
                source.sendFailure(Component.literal(company.name() + " has already cancelled ").append(name).append(Component.literal(".")));
                return 0;
            }
            case CANCELLED -> {
                CompanySavedData.get(server).setDirty();
                economyData.setDirty();
                String withdrawn = outcome.withdrawnMarkets().isEmpty()
                        ? "No market was left."
                        : "Left the markets (active or paused, reputation and open requests are gone): " + marketsText(outcome.withdrawnMarkets()) + ".";
                source.sendSuccess(() -> Component.literal(company.name() + " cancelled ").append(name)
                        .append(Component.literal(". The daily fee is billed one last time. " + withdrawn)), false);
                return 1;
            }
        }
        return 0;
    }

    /**
     * The text the players type to name a license: the id of a defined license, the item of a generated one.
     */
    static String keyText(LicenseKey key) {
        return switch (key) {
            case LicenseKey.Defined defined -> defined.id();
            case LicenseKey.Generated generated -> generated.market().value();
        };
    }

    private static String marketsText(Iterable<ItemId> markets) {
        List<String> names = new ArrayList<>();
        for (ItemId market : markets) {
            names.add(market.value());
        }
        return String.join(", ", names);
    }

    private static String limitText(LegalFormDef legalForm) {
        return legalForm.maxProductLicenses() == LegalFormDef.UNLIMITED ? "unlimited" : String.valueOf(legalForm.maxProductLicenses());
    }

    private static String statusText(LicenseOffer offer) {
        String status = describe(offer.status(), Optional.of(offer.license()));
        if (offer.lockedDailyFee().isPresent()) {
            status += " (daily fee locked at " + offer.lockedDailyFee().get() + ")";
        }
        if (offer.status() != AcquireResult.Status.ALREADY_HELD && offer.coversNothingNew()) {
            status += " - adds no new market";
        }
        return status;
    }

    private static String describe(AcquireResult.Status status, Optional<LicenseDef> license) {
        return switch (status) {
            case ACQUIRED -> "available";
            case REACTIVATED -> "cancelled, reactivating is free";
            case ALREADY_HELD -> "already held";
            case UNKNOWN_LICENSE -> "no such license";
            case LEGAL_LEVEL_TOO_LOW -> "legal level " + license.map(LicenseDef::minLegalLevel).orElse(0) + " needed";
            case LIMIT_REACHED -> "the limit of the legal form is reached";
            case NOT_OPERATIONAL -> "the company is in payment difficulties";
            case CANNOT_AFFORD -> "the company cannot afford the application fee";
        };
    }

    /**
     * Finds the license the sender typed: first in the catalog, then among the licenses the company owns, so a license
     * that is gone from the catalog can still be cancelled.
     */
    private static LicenseKey findKey(CommandContext<CommandSourceStack> context, Company company, CompanyParams params) {
        String text = StringArgumentType.getString(context, "license").trim();
        for (LicenseDef license : params.licenses().all()) {
            if (keyText(license.key()).equals(text)) {
                return license.key();
            }
        }
        for (LicenseHolding holding : company.licenses()) {
            if (keyText(holding.key()).equals(text)) {
                return holding.key();
            }
        }
        context.getSource().sendFailure(Component.literal("No license \"" + text + "\". Use /kontor license list to see the ids."));
        return null;
    }

    private static CompletableFuture<Suggestions> suggestCatalog(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        try {
            CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
            return SharedSuggestionProvider.suggest(params.licenses().all().stream().map(license -> keyText(license.key())).toList(), builder);
        } catch (IllegalArgumentException e) {
            return builder.buildFuture();
        }
    }

    private static CompletableFuture<Suggestions> suggestHeld(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        try {
            String name = StringArgumentType.getString(context, "company");
            return CompanySavedData.get(context.getSource().getServer()).getRegistry().findByName(name)
                    .map(company -> SharedSuggestionProvider.suggest(
                            company.licenses().stream().map(holding -> keyText(holding.key())).collect(Collectors.toList()), builder))
                    .orElseGet(builder::buildFuture);
        } catch (IllegalArgumentException e) {
            return builder.buildFuture();
        }
    }
}
