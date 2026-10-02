package com.timder.kontor.game.block.employee;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Optional;

public class EmployeeDeskInteraction {
    @SubscribeEvent
    public static void onVillagerUse(PlayerInteractEvent.EntityInteractSpecific event) {
        Player player = event.getEntity();
        if (player.isSpectator() || !(event.getTarget() instanceof Villager villager) || villager.isBaby()) {
            return;
        }
        Optional<BlockPos> deskPos = EmployeeSeats.findDeskFor(villager);
        if (deskPos.isEmpty()) {
            return;
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);

        if (event.getLevel() instanceof ServerLevel level
                && player instanceof ServerPlayer serverPlayer
                && level.getBlockState(deskPos.get()).getBlock() instanceof AbstractEmployeeDeskBlock desk) {
            desk.tryOpenUi(serverPlayer, level, deskPos.get());
        }
    }
}
