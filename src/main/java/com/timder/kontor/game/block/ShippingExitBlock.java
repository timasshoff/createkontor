package com.timder.kontor.game.block;

import com.timder.kontor.game.block.company.AbstractCompanyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ShippingExitBlock extends AbstractCompanyBlock {

    public ShippingExitBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ShippingExitBlockEntity(blockPos, blockState);
    }
}
