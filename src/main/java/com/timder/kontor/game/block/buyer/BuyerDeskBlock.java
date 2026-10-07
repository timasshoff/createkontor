package com.timder.kontor.game.block.buyer;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.game.block.employee.AbstractEmployeeDeskBlock;
import com.timder.kontor.game.block.employee.EmployeeDeskBlockEntity;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.BuyerUi;
import com.timder.kontor.registry.KontorBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;


public class BuyerDeskBlock extends AbstractEmployeeDeskBlock {

    public static final RoleId ROLE = new RoleId("buyer");

    public BuyerDeskBlock(Properties properties) {
        super(properties, ROLE, CompanyConfig.BUYER_SALARY_IN_DOLLARS);
    }

    @Override
    public BlockEntityType<? extends EmployeeDeskBlockEntity> getBlockEntityType() {
        return KontorBlockEntities.BUYER_DESK.get();
    }

    @Override
    protected ModularUI createEmployeeUi(EmployeeDeskContext context) {
        return BuyerUi.create(context);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BuyerDeskBlockEntity desk) {
            desk.dropPending();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
