package com.timder.kontor.game.block.buyer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.logistics.packagePort.PackagePortTarget;
import com.simibubi.create.content.logistics.packagePort.PackagePortTargetType;
import com.timder.kontor.registry.KontorBlocks;
import com.timder.kontor.registry.KontorPackagePortTargets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class BuyerDeskPortTarget extends PackagePortTarget {

    public static final MapCodec<BuyerDeskPortTarget> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(BlockPos.CODEC.fieldOf("relative_pos")
                    .forGetter(target -> target.relativePos))
                    .apply(instance, BuyerDeskPortTarget::new));

    public static final StreamCodec<ByteBuf, BuyerDeskPortTarget> STREAM_CODEC = BlockPos.STREAM_CODEC.map(BuyerDeskPortTarget::new, target -> target.relativePos);

    public BuyerDeskPortTarget(BlockPos relativePos) {
        super(relativePos);
    }

    @Override
    public boolean export(LevelAccessor level, BlockPos portPos, ItemStack box, boolean simulate) {
        return false;
    }

    @Override
    public Vec3 getExactTargetLocation(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
        return Vec3.atCenterOf(portPos.offset(relativePos));
    }

    @Override
    public void register(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
        if (level.isClientSide()) {
            return;
        }
        if (be(level, portPos) instanceof BuyerDeskBlockEntity desk) {
            desk.attachPort(ppbe);
        }
    }

    @Override
    public void deregister(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
        if (level.isClientSide()) {
            return;
        }
        if (be(level, portPos) instanceof BuyerDeskBlockEntity desk) {
            desk.detachPort(ppbe);
        }
    }

    @Override
    public ItemStack getIcon() {
        return KontorBlocks.BUYER_DESK.asStack();
    }

    @Override
    public boolean canSupport(BlockEntity be) {
        return AllBlockEntityTypes.PACKAGE_POSTBOX.is(be);
    }

    @Override
    protected PackagePortTargetType getType() {
        return KontorPackagePortTargets.BUYER_DESK.value();
    }

    public static class Type implements PackagePortTargetType {
        @Override
        public MapCodec<BuyerDeskPortTarget> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<ByteBuf, BuyerDeskPortTarget> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
