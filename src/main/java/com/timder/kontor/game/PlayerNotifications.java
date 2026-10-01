package com.timder.kontor.game;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.UpgradeEvent;
import com.timder.kontor.util.ComponentFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PlayerNotifications {

    public static void sendUpgradeEventNotifications(MinecraftServer server, CompanyRegistry registry, List<UpgradeEvent> events, CompanyParams params) {
        for (UpgradeEvent event : events) {
            Company company = registry.get(event.companyId()).orElse(null);
            if (company == null) {
                continue;
            }

            Component message = switch (event) {
                case UpgradeEvent.Completed completed -> Component.translatable("message.createkontor.upgrade.completed",
                        ComponentFormatting.highlightStandard(company.name()),
                        legalFormName(params, completed.newLevel()));
                case UpgradeEvent.Resting resting -> Component.translatable("message.createkontor.upgrade.resting",
                        ComponentFormatting.highlightStandard(company.name()),
                        legalFormName(params, resting.targetLevel()),
                        ComponentFormatting.highlightStandard(String.valueOf(resting.restingDays())),
                        ComponentFormatting.highlightStandard(minimumNetWorth(params, resting.targetLevel()).toString()));
                case UpgradeEvent.Rejected rejected -> Component.translatable("message.createkontor.upgrade.rejected",
                        ComponentFormatting.highlightError(company.name()),
                        legalFormName(params, rejected.targetLevel()),
                        ComponentFormatting.highlightError(minimumNetWorth(params, rejected.targetLevel()).toString()),
                        ComponentFormatting.highlightError(rejected.feeLost().toString()));
            };
            sendMessageToAllMembers(company, server, message);
        }
    }

    public static void sendUpgradeCancelledNotification(MinecraftServer server, Company company, int targetLevel, Money refund, CompanyParams params) {
        Component message = Component.translatable("message.createkontor.upgrade.cancelled",
                ComponentFormatting.highlightError(company.name()),
                legalFormName(params, targetLevel),
                ComponentFormatting.highlightError(refund.toString()));
        sendMessageToAllMembers(company, server, message);
    }

    public static void sendMessageToAllMembers(Company company, MinecraftServer server, Component message) {
        List<UUID> members = new ArrayList<>();
        members.add(company.owner());
        members.addAll(company.managers());
        for (UUID member : members) {
            ServerPlayer player = server.getPlayerList().getPlayer(member);
            if (player != null) {
                player.displayClientMessage(message, false);
            }
        }
    }

    public static void sendActionBarMessageToAllMembers(Company company, MinecraftServer server, Component message) {
        List<UUID> members = new ArrayList<>();
        members.add(company.owner());
        members.addAll(company.managers());
        for (UUID member : members) {
            ServerPlayer player = server.getPlayerList().getPlayer(member);
            if (player != null) {
                player.displayClientMessage(message, true);
            }
        }
    }

    private static Component legalFormName(CompanyParams params, int level) {
        return Component.translatable("legal_forms.createkontor." + params.legalForms().get(level).id());
    }

    private static Money minimumNetWorth(CompanyParams params, int level) {
        return params.legalForms().get(level).entryRequirements().minNetWorth();
    }

}
