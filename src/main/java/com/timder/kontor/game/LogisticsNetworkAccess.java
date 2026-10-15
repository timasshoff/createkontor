package com.timder.kontor.game;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.*;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.timder.kontor.core.value.ItemId;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class LogisticsNetworkAccess {

    private static final LogisticallyLinkedBehaviour.RequestType REQUEST_TYPE =
            LogisticallyLinkedBehaviour.RequestType.REDSTONE;

    public static List<UUID> networksOfLinksAttachedTo(ServerLevel level, BlockPos attachedTo) {
        Objects.requireNonNull(level, "level must not be null.");
        Objects.requireNonNull(attachedTo, "attachedTo must not be null.");

        List<UUID> networks = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos linkPos = attachedTo.relative(direction);
            if (!level.isLoaded(linkPos)) {
                continue;
            }
            BlockState state = level.getBlockState(linkPos);
            if (!(state.getBlock() instanceof PackagerLinkBlock)) {
                continue;
            }

            if (PackagerLinkBlock.getConnectedDirection(state) != direction) {
                continue;
            }
            if (level.getBlockEntity(linkPos) instanceof PackagerLinkBlockEntity link && link.behaviour != null) {
                networks.add(link.behaviour.freqId);
            }
        }
        return networks;
    }

    @Nullable
    public static UUID ownerOf(UUID networkId) {
        Objects.requireNonNull(networkId, "networkId must not be null.");
        LogisticsNetwork network = Create.LOGISTICS.logisticsNetworks.get(networkId);
        return network == null ? null : network.owner;
    }

    public static int unloadedLinkCount(UUID networkId) {
        Objects.requireNonNull(networkId, "networkId must not be null.");
        return Create.LOGISTICS.getUnloadedLinkCount(networkId);
    }

    public static long totalItemCount(UUID networkId) {
        Objects.requireNonNull(networkId, "networkId must not be null.");
        return Math.max(
                0, LogisticsManager.getSummaryOfNetwork(networkId, false).getTotalCount());
    }

    public static Map<ItemId, Integer> stockOf(UUID networkId, Collection<ItemId> products) {
        Objects.requireNonNull(networkId, "networkId must not be null.");
        Objects.requireNonNull(products, "products must not be null.");

        InventorySummary summary = LogisticsManager.getSummaryOfNetwork(networkId, true);
        Map<ItemId, Integer> stock = new HashMap<>();
        for (ItemId product : products) {
            Item item = itemOf(product);
            if (item == null) {
                continue;
            }
            int count = summary.getCountOf(new ItemStack(item));
            if (count > 0) {
                stock.put(product, count);
            }
        }
        return stock;
    }

    public static boolean requestPackages(UUID networkId, String address, Map<ItemId, Integer> amounts) {
        Objects.requireNonNull(networkId, "networkId must not be null.");
        Objects.requireNonNull(address, "address must not be null.");
        Objects.requireNonNull(amounts, "amounts must not be null.");

        List<BigItemStack> stacks = new ArrayList<>();
        for (Map.Entry<ItemId, Integer> entry : amounts.entrySet()) {
            Item item = itemOf(entry.getKey());
            if (item == null || entry.getValue() < 1) {
                continue;
            }
            stacks.add(new BigItemStack(new ItemStack(item), entry.getValue()));
        }
        if (stacks.isEmpty()) {
            return false;
        }
        return LogisticsManager.broadcastPackageRequest(
                networkId, REQUEST_TYPE, PackageOrderWithCrafts.simple(stacks), null, address);
    }

    public static boolean isStockLink(ItemStack stack) {
        Objects.requireNonNull(stack, "stack must not be null.");
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof PackagerLinkBlock;
    }

    public static List<Map.Entry<ItemId, Long>> stockByItem(UUID networkId) {
        Objects.requireNonNull(networkId, "networkId must not be null.");
        Map<ItemId, Long> totals = new HashMap<>();
        for (BigItemStack big :
                LogisticsManager.getSummaryOfNetwork(networkId, false).getStacks()) {
            if (big.count < 1 || big.stack.isEmpty()) continue;
            ItemId id = new ItemId(
                    BuiltInRegistries.ITEM.getKey(big.stack.getItem()).toString());
            totals.merge(id, (long) big.count, Long::sum);
        }
        List<Map.Entry<ItemId, Long>> sorted = new ArrayList<>(totals.entrySet());
        sorted.sort(Map.Entry.<ItemId, Long>comparingByValue()
                .reversed()
                .thenComparing(e -> e.getKey().value()));
        return sorted;
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
}
