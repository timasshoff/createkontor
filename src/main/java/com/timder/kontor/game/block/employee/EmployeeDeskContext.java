package com.timder.kontor.game.block.employee;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.block.company.CompanyBlockSupport;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public record EmployeeDeskContext(
        BlockUIMenuType.BlockUIHolder holder,
        EmployeeDeskBlockEntity desk,
        @Nullable Company company,
        @Nullable Economy economy
) {

    public static EmployeeDeskContext of(BlockUIMenuType.BlockUIHolder holder, EmployeeDeskBlockEntity desk) {
        Company company = null;
        Economy economy = null;
        if (holder.player.level() instanceof ServerLevel level) {
            if (holder.player instanceof ServerPlayer player) {
                company = CompanyBlockSupport.requireMember(level, holder.pos, player);
            }
            economy = EconomySavedData.get(level.getServer()).getEconomy();
        }
        return new EmployeeDeskContext(holder, desk, company, economy);
    }

    public Player player() {
        return holder.player;
    }
}
