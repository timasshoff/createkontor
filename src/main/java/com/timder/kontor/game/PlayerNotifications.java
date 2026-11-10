package com.timder.kontor.game;

import com.timder.kontor.core.company.*;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.UpgradeEvent;
import com.timder.kontor.game.advancement.Milestones;
import com.timder.kontor.game.network.SettlementPayload;
import com.timder.kontor.util.ComponentFormatting;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PlayerNotifications {

    public static void sendUpgradeEventNotifications(
            MinecraftServer server, CompanyRegistry registry, List<UpgradeEvent> events, CompanyParams params) {
        for (UpgradeEvent event : events) {
            Company company = registry.get(event.companyId()).orElse(null);
            if (company == null) {
                continue;
            }

            Component message = switch (event) {
                case UpgradeEvent.Completed completed:
                    Milestones.awardToMembers(server, company, "legal_form_upgrade");
                    yield Component.translatable(
                            "message.createkontor.upgrade.completed",
                            ComponentFormatting.highlightStandard(company.name()),
                            legalFormName(params, completed.newLevel()));
                case UpgradeEvent.Resting resting:
                    yield Component.translatable(
                            "message.createkontor.upgrade.resting",
                            ComponentFormatting.highlightStandard(company.name()),
                            legalFormName(params, resting.targetLevel()),
                            ComponentFormatting.highlightStandard(String.valueOf(resting.restingDays())),
                            ComponentFormatting.highlightStandard(minimumNetWorth(params, resting.targetLevel())
                                    .toString()));
                case UpgradeEvent.Rejected rejected:
                    yield Component.translatable(
                            "message.createkontor.upgrade.rejected",
                            ComponentFormatting.highlightError(company.name()),
                            legalFormName(params, rejected.targetLevel()),
                            ComponentFormatting.highlightError(minimumNetWorth(params, rejected.targetLevel())
                                    .toString()),
                            ComponentFormatting.highlightError(
                                    rejected.feeLost().toString()));
            };
            sendMessageToAllMembers(company, server, message);
        }
    }

    public static void sendNewDayNotification(MinecraftServer server, long day) {
        Component message = Component.translatable(
                "message.createkontor.new_day", ComponentFormatting.highlightStandard(String.valueOf(day)));
        server.getPlayerList().broadcastSystemMessage(message, false);
    }

    public static void sendSettlementHud(
            MinecraftServer server, CompanyRegistry registry, Map<CompanyId, CompanyHistoryEntry> entries) {
        for (Map.Entry<CompanyId, CompanyHistoryEntry> entry : entries.entrySet()) {
            Company company = registry.get(entry.getKey()).orElse(null);
            if (company == null) {
                continue;
            }

            List<UUID> members = new ArrayList<>();
            members.add(company.owner());
            members.addAll(company.managers());

            boolean recordDay = CompanyRules.isRecordDay(company.history(), entry.getValue());
            int profitStreak = CompanyRules.profitStreak(company.history(), entry.getValue());
            boolean comeback = CompanyRules.isComeback(company.history(), entry.getValue());

            if (profitStreak > 0) {
                Milestones.awardToMembers(server, company, "first_profit");
            }
            if (profitStreak >= 360) {
                Milestones.awardToMembers(server, company, "max_profit_streak");
            }
            if (recordDay) {
                Milestones.awardToMembers(server, company, "record_day");
            }

            for (UUID member : members) {
                ServerPlayer player = server.getPlayerList().getPlayer(member);
                if (player != null) {
                    PacketDistributor.sendToPlayer(
                            player,
                            SettlementPayload.of(company.name(), entry.getValue(), recordDay, profitStreak, comeback));
                }
            }
        }
    }

    public static void sendUpgradeCancelledNotification(
            MinecraftServer server, Company company, int targetLevel, Money refund, CompanyParams params) {
        Component message = Component.translatable(
                "message.createkontor.upgrade.cancelled",
                ComponentFormatting.highlightError(company.name()),
                legalFormName(params, targetLevel),
                ComponentFormatting.highlightError(refund.toString()));
        sendMessageToAllMembers(company, server, message);
    }

    public static void sendLogisticsConnectedNotification(MinecraftServer server, Company company) {
        Component message = Component.translatable(
                "message.createkontor.logistics.connected", ComponentFormatting.highlightStandard(company.name()));
        sendMessageToAllMembers(company, server, message);
    }

    public static void sendLogisticsDisconnectedNotification(
            MinecraftServer server, Company company, Component reason, Money booked) {
        Component message = Component.translatable(
                "message.createkontor.logistics.disconnected",
                ComponentFormatting.highlightError(company.name()),
                Component.empty().append(reason).withStyle(ChatFormatting.DARK_RED),
                ComponentFormatting.highlightError(booked.toString()));
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
        return Component.translatable(
                "legal_forms.createkontor." + params.legalForms().get(level).id());
    }

    private static Money minimumNetWorth(CompanyParams params, int level) {
        return params.legalForms().get(level).entryRequirements().minNetWorth();
    }
}
