package com.timder.kontor.game.item;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.block.company.CompanyBlockSupport;
import com.timder.kontor.game.network.NotebookPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class NotebookItem extends Item {

    public NotebookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        MinecraftServer server = serverPlayer.getServer();
        Company company = CompanySavedData.get(server)
                .getRegistry()
                .companyOf(serverPlayer.getUUID())
                .orElse(null);

        if (company == null) {
            CompanyBlockSupport.message(serverPlayer, "message.createkontor.notebook.not_member");
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        PacketDistributor.sendToPlayer(
                serverPlayer,
                new NotebookPayload(
                        company.name(),
                        EconomySavedData.get(server).getEconomy().currentDay(),
                        company.account().getBalance().cents(),
                        CompanySavedData.writeOrderBook(company.orderBook().getSaveState())));
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
