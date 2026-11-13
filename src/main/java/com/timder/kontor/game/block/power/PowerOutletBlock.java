package com.timder.kontor.game.block.power;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.legalform.LegalFormDef;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.block.company.CompanyBindGate;
import com.timder.kontor.game.block.company.CompanyBlockSupport;
import com.timder.kontor.registry.KontorBlockEntities;
import com.timder.kontor.util.ComponentFormatting;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class PowerOutletBlock extends DirectionalKineticBlock implements IBE<PowerOutletBlockEntity> {

    public static final String RESOURCE_KEY = "power_outlet";
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public PowerOutletBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredFacing(context);
        BlockState state;
        if ((context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) || preferred == null) {
            state = super.getStateForPlacement(context);
        } else {
            state = defaultBlockState().setValue(FACING, preferred);
        }
        return state.setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        CompanyBlockSupport.bindOnPlacement(level, pos, placer, bindGate());
    }

    @Override
    public void neighborChanged(
            BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide()) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (powered == state.getValue(POWERED)) {
            return;
        }
        level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
        withBlockEntityDo(level, pos, PowerOutletBlockEntity::updateGeneratedRotation);
    }

    private static CompanyBindGate bindGate() {
        return company -> {
            CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
            LegalFormDef legalForm = company.legalForm(params);
            if (legalForm.allowsMorePowerOutlets(company.boundResourceCount(RESOURCE_KEY))) {
                return null;
            }
            return Component.translatable(
                    "message.createkontor.power_outlet.limit",
                    ComponentFormatting.highlightError(String.valueOf(legalForm.maxPowerOutlets())));
        };
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hideStressImpact() {
        return true;
    }

    @Override
    public Class<PowerOutletBlockEntity> getBlockEntityClass() {
        return PowerOutletBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PowerOutletBlockEntity> getBlockEntityType() {
        return KontorBlockEntities.POWER_OUTLET.get();
    }
}
