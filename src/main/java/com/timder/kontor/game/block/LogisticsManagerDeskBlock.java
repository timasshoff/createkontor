package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.game.LogisticsNetworkAccess;
import com.timder.kontor.game.block.company.CompanyBindGate;
import com.timder.kontor.game.block.employee.AbstractEmployeeDeskBlock;
import com.timder.kontor.game.block.employee.EmployeeDeskBlockEntity;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.LogisticsManagerUi;
import com.timder.kontor.registry.KontorBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class LogisticsManagerDeskBlock extends AbstractEmployeeDeskBlock {

    public static final RoleId ROLE = new RoleId("logistics_manager");
    public static final String RESOURCE_KEY = "logistics_manager_desk";

    public LogisticsManagerDeskBlock(Properties properties) {
        super(properties, ROLE, CompanyConfig.LOGISTICS_MANAGER_SALARY_IN_DOLLARS);
    }

    @Override
    protected CompanyBindGate bindGate() {
        return company -> company.boundResourceCount(RESOURCE_KEY) > 0
                ? Component.translatable("message.createkontor.logistics_manager_desk.limit")
                : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (LogisticsNetworkAccess.isStockLink(stack)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public BlockEntityType<? extends EmployeeDeskBlockEntity> getBlockEntityType() {
        return KontorBlockEntities.LOGISTICS_MANAGER_DESK.get();
    }

    @Override
    protected ModularUI createEmployeeUi(EmployeeDeskContext context) {
        return LogisticsManagerUi.create(context);
    }
}
