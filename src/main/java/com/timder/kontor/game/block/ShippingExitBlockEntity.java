package com.timder.kontor.game.block;

import com.simibubi.create.foundation.item.ItemHelper;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.order.DeliveryRules;
import com.timder.kontor.core.company.order.Order;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.block.company.AbstractCompanyBlockEntity;
import com.timder.kontor.registry.KontorBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ShippingExitBlockEntity extends AbstractCompanyBlockEntity {

    public ShippingExitBlockEntity(BlockPos pos, BlockState blockState) {
        super(KontorBlockEntities.SHIPPING_EXIT.get(), pos, blockState);
    }

    void onRedstonePulse() {
        if (level instanceof ServerLevel serverLevel) {
            checkDeliveries(serverLevel, getBlockPos());
        }
    }

    private void checkDeliveries(ServerLevel level, BlockPos pos) {
        CompanyId companyId = getCompanyId();
        if (companyId == null) {
            return;
        }
        MinecraftServer server = level.getServer();
        CompanySavedData companyData = CompanySavedData.get(server);
        Company company = companyData.getRegistry().get(companyId).orElse(null);
        if (company == null) {
            return;
        }

        Set<ItemId> products = new LinkedHashSet<>();
        for (Order order : company.orderBook().allOrders()) {
            products.add(order.getProduct());
        }
        if (products.isEmpty()) {
            return;
        }

        List<IItemHandler> handlers = neighborHandlers(level, pos);
        if (handlers.isEmpty()) {
            return;
        }

        EconomySavedData economyData = EconomySavedData.get(server);
        Economy economy = economyData.getEconomy();
        long day = economy.currentDay();

        boolean anyDelivered = false;
        for (ItemId product : products) {
            anyDelivered |= fullfillAsManyAsPossible(company, economy, day, product, handlers);
        }

        if (anyDelivered) {
            level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.25f, 0.75f);
            companyData.setDirty();
            economyData.setDirty();
        }
    }

    private boolean fullfillAsManyAsPossible(Company company, Economy economy, long day, ItemId product, List<IItemHandler> handlers) {
        Item item = itemOf(product);
        if (item == null) {
            return false;
        }

        boolean deliveredAny = false;
        while (true) {
            List<Order> candidates = DeliveryRules.getOrdersByPriority(company, economy, product);
            if (candidates.isEmpty()) {
                return deliveredAny;
            }

            Order order = candidates.get(0);
            int needed = order.remainingQuantity();

            if (drainAcrossHandlers(handlers, item, needed, true) < needed) {
                return deliveredAny;
            }
            if (drainAcrossHandlers(handlers, item, needed, false) < needed) {
                // This branch will probably never run
                return deliveredAny;
            }

            DeliveryRules.deliverAndComplete(company, economy, day, order);
            deliveredAny = true;
        }
    }

    @Nullable
    private static Item itemOf(ItemId product) {
        ResourceLocation location = ResourceLocation.tryParse(product.value());
        if (location == null) {
            return null;
        }
        Item item = BuiltInRegistries.ITEM.get(location);
        return item == Items.AIR ? null : item;
    }

    private static List<IItemHandler> neighborHandlers(ServerLevel level, BlockPos pos) {
        List<IItemHandler> handlers = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (!level.isLoaded(neighborPos)) {
                continue;
            }
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, direction.getOpposite());
            if (handler != null) {
                handlers.add(handler);
            }
        }
        return handlers;
    }

    private static int drainAcrossHandlers(List<IItemHandler> handlers, Item item, int needed, boolean simulate) {
        Predicate<ItemStack> matches = stack -> stack.getItem() == item && stack.getComponentsPatch().isEmpty();
        int collected = 0;
        for (IItemHandler handler : handlers) {
            if (collected >= needed) {
                break;
            }
            ItemStack extracted = ItemHelper.extract(handler, matches, ItemHelper.ExtractionCountMode.UPTO, needed - collected, simulate);
            collected += extracted.getCount();
        }
        return collected;
    }
}