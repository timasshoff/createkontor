package com.timder.kontor.game.block.employee;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.simibubi.create.foundation.block.IBE;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.core.company.employee.RoleSpec;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.game.block.company.AbstractCompanyBlock;
import com.timder.kontor.game.block.company.CompanyBlockSupport;
import com.timder.kontor.game.chunk.ChunkLoadingDesk;
import com.timder.kontor.registry.KontorBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractEmployeeDeskBlock extends AbstractCompanyBlock implements IBE<EmployeeDeskBlockEntity>, ChunkLoadingDesk, BlockUIMenuType.BlockUI {

    private final RoleId role;
    private final Supplier<Integer> baseSalaryInDollars;

    protected AbstractEmployeeDeskBlock(Properties properties, RoleId role, Supplier<Integer> baseSalaryInDollars) {
        super(properties);
        this.role = Objects.requireNonNull(role, "role must not be null.");
        this.baseSalaryInDollars = Objects.requireNonNull(baseSalaryInDollars, "baseSalaryInDollars must not be null.");
    }

    public RoleId role() {
        return role;
    }

    public RoleSpec roleSpec() {
        return new RoleSpec(role, Money.ofDollars(baseSalaryInDollars.get()));
    }

    public String roleTranslationKey() {
        return "employee_role.createkontor." + role.value();
    }

    protected abstract ModularUI createEmployeeUi(EmployeeDeskContext context);

    @Override
    public final ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        return getBlockEntityOptional(holder.player.level(), holder.pos)
                .map(desk -> createEmployeeUi(EmployeeDeskContext.of(holder, desk)))
                .orElseGet(() -> new ModularUI(UI.empty(), holder.player));
    }

    public final boolean tryOpenUi(ServerPlayer player, ServerLevel level, BlockPos pos) {
        return getBlockEntityOptional(level, pos)
                .filter(desk -> mayOpenUi(player, level, desk))
                .map(desk -> BlockUIMenuType.openUI(player, pos))
                .orElse(false);
    }

    protected boolean mayOpenUi(ServerPlayer player, ServerLevel level, EmployeeDeskBlockEntity desk) {
        if (CompanyBlockSupport.requireMember(level, desk.getBlockPos(), player) == null) {
            return false;
        }
        if (!desk.hasEmployee()) {
            CompanyBlockSupport.message(player, "message.createkontor.employee_desk.vacant");
            return false;
        }
        return true;
    }

    @Override
    public boolean stillValid(BlockUIMenuType.BlockUIHolder holder) {
        return BlockUIMenuType.BlockUI.super.stillValid(holder)
                && getBlockEntityOptional(holder.player.level(), holder.pos)
                .map(EmployeeDeskBlockEntity::hasEmployee)
                .orElse(false);
    }

    @Override
    public Class<EmployeeDeskBlockEntity> getBlockEntityClass() {
        return EmployeeDeskBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EmployeeDeskBlockEntity> getBlockEntityType() {
        return KontorBlockEntities.EMPLOYEE_DESK.get();
    }
}
