package com.timder.kontor.game.advancements;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.registry.KontorTriggers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Milestones {

    public static void award(ServerPlayer player, String key) {
        KontorTriggers.MILESTONE.get().trigger(player, key);
    }

    public static void awardToMembers(MinecraftServer server, Company company, String key) {
        List<UUID> members = new ArrayList<>();
        members.add(company.owner());
        members.addAll(company.managers());
        for (UUID member : members) {
            ServerPlayer player = server.getPlayerList().getPlayer(member);
            if (player != null) {
                award(player, key);
            }
        }
    }
}
