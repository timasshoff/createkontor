package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.config.EconomyConfig;
import com.timder.kontor.core.company.Company;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;

public class KontorDeskBlockEntity extends AbstractCompanyBlockEntity {

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

        return KontorDeskUnboundUi.create(holder,this);
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
            company = registry.found(name, economy.currentDay(), player.getUUID(), takeLoan, economy.policyRate(), params);
        } catch (IllegalArgumentException e) {
            return S2CActionResult.error(ComponentFormatting.error("Founding failed: " + e.getMessage()));
        }
        data.setDirty();
        setCompanyId(company.id());
        CompanyBlockSupport.message(player, CompanyBlockSupport.MESSAGE_BOUND, ComponentFormatting.highlightStandard(company.name()));
        player.closeContainer();
        return S2CActionResult.success(Component.translatable("ui.createkontor.kontor_desk.founded", ComponentFormatting.highlightStandard(company.name())));
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
            return S2CActionResult.error(ComponentFormatting.errorTranslatable(CompanyBlockSupport.MESSAGE_COMPANY_GONE));
        }

        try {
            company.acceptRequest(number, CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions())), EconomyConfig.toRequestParams());
        } catch (IllegalStateException e) {
            return S2CActionResult.error(Component.translatable(
                    "message.createkontor.kontor_desk.request_accepting_failed",
                    ComponentFormatting.highlightError("#" + number),
                    ComponentFormatting.error(e.getMessage())
            ));
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
            return S2CActionResult.error(ComponentFormatting.errorTranslatable(CompanyBlockSupport.MESSAGE_COMPANY_GONE));
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
        Company company = CompanySavedData.get(server).getRegistry().get(getCompanyId()).orElse(null);
        if (company == null) {
            return S2CActionResult.error(ComponentFormatting.errorTranslatable(CompanyBlockSupport.MESSAGE_COMPANY_GONE));
        }

        EconomySavedData economyData = EconomySavedData.get(server);
        Economy economy = economyData.getEconomy();
        if (!economy.isParticipant(market, company.id())) {
            return S2CActionResult.error(ComponentFormatting.error("The company does not actively take part in " + market + "."));
        }

        try {
            economy.updateListPrice(market, company.id(), Money.ofCents(priceCents).toDollars());
        } catch (IllegalArgumentException e) {
            return S2CActionResult.error(ComponentFormatting.error(e.getMessage()));
        }
        economyData.setDirty();
        return S2CActionResult.empty();
    }
}
