package com.timder.kontor.game.block;

import com.timder.kontor.game.block.company.AbstractCompanyBlockEntity;
import com.timder.kontor.registry.KontorBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ShippingExitBlockEntity extends AbstractCompanyBlockEntity {
    public ShippingExitBlockEntity(BlockPos pos, BlockState blockState) {
        super(KontorBlockEntities.SHIPPING_EXIT.get(), pos, blockState);
    }
}
