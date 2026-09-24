package com.timder.kontor.game.block.company;

import com.timder.kontor.core.company.CompanyId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A block entity that belongs to a company.
 * Stores, saves and synchs the company id.
 */
public abstract class AbstractCompanyBlockEntity extends BlockEntity implements CompanyBound {

    @Nullable
    private CompanyId companyId;

    public AbstractCompanyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Nullable
    @Override
    public CompanyId getCompanyId() {
        return companyId;
    }

    @Override
    public void setCompanyId(@Nullable CompanyId id) {
        if (Objects.equals(companyId, id)) {
            return;
        }
        companyId = id;
        CompanyBlockSupport.afterCompanyIdChanged(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompanyBlockSupport.writeCompanyId(companyId, tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        companyId = CompanyBlockSupport.readCompanyId(tag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        CompanyBlockSupport.writeCompanyId(companyId, tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
