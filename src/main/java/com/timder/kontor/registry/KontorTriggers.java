package com.timder.kontor.registry;

import com.timder.kontor.game.advancements.MilestoneTrigger;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;

import static com.timder.kontor.registry.KontorRegistries.REGISTRATE;

public final class KontorTriggers {
    public static final RegistryEntry<CriterionTrigger<?>, MilestoneTrigger> MILESTONE =
            REGISTRATE.simple("milestone", Registries.TRIGGER_TYPE, MilestoneTrigger::new);

    static void touch() {}
}
