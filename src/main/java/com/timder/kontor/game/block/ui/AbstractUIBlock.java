package com.timder.kontor.game.block.ui;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class AbstractUIBlock<BE extends BlockEntity> extends Block implements IBE<BE>, BlockUIMenuType.BlockUI {
    protected AbstractUIBlock(Properties properties) {
        super(properties);
    }

    protected abstract ModularUI createUI(BlockUIMenuType.BlockUIHolder holder, BE blockEntity);

    /**
     * Called when no matching block entity was found.
     */
    protected ModularUI createFallbackUI(BlockUIMenuType.BlockUIHolder holder) {
        return new ModularUI(UI.empty(), holder.player);
    }

    @Override
    public final ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        return getBlockEntityOptional(holder.player.level(), holder.pos)
                .<ModularUI>map(be -> createUI(holder, be))
                .orElseGet(() -> createFallbackUI(holder));
    }

    protected boolean canOpenUI(ServerPlayer player, ServerLevel level, BlockPos pos) {
        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            if (!canOpenUI(serverPlayer, serverLevel, pos)) {
                return InteractionResult.CONSUME;
            }
            BlockUIMenuType.openUI(serverPlayer, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
