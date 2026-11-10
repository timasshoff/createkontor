package com.timder.kontor.registry;

import static com.timder.kontor.registry.KontorRegistries.REGISTRATE;

import com.timder.kontor.game.advancement.MilestoneTrigger;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;

public final class KontorTriggers {
    public static final RegistryEntry<CriterionTrigger<?>, MilestoneTrigger> MILESTONE =
            REGISTRATE.simple("milestone", Registries.TRIGGER_TYPE, MilestoneTrigger::new);

    static void touch() {}
}
