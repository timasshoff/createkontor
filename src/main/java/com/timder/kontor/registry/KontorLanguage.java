package com.timder.kontor.registry;

import com.timder.kontor.game.block.company.CompanyBlockSupport;

import static com.timder.kontor.registry.KontorRegistries.REGISTRATE;

public class KontorLanguage {

    static {
        REGISTRATE.addRawLang("createkontor.configuration.title", "Create: Kontor Configs");
        REGISTRATE.addRawLang("itemGroup.createkontor", "Create: Kontor");

        REGISTRATE.addRawLang(CompanyBlockSupport.MESSAGE_BOUND, "§7Connected to %s");
        REGISTRATE.addRawLang(CompanyBlockSupport.MESSAGE_NOT_IN_COMPANY, "§cYou are not a member of any company. The block stays unconnected.");
        REGISTRATE.addRawLang(CompanyBlockSupport.MESSAGE_UNBOUND, "§cThe block is not connected to a company.");
        REGISTRATE.addRawLang(CompanyBlockSupport.MESSAGE_COMPANY_GONE, "§cThe company of this block no longer exists.");
        REGISTRATE.addRawLang(CompanyBlockSupport.MESSAGE_NOT_MEMBER, "§cThis block belongs to another company.");
        REGISTRATE.addRawLang(CompanyBlockSupport.MESSAGE_STATUS, "§7Company: %s");
        REGISTRATE.addRawLang("message.createkontor.already_member", "§cYou are already a member of a company.");
        REGISTRATE.addRawLang("message.createkontor.shipping_exit.limit", "§cLimit reached: Legal form allows at most %s §cbound shipping exits.");
        REGISTRATE.addRawLang("message.createkontor.kontor_desk.limit", "§cThis company already has a Kontor Desk.");

        REGISTRATE.addRawLang(CompanyBlockSupport.GOGGLE_COMPANY_BLOCK, "Company");
        REGISTRATE.addRawLang(CompanyBlockSupport.GOGGLE_COMPANY_BLOCK_UNBOUND, "Not bound to any company.");

        REGISTRATE.addRawLang("ui.createkontor.chart.no_data.detailed", "There is no data for the chart \"%s\" yet. You can try again later.");
        REGISTRATE.addRawLang("ui.createkontor.chart.no_data.short", "There is no data for this chart yet. You can try again later.");

        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.business_fee.info", "§7A business fee of %s is charged every day and debited from the company's bank account.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.deposit.info", "§7Every company receives a free deposit of %s that does not need to be payed back.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.founders_loan.info", "§7Additionally, you can take a founders loan of %s with %s free days before you need to repay it.");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.founding", "Found company");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.founding_title", "Found a new company");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.company_name", "Company Name");
        REGISTRATE.addRawLang("ui.createkontor.kontor_desk.open_orders", "Open Orders: %s");

        REGISTRATE.addRawLang("enum.createkontor.liquidity.normal", "§2Normal Liquidity");
        REGISTRATE.addRawLang("enum.createkontor.liquidity.illiquidity", "§4Illiquidity");

        REGISTRATE.addRawLang("legal_forms.createkontor.sole_proprietorship", "§7Sole Proprietorship");
        REGISTRATE.addRawLang("legal_forms.createkontor.partnership", "§bPartnership");
        REGISTRATE.addRawLang("legal_forms.createkontor.limited_company", "§5Limited Company");
        REGISTRATE.addRawLang("legal_forms.createkontor.public_company", "§6Public Company");

        REGISTRATE.addRawLang("economy.createkontor.current_day", "§7Day %s");
    }

    static void touch() {
    }

    private KontorLanguage() {
    }
}
