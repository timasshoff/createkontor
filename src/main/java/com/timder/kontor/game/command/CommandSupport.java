package com.timder.kontor.game.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.config.EconomyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.LegalFormDef;
import com.timder.kontor.core.company.LegalForms;
import com.timder.kontor.core.company.request.RequestParams;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.CompanySavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.Optional;

final class CommandSupport {

    /** 20 ticks per second, 60 seconds per minute. */
    static final double TICKS_PER_MINUTE = 1_200.0;

    /**
     * Looks up the company named by a string argument. Tells the sender if there is none.
     * @param context The command context
     * @param argumentName The name of the string argument
     * @return The company, or null if there is none
     */
    static Company findCompany(CommandContext<CommandSourceStack> context, String argumentName) {
        String name = StringArgumentType.getString(context, argumentName);
        Optional<Company> company = CompanySavedData.get(context.getSource().getServer()).getRegistry().findByName(name);
        if (company.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No company named \"" + name + "\"."));
            return null;
        }
        return company.get();
    }

    /**
     * @param source The sender, told if the legal form data is not available
     * @return The company parameters, or null if the legal form data is not available
     */
    static CompanyParams companyParams(CommandSourceStack source) {
        try {
            return CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Legal form data unavailable: " + e.getMessage()));
            return null;
        }
    }

    static RequestParams requestParams(CommandSourceStack source) {
        try {
            return EconomyConfig.toRequestParams();
        } catch (IllegalArgumentException | IllegalStateException e) {
            source.sendFailure(Component.literal("Request settings unavailable: " + e.getMessage()));
            return null;
        }
    }

    /**
     * @param source The sender, told if the company's legal form is not available
     * @param company The company
     * @param params The company parameters
     * @return The company's legal form, or null if it is not available
     */
    static LegalFormDef legalForm(CommandSourceStack source, Company company, CompanyParams params) {
        try {
            return company.legalForm(params);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Legal form data unavailable: " + e.getMessage()));
            return null;
        }
    }

    /**
     * @param ticks A time in ticks
     * @return The time as ticks and minutes, for example "2400 ticks (2.0 min)"
     */
    static String formatTicks(long ticks) {
        return String.format(Locale.ROOT, "%d ticks (%.1f min)", ticks, ticks / TICKS_PER_MINUTE);
    }
}
