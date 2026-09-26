package com.timder.kontor.game.block;

import com.simibubi.create.foundation.block.IBE;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.LegalFormDef;
import com.timder.kontor.core.company.LegalForms;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.block.company.AbstractCompanyBlock;
import com.timder.kontor.game.block.company.CompanyBindGate;
import com.timder.kontor.registry.KontorBlockEntities;
import com.timder.kontor.util.ComponentFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class ShippingExitBlock extends AbstractCompanyBlock implements IBE<ShippingExitBlockEntity> {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final String RESOURCE_KEY = "shipping_exit";

    public ShippingExitBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected CompanyBindGate bindGate() {
        return company -> {
            CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
            LegalFormDef legalForm = company.legalForm(params);
            if (legalForm.allowsMoreShippingExits(company.boundResourceCount(RESOURCE_KEY))) {
                return null;
            }
            return Component.translatable("message.createkontor.shipping_exit.limit", ComponentFormatting.highlightError(String.valueOf(legalForm.maxShippingExits())));
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public Class<ShippingExitBlockEntity> getBlockEntityClass() {
        return ShippingExitBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ShippingExitBlockEntity> getBlockEntityType() {
        return KontorBlockEntities.SHIPPING_EXIT.get();
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return getBlockEntityOptional(level, pos).map(pbe -> pbe.isSignallingDelivery() ? 15 : 0)
                .orElse(0);
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
