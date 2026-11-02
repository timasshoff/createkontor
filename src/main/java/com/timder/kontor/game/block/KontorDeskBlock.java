package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.block.company.CompanyBindGate;
import com.timder.kontor.game.block.company.CompanyBlockSupport;
import com.timder.kontor.game.block.ui.AbstractUIBlock;
import com.timder.kontor.game.chunk.ChunkLoadingDesk;
import com.timder.kontor.registry.KontorBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class KontorDeskBlock extends AbstractUIBlock<KontorDeskBlockEntity> implements ChunkLoadingDesk {

    public static final String RESOURCE_KEY = "kontor_desk";
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final double ITEMS_X1 = 0, ITEMS_Z1 = 0, ITEMS_X2 = 16, ITEMS_Z2 = 16;
    private static final double ITEMS_Y1 = 16, ITEMS_Y2 = 19;

    private static final VoxelShape BASE = Block.box(0, 0, 0, 16, 16, 16);
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    public KontorDeskBlock(Properties properties) {
        super(properties);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            SHAPES.put(facing, Shapes.or(BASE, itemsShape(facing)));
        }
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        CompanyBlockSupport.bindOnPlacement(level, pos, placer, bindGate());
    }

    private CompanyBindGate bindGate() {
        return company -> company.boundResourceCount(RESOURCE_KEY) > 0
                ? Component.translatable("message.createkontor.kontor_desk.limit")
                : null;
    }

    @Override
    protected ModularUI createUI(BlockUIMenuType.BlockUIHolder holder, KontorDeskBlockEntity blockEntity) {
        return blockEntity.createUI(holder);
    }

    @Override
    protected boolean canOpenUI(ServerPlayer player, ServerLevel level, BlockPos pos) {
        return getBlockEntityOptional(level, pos)
                .map(blockEntity -> mayAccess(blockEntity, player, level))
                .orElse(true);
    }

    private boolean mayAccess(KontorDeskBlockEntity blockEntity, ServerPlayer player, ServerLevel level) {
        CompanyId companyId = blockEntity.getCompanyId();
        CompanyRegistry registry = CompanySavedData.get(level.getServer()).getRegistry();
        if (companyId == null) {
            if (registry.companyOf(player.getUUID()).isPresent()) {
                CompanyBlockSupport.message(player, "message.createkontor.already_member");
                return false; // Unbound, but player has a company
            }
            return true; // Unbound and player has no company: Everyone can found a new company using this block
        }
        Company company = registry.get(companyId).orElse(null);
        if (company == null) {
            return true; // Company does not exist anymore
        }
        if (company.isMember(player.getUUID())) {
            return true; // Company exists and player is member
        }
        CompanyBlockSupport.message(player, CompanyBlockSupport.MESSAGE_NOT_MEMBER);
        return false; // Company exists and player is not a member
    }

    @Override
    public Class<KontorDeskBlockEntity> getBlockEntityClass() {
        return KontorDeskBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KontorDeskBlockEntity> getBlockEntityType() {
        return KontorBlockEntities.KONTOR_DESK.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASE;
    }

    private static VoxelShape itemsShape(Direction facing) { // Rotating everything because I am an idiot and made the model facing east
        double x1 = ITEMS_X1, z1 = ITEMS_Z1, x2 = ITEMS_X2, z2 = ITEMS_Z2;
        int steps = switch (facing) { case EAST -> 0; case SOUTH -> 1; case WEST -> 2; default -> 3; };
        for (int i = 0; i < steps; i++) {
            double nx1 = 16 - z2, nx2 = 16 - z1, nz1 = x1, nz2 = x2;
            x1 = nx1; x2 = nx2; z1 = nz1; z2 = nz2;
        }
        return Block.box(x1, ITEMS_Y1, z1, x2, ITEMS_Y2, z2);
    }
}
