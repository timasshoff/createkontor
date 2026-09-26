package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.timder.kontor.game.block.ui.AbstractUIBlock;
import com.timder.kontor.registry.KontorBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class KontorDeskBlock extends AbstractUIBlock<KontorDeskBlockEntity> {

    public KontorDeskBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ModularUI createUI(BlockUIMenuType.BlockUIHolder holder, KontorDeskBlockEntity blockEntity) {
        return blockEntity.createUI(holder);
    }

    @Override
    public Class<KontorDeskBlockEntity> getBlockEntityClass() {
        return KontorDeskBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KontorDeskBlockEntity> getBlockEntityType() {
        return KontorBlockEntities.KONTOR_DESK.get();
    }
}
