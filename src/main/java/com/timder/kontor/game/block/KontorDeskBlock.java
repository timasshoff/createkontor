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
import com.timder.kontor.registry.KontorBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class KontorDeskBlock extends AbstractUIBlock<KontorDeskBlockEntity> {

    public static final String RESOURCE_KEY = "kontor_desk";

    public KontorDeskBlock(Properties properties) {
        super(properties);
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
}
