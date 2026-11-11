package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.simibubi.create.foundation.block.IBE;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.block.company.CompanyBlockSupport;
import com.timder.kontor.game.block.ui.AbstractUIBlock;
import com.timder.kontor.game.ui.PaymentTerminalUi;
import com.timder.kontor.registry.KontorBlockEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PaymentTerminalBlock extends AbstractUIBlock<PaymentTerminalBlockEntity>
        implements IBE<PaymentTerminalBlockEntity> {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final int SIGNAL_STRENGTH = 15;

    private static final VoxelShape SHAPE_NORTH_SOUTH =
            Shapes.or(Block.box(0, 0, 0, 16, 2, 16), Block.box(3, 2, 2, 13, 14, 14));
    private static final VoxelShape SHAPE_EAST_WEST =
            Shapes.or(Block.box(0, 0, 0, 16, 2, 16), Block.box(2, 2, 3, 14, 14, 13));

    public PaymentTerminalBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        CompanyBlockSupport.bindOnPlacement(level, pos, placer);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
    }

    @Override
    protected ModularUI createUI(BlockUIMenuType.BlockUIHolder holder, PaymentTerminalBlockEntity blockEntity) {
        return PaymentTerminalUi.create(holder, blockEntity);
    }

    @Override
    protected boolean canOpenUI(ServerPlayer player, ServerLevel level, BlockPos pos) {
        boolean bound = getBlockEntityOptional(level, pos)
                .map(PaymentTerminalBlockEntity::isBoundToCompany)
                .orElse(false);
        if (!bound) {
            CompanyBlockSupport.message(player, CompanyBlockSupport.MESSAGE_UNBOUND);
            return false;
        }

        boolean isMember = CompanySavedData.get(level.getServer())
                .getRegistry()
                .companyOf(player.getUUID())
                .isPresent();
        if (!isMember) {
            CompanyBlockSupport.message(player, "message.createkontor.payment_terminal.no_company");
        }
        return isMember;
    }

    @Override
    public Class<PaymentTerminalBlockEntity> getBlockEntityClass() {
        return PaymentTerminalBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PaymentTerminalBlockEntity> getBlockEntityType() {
        return KontorBlockEntities.PAYMENT_TERMINAL.get();
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(ACTIVE) ? SIGNAL_STRENGTH : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(ACTIVE) ? SIGNAL_STRENGTH : 0;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        boolean wasActive = state.getValue(ACTIVE);
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (wasActive && !state.is(newState.getBlock()) && !level.isClientSide()) {
            notifyNeighbours(level, pos, state.getBlock());
        }
    }

    static void notifyNeighbours(Level level, BlockPos pos, Block block) {
        level.updateNeighborsAt(pos, block);
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), block);
        }
    }
}
