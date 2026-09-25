package com.timder.kontor.game.block;

import com.timder.kontor.game.block.company.AbstractCompanyBlock;
import com.timder.kontor.registry.KontorBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class ShippingExitBlock extends AbstractCompanyBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ShippingExitBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ShippingExitBlockEntity(blockPos, blockState);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.isClientSide()) {
            return;
        }

        boolean wasPowered = state.getValue(POWERED);
        boolean isPowered = level.hasNeighborSignal(pos);
        if (isPowered == wasPowered) {
            return;
        }
        level.setBlock(pos, state.setValue(POWERED, isPowered), Block.UPDATE_CLIENTS);

        if (isPowered && level.getBlockEntity(pos) instanceof ShippingExitBlockEntity blockEntity) {
            blockEntity.onRedstonePulse();
        }
    }
}
