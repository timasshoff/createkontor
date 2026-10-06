package com.timder.kontor.client;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.logistics.packagePort.PackagePortTargetSelectionHandler;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.game.block.buyer.BuyerDeskBlock;
import com.timder.kontor.game.block.buyer.BuyerDeskPortTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = CreateKontor.MODID, value = Dist.CLIENT)
public final class BuyerDeskPostboxHandler {
    @SubscribeEvent
    public static void onInteract(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null || mc.level == null) {
            return;
        }
        if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() == HitResult.Type.MISS) {
            return;
        }
        if (!AllTags.AllItemTags.POSTBOXES.matches(mc.player.getMainHandItem())) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        if (!(mc.level.getBlockState(pos).getBlock() instanceof BuyerDeskBlock)) {
            return;
        }

        PackagePortTargetSelectionHandler.exactPositionOfTarget = Vec3.atCenterOf(pos);
        PackagePortTargetSelectionHandler.activePackageTarget = new BuyerDeskPortTarget(pos);
        PackagePortTargetSelectionHandler.isPostbox = true;
        event.setCanceled(true);
    }
}
