package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.config.EconomyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.market.MarketParticipationRules;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.block.company.AbstractCompanyBlockEntity;
import com.timder.kontor.game.block.company.CompanyBlockSupport;
import com.timder.kontor.game.network.S2CActionResult;
import com.timder.kontor.game.ui.KontorDeskBoundUi;
import com.timder.kontor.game.ui.KontorDeskUnboundUi;
import com.timder.kontor.util.ComponentFormatting;
import com.timder.kontor.util.PlayerNotifications;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class KontorDeskBlockEntity extends AbstractCompanyBlockEntity {

    // This is necessary to fix an exploit where a player can add himself as a manager of a company
    private static final byte[] INVITE_KEY = new byte[32];

    static {
        new SecureRandom().nextBytes(INVITE_KEY);
    }

    private static final long INVITE_VALID_MILLIS = 10 * 60 * 1000L;

    public KontorDeskBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected @Nullable String boundResourceKey() {
        return KontorDeskBlock.RESOURCE_KEY;
    }

    @Override
    protected boolean loadsChunks() {
        return true;
    }

    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        if (getCompanyId() != null) {
            Company company = holder.player.level() instanceof ServerLevel serverLevel
                    ? CompanyBlockSupport.requireMember(serverLevel, holder.pos, (ServerPlayer) holder.player)
                    : null;
            Economy economy = holder.player.level() instanceof ServerLevel serverLevel
                    ? EconomySavedData.get(serverLevel.getServer()).getEconomy()
                    : null;

            return KontorDeskBoundUi.create(holder, this, company, economy);
        }

        return KontorDeskUnboundUi.create(holder, this);
    }

    public Optional<S2CActionResult> foundNewCompany(ServerPlayer player, String name, boolean takeLoan) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return S2CActionResult.illegalEnvironment();
        }
        MinecraftServer server = serverLevel.getServer();
        var data = CompanySavedData.get(server);
        CompanyRegistry registry = data.getRegistry();
        Economy economy = EconomySavedData.get(server).getEconomy();
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));

        Company company;
        try {
            company = registry.found(
                    name, economy.currentDay(), player.getUUID(), takeLoan, economy.policyRate(), params);
        } catch (IllegalArgumentException e) {
            return S2CActionResult.error(ComponentFormatting.error("Founding failed: " + e.getMessage()));
        }
        data.setDirty();
        setCompanyId(company.id());
        CompanyBlockSupport.message(
                player, CompanyBlockSupport.MESSAGE_BOUND, ComponentFormatting.highlightStandard(company.name()));
        player.closeContainer();
        return S2CActionResult.success(Component.translatable(
                "ui.createkontor.kontor_desk.founded", ComponentFormatting.highlightStandard(company.name())));
    }

    public Optional<S2CActionResult> acceptRequest(long number) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return S2CActionResult.illegalEnvironment();
        }
        MinecraftServer server = serverLevel.getServer();
        var data = CompanySavedData.get(server);
        CompanyRegistry registry = data.getRegistry();
        Company company = registry.get(getCompanyId()).orElse(null);
        if (company == null) {
            return S2CActionResult.error(
                    ComponentFormatting.errorTranslatable(CompanyBlockSupport.MESSAGE_COMPANY_GONE));
        }

        try {
            company.acceptRequest(
                    number,
                    CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions())),
                    EconomyConfig.toRequestParams());
        } catch (IllegalStateException e) {
            return S2CActionResult.error(Component.translatable(
                    "message.createkontor.kontor_desk.request_accepting_failed",
                    ComponentFormatting.highlightError("#" + number),
                    ComponentFormatting.error(e.getMessage())));
        }
        return S2CActionResult.empty();
    }

    public Optional<S2CActionResult> setMarketParticipation(ItemId market, boolean participate) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return S2CActionResult.illegalEnvironment();
        }
        MinecraftServer server = serverLevel.getServer();
        CompanySavedData companyData = CompanySavedData.get(server);
        Company company = companyData.getRegistry().get(getCompanyId()).orElse(null);
        if (company == null) {
            return S2CActionResult.error(
                    ComponentFormatting.errorTranslatable(CompanyBlockSupport.MESSAGE_COMPANY_GONE));
        }

        EconomySavedData economyData = EconomySavedData.get(server);
        Economy economy = economyData.getEconomy();
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));

        try {
            if (participate) {
                MarketParticipationRules.join(company, economy, market, params);
            } else {
                MarketParticipationRules.pause(company, economy, market);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            return S2CActionResult.error(ComponentFormatting.error(e.getMessage()));
        }
        economyData.setDirty();
        companyData.setDirty();
        return S2CActionResult.empty();
    }

    public Optional<S2CActionResult> setListPrice(ItemId market, long priceCents) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return S2CActionResult.illegalEnvironment();
        }
        MinecraftServer server = serverLevel.getServer();
        Company company =
                CompanySavedData.get(server).getRegistry().get(getCompanyId()).orElse(null);
        if (company == null) {
            return S2CActionResult.error(
                    ComponentFormatting.errorTranslatable(CompanyBlockSupport.MESSAGE_COMPANY_GONE));
        }

        EconomySavedData economyData = EconomySavedData.get(server);
        Economy economy = economyData.getEconomy();
        if (!economy.isParticipant(market, company.id())) {
            return S2CActionResult.error(
                    ComponentFormatting.error("The company does not actively take part in " + market + "."));
        }

        try {
            economy.updateListPrice(
                    market, company.id(), Money.ofCents(priceCents).toDollars());
        } catch (IllegalArgumentException e) {
            return S2CActionResult.error(ComponentFormatting.error(e.getMessage()));
        }
        economyData.setDirty();
        return S2CActionResult.empty();
    }

    public Optional<S2CActionResult> declareBankruptcy(ServerPlayer player, boolean confirmed) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return S2CActionResult.illegalEnvironment();
        }
        if (!confirmed) {
            return S2CActionResult.error(Component.translatable("message.createkontor.bankruptcy.not_confirmed"));
        }

        MinecraftServer server = serverLevel.getServer();
        CompanySavedData companyData = CompanySavedData.get(server);
        Company company = companyData.getRegistry().get(getCompanyId()).orElse(null);
        if (company == null) {
            return S2CActionResult.error(
                    ComponentFormatting.errorTranslatable(CompanyBlockSupport.MESSAGE_COMPANY_GONE));
        }
        if (!company.isOwner(player.getUUID())) {
            return S2CActionResult.error(
                    Component.literal("This action can only be taken by the owner of this company."));
        }
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        if (!company.isInsolvent(params)) {
            return S2CActionResult.error(Component.literal("Company is not insolvent."));
        }

        EconomySavedData economyData = EconomySavedData.get(server);
        String name = company.name();
        MarketParticipationRules.withdrawFromAll(company, economyData.getEconomy());
        companyData.getRegistry().delete(company.id());
        economyData.setDirty();
        companyData.setDirty();

        setCompanyId(null);
        player.closeContainer();
        return S2CActionResult.success(Component.translatable(
                "message.createkontor.bankruptcy.declared", ComponentFormatting.highlightStandard(name)));
    }

    public Optional<S2CActionResult> inviteManager(ServerPlayer inviter, String playerName) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return S2CActionResult.illegalEnvironment();
        }

        MinecraftServer server = serverLevel.getServer();
        CompanyRegistry registry = CompanySavedData.get(server).getRegistry();
        Company company = registry.get(getCompanyId()).orElse(null);
        if (company == null) {
            return S2CActionResult.error(
                    ComponentFormatting.errorTranslatable(CompanyBlockSupport.MESSAGE_COMPANY_GONE));
        }
        if (!company.isOwner(inviter.getUUID())) {
            return S2CActionResult.error(
                    Component.literal("This action can only be taken by the owner of this company."));
        }

        ServerPlayer target = server.getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            return S2CActionResult.error(Component.translatable(
                    "message.createkontor.manager_invite.offline", ComponentFormatting.highlightError(playerName)));
        }
        if (registry.companyOf(target.getUUID()).isPresent()) {
            return S2CActionResult.error(Component.translatable(
                    "message.createkontor.manager_invite.in_company", ComponentFormatting.highlightError(playerName)));
        }

        long issuedAt = System.currentTimeMillis();
        String command = "/kontorinvite accept " + company.id().value() + " " + inviter.getUUID() + " " + issuedAt + " "
                + inviteToken(company.id().value(), inviter.getUUID(), target.getUUID(), issuedAt);
        Component accept = Component.translatable("message.createkontor.manager_invite.accept")
                .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("message.createkontor.manager_invite.accept_hover"))));

        target.sendSystemMessage(Component.translatable(
                        "message.createkontor.manager_invite.text",
                        ComponentFormatting.highlightStandard(
                                inviter.getGameProfile().getName()),
                        ComponentFormatting.highlightStandard(company.name()))
                .append(" ")
                .append(accept));

        return S2CActionResult.success(Component.translatable(
                "message.createkontor.manager_invite.sent", ComponentFormatting.highlightStandard(playerName)));
    }

    public static Optional<S2CActionResult> acceptManagerInvitation(
            ServerPlayer invitee, int companyId, UUID inviterId, long issuedAt, String token) {
        byte[] expected =
                inviteToken(companyId, inviterId, invitee.getUUID(), issuedAt).getBytes(StandardCharsets.UTF_8);
        boolean validToken = MessageDigest.isEqual(expected, token.getBytes(StandardCharsets.UTF_8));
        if (!validToken || System.currentTimeMillis() - issuedAt > INVITE_VALID_MILLIS) {
            return S2CActionResult.error(
                    ComponentFormatting.errorTranslatable("message.createkontor.manager_invite.invalid"));
        }

        MinecraftServer server = invitee.server;
        CompanySavedData data = CompanySavedData.get(server);
        CompanyRegistry registry = data.getRegistry();

        Company company = registry.get(new CompanyId(companyId)).orElse(null);
        if (company == null || !company.isOwner(inviterId)) {
            return S2CActionResult.error(ComponentFormatting.error("Invalid invite."));
        }
        if (registry.companyOf(invitee.getUUID()).isPresent()) {
            return S2CActionResult.error(
                    ComponentFormatting.errorTranslatable("message.createkontor.manager_invite.already_in_company"));
        }

        company.addManager(invitee.getUUID());
        data.setDirty();
        PlayerNotifications.sendMessageToAllMembers(
                company,
                server,
                Component.translatable(
                        "message.createkontor.manager_invite.joined",
                        ComponentFormatting.highlightStandard(
                                invitee.getGameProfile().getName()),
                        ComponentFormatting.highlightStandard(company.name())));
        return S2CActionResult.empty();
    }

    public Optional<S2CActionResult> fireManager(ServerPlayer owner, UUID managerId) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return S2CActionResult.illegalEnvironment();
        }
        MinecraftServer server = serverLevel.getServer();
        CompanySavedData data = CompanySavedData.get(server);
        Company company = data.getRegistry().get(getCompanyId()).orElse(null);
        if (company == null) {
            return S2CActionResult.error(
                    ComponentFormatting.errorTranslatable(CompanyBlockSupport.MESSAGE_COMPANY_GONE));
        }
        if (!company.isOwner(owner.getUUID())) {
            return S2CActionResult.error(
                    Component.literal("This action can only be taken by the owner of this company."));
        }
        if (!company.removeManager(managerId)) {
            return S2CActionResult.error(ComponentFormatting.error("This player is not a manager."));
        }
        data.setDirty();

        ServerPlayer fired = server.getPlayerList().getPlayer(managerId);
        if (fired != null) {
            fired.sendSystemMessage(Component.translatable(
                    "message.createkontor.manager_fire.fired", ComponentFormatting.highlightError(company.name())));
        }
        return S2CActionResult.success(Component.translatable("message.createkontor.manager_fire.done"));
    }

    public Optional<S2CActionResult> leaveCompany(ServerPlayer player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return S2CActionResult.illegalEnvironment();
        }

        MinecraftServer server = serverLevel.getServer();
        CompanySavedData data = CompanySavedData.get(server);
        Company company = data.getRegistry().get(getCompanyId()).orElse(null);
        if (company == null) {
            return S2CActionResult.error(
                    ComponentFormatting.errorTranslatable(CompanyBlockSupport.MESSAGE_COMPANY_GONE));
        }
        if (company.isOwner(player.getUUID())) {
            return S2CActionResult.error(ComponentFormatting.error("The owner cannot leave the company."));
        }
        if (!company.removeManager(player.getUUID())) {
            return S2CActionResult.error(ComponentFormatting.error("You are not a manager."));
        }
        data.setDirty();

        PlayerNotifications.sendMessageToAllMembers(
                company,
                server,
                Component.translatable(
                        "message.createkontor.leave.left",
                        ComponentFormatting.highlightStandard(
                                player.getGameProfile().getName())));
        player.closeContainer();
        return S2CActionResult.success(Component.translatable(
                "message.createkontor.leave.done", ComponentFormatting.highlightStandard(company.name())));
    }

    private static String inviteToken(int companyId, UUID inviterId, UUID inviteeId, long issuedAt) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(INVITE_KEY, "HmacSHA256"));
            byte[] hash = mac.doFinal(
                    (companyId + "|" + inviterId + "|" + inviteeId + "|" + issuedAt).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
