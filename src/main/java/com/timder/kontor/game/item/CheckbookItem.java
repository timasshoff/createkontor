package com.timder.kontor.game.item;

import com.lowdragmc.lowdraglib2.gui.factory.HeldItemUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.block.company.CompanyBlockSupport;
import com.timder.kontor.game.ui.CheckbookUi;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CheckbookItem extends Item implements HeldItemUIMenuType.HeldItemUI {

    public CheckbookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        boolean isMember = CompanySavedData.get(serverPlayer.getServer())
                .getRegistry()
                .companyOf(serverPlayer.getUUID())
                .isPresent();
        if (!isMember) {
            CompanyBlockSupport.message(serverPlayer, "message.createkontor.checkbook.not_member");
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        HeldItemUIMenuType.openUI(serverPlayer, hand);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public ModularUI createUI(HeldItemUIMenuType.HeldItemUIHolder holder) {
        return CheckbookUi.create(holder.player);
    }
}
