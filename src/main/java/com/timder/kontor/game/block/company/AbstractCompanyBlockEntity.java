package com.timder.kontor.game.block.company;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.CompanyRegistry;
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
        updateBoundResourceCount(companyId, id);
        companyId = id;
        companyName = resolveCompanyName();
        CompanyBlockSupport.afterCompanyIdChanged(this);
    }

    @Nullable
    protected String boundResourceKey() {
        return null;
    }

    private void updateBoundResourceCount(@Nullable CompanyId oldId, @Nullable CompanyId newId) {
        String resourceKey = boundResourceKey();
        if (resourceKey == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        CompanySavedData data = CompanySavedData.get(serverLevel.getServer());
        CompanyRegistry registry = data.getRegistry();
        boolean changedAny = false;
        if (oldId != null) {
            Company old = registry.get(oldId).orElse(null);
            if (old != null) {
                old.unbindResource(resourceKey);
                changedAny = true;
            }
        }
        if (newId != null) {
            Company next = registry.get(newId).orElse(null);
            if (next != null) {
                next.bindResource(resourceKey);
                changedAny = true;
            }
        }
        if (changedAny) {
            data.setDirty();
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (boundResourceKey() != null) {
            setCompanyId(null);
        }
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
            CreateLang.builder(CreateKontor.MODID)
                    .add(ComponentFormatting.errorTranslatable(CompanyBlockSupport.GOGGLE_COMPANY_BLOCK_UNBOUND))
                    .forGoggles(tooltip, 1);
            return true;
        }

        CreateLang.builder()
                .add(ComponentFormatting.standardTranslatable(CompanyBlockSupport.GOGGLE_COMPANY_BLOCK))
                .forGoggles(tooltip, 1);

        CreateLang.builder()
                .add(ComponentFormatting.highlightStandard(companyName))
                .forGoggles(tooltip, 2);

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
