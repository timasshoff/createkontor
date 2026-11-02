package com.timder.kontor.game.display;

import static com.timder.kontor.registry.KontorRegistries.REGISTRATE;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import com.tterrag.registrate.util.entry.RegistryEntry;

public class KontorDisplaySources {

    public static final RegistryEntry<DisplaySource, CompanyOverviewDisplaySource> KONTOR_DESK = REGISTRATE
            .generic("company_overview", CreateRegistries.DISPLAY_SOURCE, CompanyOverviewDisplaySource::new)
            .register();
}
