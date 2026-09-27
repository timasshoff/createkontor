package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.config.EconomyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.core.company.LegalForms;
import com.timder.kontor.core.company.financial.Booking;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.block.company.AbstractCompanyBlockEntity;
import com.timder.kontor.game.block.company.CompanyBlockSupport;
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

    public Component foundNewCompany(ServerPlayer player, String name, boolean takeLoan) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Component.empty();
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
            return ComponentFormatting.error("Founding failed: " + e.getMessage());
        }
        data.setDirty();
        setCompanyId(company.id());
        CompanyBlockSupport.message(player, CompanyBlockSupport.MESSAGE_BOUND, ComponentFormatting.highlightStandard(company.name()));
        player.closeContainer();
        return Component.empty();
    }

    public Component acceptRequest(long number) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Component.empty();
        }
        MinecraftServer server = serverLevel.getServer();
        var data = CompanySavedData.get(server);
        CompanyRegistry registry = data.getRegistry();
        Company company = registry.get(getCompanyId()).orElse(null);
        if (company == null) {
            return Component.empty();
        }

        try {
            company.acceptRequest(number, CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions())), EconomyConfig.toRequestParams());
        } catch (IllegalStateException e) {
            return Component.empty();
        }
        return Component.empty();
    }
}
