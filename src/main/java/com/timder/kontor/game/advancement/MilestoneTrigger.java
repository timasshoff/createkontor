package com.timder.kontor.game.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class MilestoneTrigger extends SimpleCriterionTrigger<MilestoneTrigger.Instance> {

    public record Instance(Optional<ContextAwarePredicate> player, String milestone)
            implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC
                                .optionalFieldOf("player")
                                .forGetter(Instance::player),
                        Codec.STRING.fieldOf("milestone").forGetter(Instance::milestone))
                .apply(instance, Instance::new));

        boolean matches(String id) {
            return milestone.equals(id);
        }
    }

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, String milestone) {
        trigger(player, instance -> instance.matches(milestone));
    }
}
