package com.timder.kontor.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue SETTLEMENT_HUD_ENABLED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Settlement HUD: the daily settlement shown at the bottom right of the screen")
                .push("settlementHud");

        SETTLEMENT_HUD_ENABLED = builder.comment("Show the settlement hud when a day is settled.")
                .define("enabled", true);

        builder.pop();
        SPEC = builder.build();
    }
}
