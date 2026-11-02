package com.timder.kontor.game.block.employee;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.simibubi.create.foundation.block.IBE;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.core.company.employee.RoleSpec;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.game.block.company.AbstractCompanyBlock;
import com.timder.kontor.game.block.company.CompanyBlockSupport;
import com.timder.kontor.game.chunk.ChunkLoadingDesk;
import com.timder.kontor.registry.KontorBlockEntities;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractEmployeeDeskBlock extends AbstractCompanyBlock
        implements IBE<EmployeeDeskBlockEntity>, ChunkLoadingDesk, BlockUIMenuType.BlockUI {

    private final RoleId role;
    private final Supplier<Integer> baseSalaryInDollars;

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final double ITEMS_X1 = 0, ITEMS_Z1 = 0, ITEMS_X2 = 16, ITEMS_Z2 = 16;
    private static final double ITEMS_Y1 = 16, ITEMS_Y2 = 19;

    private static final VoxelShape BASE = Block.box(0, 0, 0, 16, 16, 16);
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    protected AbstractEmployeeDeskBlock(Properties properties, RoleId role, Supplier<Integer> baseSalaryInDollars) {
        super(properties);
        this.role = Objects.requireNonNull(role, "role must not be null.");
        this.baseSalaryInDollars = Objects.requireNonNull(baseSalaryInDollars, "baseSalaryInDollars must not be null.");
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            SHAPES.put(facing, Shapes.or(BASE, itemsShape(facing)));
        }
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public RoleId role() {
        return role;
    }

    public RoleSpec roleSpec() {
        return new RoleSpec(role, Money.ofDollars(baseSalaryInDollars.get()));
    }

    public String roleTranslationKey() {
        return "employee_role.createkontor." + role.value();
    }

    protected abstract ModularUI createEmployeeUi(EmployeeDeskContext context);

    @Override
    public final ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        return getBlockEntityOptional(holder.player.level(), holder.pos)
                .map(desk -> createEmployeeUi(EmployeeDeskContext.of(holder, desk)))
                .orElseGet(() -> new ModularUI(UI.empty(), holder.player));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
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
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASE;
    }

    private static VoxelShape itemsShape(Direction facing) {
        double x1 = ITEMS_X1, z1 = ITEMS_Z1, x2 = ITEMS_X2, z2 = ITEMS_Z2;
        int steps = switch (facing) {
            case EAST -> 0;
            case SOUTH -> 1;
            case WEST -> 2;
            default -> 3;
        };
        for (int i = 0; i < steps; i++) {
            double nx1 = 16 - z2, nx2 = 16 - z1, nz1 = x1, nz2 = x2;
            x1 = nx1;
            x2 = nx2;
            z1 = nz1;
            z2 = nz2;
        }
        return Block.box(x1, ITEMS_Y1, z1, x2, ITEMS_Y2, z2);
    }

    public final boolean tryOpenUi(ServerPlayer player, ServerLevel level, BlockPos pos) {
        return getBlockEntityOptional(level, pos)
                .filter(desk -> mayOpenUi(player, level, desk))
                .map(desk -> BlockUIMenuType.openUI(player, pos))
                .orElse(false);
    }

    protected boolean mayOpenUi(ServerPlayer player, ServerLevel level, EmployeeDeskBlockEntity desk) {
        if (CompanyBlockSupport.requireMember(level, desk.getBlockPos(), player) == null) {
            return false;
        }
        if (!desk.hasEmployee()) {
            CompanyBlockSupport.message(player, "message.createkontor.employee_desk.vacant");
            return false;
        }
        return true;
    }

    @Override
    public boolean stillValid(BlockUIMenuType.BlockUIHolder holder) {
        return BlockUIMenuType.BlockUI.super.stillValid(holder)
                && getBlockEntityOptional(holder.player.level(), holder.pos)
                        .map(EmployeeDeskBlockEntity::hasEmployee)
                        .orElse(false);
    }

    @Override
    public Class<EmployeeDeskBlockEntity> getBlockEntityClass() {
        return EmployeeDeskBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EmployeeDeskBlockEntity> getBlockEntityType() {
        return KontorBlockEntities.EMPLOYEE_DESK.get();
    }
}
