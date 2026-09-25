package com.timder.kontor.game.block.company;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.util.ComponentFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * A block entity that belongs to a company.
 * Stores, saves and synchs the company id.
 */
public abstract class AbstractCompanyBlockEntity extends SmartBlockEntity implements CompanyBound, IHaveGoggleInformation {

    @Nullable
    private CompanyId companyId;
    private String companyName = "";

    public AbstractCompanyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
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
        companyName = resolveCompanyName();
        CompanyBlockSupport.afterCompanyIdChanged(this);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level != null && !level.isClientSide()) {
            String resolved = resolveCompanyName();
            if (!resolved.equals(companyName)) {
                companyName = resolved;
                notifyUpdate();
            }
        }
    }

    private String resolveCompanyName() {
        if (companyId == null || !(level instanceof ServerLevel serverLevel)) {
            return "";
        }
        return CompanySavedData.get(serverLevel.getServer()).getRegistry().get(companyId)
                .map(Company::name)
                .orElse("");
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (companyId == null || companyName.isEmpty()) {
            tooltip.add(Component.translatable(CompanyBlockSupport.MESSAGE_UNBOUND));
            return true;
        }
        Component c = Component.translatable(CompanyBlockSupport.MESSAGE_STATUS, ComponentFormatting.highlightStandard(companyName));
        CreateLang.builder().add(c).forGoggles(tooltip, 1);
        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        CompanyBlockSupport.writeCompanyId(companyId, tag);
        CompanyBlockSupport.writeCompanyName(companyName, tag);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        companyId = CompanyBlockSupport.readCompanyId(tag);
        companyName = CompanyBlockSupport.readCompanyName(tag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        CompanyBlockSupport.writeCompanyId(companyId, tag);
        CompanyBlockSupport.writeCompanyName(companyName, tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
