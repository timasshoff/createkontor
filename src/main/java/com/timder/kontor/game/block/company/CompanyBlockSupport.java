package com.timder.kontor.game.block.company;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.util.ComponentFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public final class CompanyBlockSupport {

    public static final String TAG_COMPANY_ID = "CompanyId";

    public static final String MESSAGE_BOUND = "message.createkontor.company_block.bound";
    public static final String MESSAGE_NOT_IN_COMPANY = "message.createkontor.company_block.not_in_company";
    public static final String MESSAGE_UNBOUND = "message.createkontor.company_block.unbound";
    public static final String MESSAGE_COMPANY_GONE = "message.createkontor.company_block.company_gone";
    public static final String MESSAGE_NOT_MEMBER = "message.createkontor.company_block.not_member";
    public static final String MESSAGE_STATUS = "message.createkontor.company_block.status";

    public static void writeCompanyId(@Nullable CompanyId companyId, CompoundTag tag) {
        tag.putInt(TAG_COMPANY_ID, companyId == null ? 0 : companyId.value());
    }

    @Nullable
    public static CompanyId readCompanyId(CompoundTag tag) {
        int raw = tag.getInt(TAG_COMPANY_ID);
        return raw > 0 ? new CompanyId(raw) : null;
    }

    public static void afterCompanyIdChanged(BlockEntity blockEntity) {
        blockEntity.setChanged();
        if (blockEntity.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().blockChanged(blockEntity.getBlockPos());
        }
    }

    public static void bindOnPlacement(Level level, BlockPos pos, @Nullable LivingEntity placer) {
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof CompanyBound bound)) {
            return;
        }

        ServerPlayer player = placer instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        MinecraftServer server = level.getServer();

        Company company = null;
        if (player != null && server != null) {
            company = registry(server).companyOf(player.getUUID()).orElse(null);
        }

        bound.setCompanyId(company == null ? null : company.id());

        if (player != null) {
            if (company != null) {
                message(player, MESSAGE_BOUND, ComponentFormatting.highlightStandard(company.name()));
            } else {
                message(player, MESSAGE_NOT_IN_COMPANY);
            }
        }
    }

    /**
     * Checks that the block belongs to a company the player is a member of
     * @param level The level
     * @param pos The position of the block
     * @param player The player who clicked
     * @return The company of the block, or null if the player may not use it
     */
    @Nullable
    public static Company requireMember(ServerLevel level, BlockPos pos, ServerPlayer player) {
        if (!(level.getBlockEntity(pos) instanceof CompanyBound bound)) {
            return null;
        }
        CompanyId id = bound.getCompanyId();
        if (id == null) {
            message(player, MESSAGE_UNBOUND);
            return null;
        }
        Company company = registry(level.getServer()).get(id).orElse(null);
        if (company == null) {
            message(player, MESSAGE_COMPANY_GONE);
            return null;
        }
        if (!company.isMember(player.getUUID())) {
            message(player, MESSAGE_NOT_MEMBER);
            return null;
        }
        return company;
    }

    public static void message(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args), true);
    }

    private static CompanyRegistry registry(MinecraftServer server) {
        return CompanySavedData.get(server).getRegistry();
    }
}
