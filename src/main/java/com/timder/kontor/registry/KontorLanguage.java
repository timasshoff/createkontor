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
        REGISTRATE.addRawLang("message.createkontor.shipping_exit.limit", "§cLimit reached: Legal form allows at most %s §cbound shipping exits.");

        REGISTRATE.addRawLang(CompanyBlockSupport.GOGGLE_COMPANY_BLOCK, "Company");
        REGISTRATE.addRawLang(CompanyBlockSupport.GOGGLE_COMPANY_BLOCK_UNBOUND, "Not bound to any company.");
    }

    static void touch() {
    }

    private KontorLanguage() {
    }
}
