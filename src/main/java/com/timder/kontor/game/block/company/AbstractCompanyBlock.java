package com.timder.kontor.game.block.company;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.util.ComponentFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCompanyBlock extends Block implements EntityBlock {

    public AbstractCompanyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        CompanyBlockSupport.bindOnPlacement(level, pos, placer, bindGate());
    }

    protected CompanyBindGate bindGate() {
        return CompanyBindGate.ALWAYS_ALLOWED;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            Company company = CompanyBlockSupport.requireMember(serverLevel, pos, serverPlayer);
            if (company != null) {
                onMemberUse(serverPlayer, serverLevel, pos, company);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    /**
     * A member of the company right-clicked this block.
     * By default, this shows the name of the company. Runs on the server.
     * @param player The member
     * @param level The level
     * @param pos The pos of the block
     * @param company The company of the block
     */
    protected void onMemberUse(ServerPlayer player, ServerLevel level, BlockPos pos, Company company) {
        CompanyBlockSupport.message(player, CompanyBlockSupport.MESSAGE_STATUS, ComponentFormatting.highlightStandard(company.name()));
    }
}
