package com.timder.kontor.game.block.buyer;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.purchase.PurchaseOffers;
import com.timder.kontor.core.company.purchase.PurchaseQuote;
import com.timder.kontor.core.company.purchase.PurchaseRules;
import com.timder.kontor.core.company.purchase.PurchaseStatus;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.block.employee.EmployeeDeskBlockEntity;
import com.timder.kontor.game.network.S2CActionResult;
import com.timder.kontor.util.ComponentFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.*;

public class BuyerDeskBlockEntity extends EmployeeDeskBlockEntity {

    private static final int MAX_STACK_IN_PACKAGE = 64;
    public static final int MAX_GOOD_SIZE = 9999;
    public static final int MAX_ADDRESS_LENGTH = 25;

    private static final String TAG_PORT_COUNT = "ConnectedPorts";
    private static final String TAG_PENDING = "PendingPackages";
    private static final String TAG_PENDING_COUNT = "PendingCount";
    private static final String TAG_ADDRESS = "Address";

    private String address = "";

    public record Goods(Item item, int quantity) {
        public Goods {
            if (quantity < 1) throw new IllegalArgumentException("quantity must be at least 1.");
        }
    }

    private final Set<BlockPos> ports = new LinkedHashSet<>();
    private final List<ItemStack> pendingPackages = new ArrayList<>();
    private int portCount = 0;
    private int pendingCount = 0;

    public BuyerDeskBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String value) {
        if (level == null || level.isClientSide()) {
            return;
        }
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.length() > MAX_ADDRESS_LENGTH) {
            cleaned = cleaned.substring(0, MAX_ADDRESS_LENGTH);
        }
        if (cleaned.equals(address)) {
            return;
        }
        address = cleaned;
        notifyUpdate();
    }

    public void attachPort(PackagePortBlockEntity port) {
        if (level == null || level.isClientSide()) {
            return;
        }
        ports.add(port.getBlockPos().immutable());
    }

    public void detachPort(PackagePortBlockEntity port) {
        ports.remove(port.getBlockPos());
    }

    public List<PackagePortBlockEntity> connectedPorts() {
        List<PackagePortBlockEntity> result = new ArrayList<>();
        if (level == null || level.isClientSide()) {
            return result;
        }
        for (Iterator<BlockPos> it = ports.iterator(); it.hasNext(); ) {
            PackagePortBlockEntity port = portAt(it.next());
            if (port == null) {
                it.remove();
            } else {
                result.add(port);
            }
        }
        return result;
    }

    @Nullable
    private PackagePortBlockEntity portAt(BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return null;
        }
        if (level.getBlockEntity(pos) instanceof PackagePortBlockEntity port
                && port.target instanceof BuyerDeskPortTarget target
                && getBlockPos().equals(pos.offset(target.relativePos))) {
            return port;
        }
        return null;
    }

    public Optional<S2CActionResult> buy(Map<ItemId, Integer> quote, long expectedTotalCents) {
        if (!(level instanceof ServerLevel serverLevel)) return S2CActionResult.illegalEnvironment();
        if (quote.isEmpty()) return S2CActionResult.error(Component.literal("Quote is empty"));
        if (connectedPorts().isEmpty()) return S2CActionResult.error(Component.translatable("ui.createkontor.buyer_desk.no_postbox"));

        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        EconomySavedData economyData = EconomySavedData.get(serverLevel.getServer());
        CompanySavedData companyData = CompanySavedData.get(serverLevel.getServer());

        Economy economy = economyData.getEconomy();
        Company company = companyData.getRegistry().get(getCompanyId()).orElse(null);
        if (company == null) return S2CActionResult.error(Component.literal("Company does not exist."));

        List<PurchaseQuote.Line> lines = new ArrayList<>();
        List<Goods> goods = new ArrayList<>();

        for (Map.Entry<ItemId, Integer> entry : quote.entrySet()) {
            int quantity = entry.getValue();
            ResourceLocation id = ResourceLocation.tryParse(entry.getKey().value());
            Optional<Item> item = id == null ? Optional.empty() : BuiltInRegistries.ITEM.getOptional(id);

            if (quantity < 1 || quantity > MAX_GOOD_SIZE || item.isEmpty()) return S2CActionResult.error(Component.literal("Quantity invalid."));

            try {
                lines.add(PurchaseOffers.offerFor(economy, params, entry.getKey()).lineFor(quantity));
            } catch (IllegalArgumentException e) {
                return S2CActionResult.error(Component.literal("Failed: " + e.getMessage()));
            }
            goods.add(new BuyerDeskBlockEntity.Goods(item.get(), quantity));
        }

        PurchaseQuote purchaseQuote = new PurchaseQuote(lines);
        if (purchaseQuote.total().cents() != expectedTotalCents) return S2CActionResult.error(Component.literal("Prices changed. Try again."));

        PurchaseStatus status = PurchaseRules.preview(company, purchaseQuote, params);
        if (status != PurchaseStatus.AFFORDABLE) {
            return S2CActionResult.error(Component.translatable("enum.createkontor.purchase_status." + status.toString().toLowerCase()));
        }

        try {
            PurchaseRules.buy(company, economy, purchaseQuote, economy.currentDay(), params);
        } catch (IllegalStateException e) {
            return S2CActionResult.error(ComponentFormatting.error("Failed: " + e.getMessage()));
        }

        economyData.setDirty();
        companyData.setDirty();

        int packages = queueDelivery(goods, address);
        return S2CActionResult.success(Component.translatable("message.createkontor.buyer_desk.buy.success", packages));
    }

    public int queueDelivery(List<Goods> goods, String address) {
        if (goods.isEmpty()) throw new IllegalArgumentException("goods must not be empty.");

        List<ItemStack> stacks = new ArrayList<>();
        for (Goods entry : goods) {
            int max = Math.min(new ItemStack(entry.item()).getMaxStackSize(), MAX_STACK_IN_PACKAGE);
            for (int left = entry.quantity(); left > 0; left -= max) {
                stacks.add(new ItemStack(entry.item(), Math.min(left, max)));
            }
        }
        int packages = 0;
        for (int from = 0; from < stacks.size(); from += PackageItem.SLOTS) {
            ItemStack box = PackageItem.containing(stacks.subList(from, Math.min(from + PackageItem.SLOTS, stacks.size())));
            if (address.isBlank()) {
                address = "";
            }
            PackageItem.addAddress(box, address);
            pendingPackages.add(box);
            packages++;
        }
        update();
        return packages;
    }

    public void dropPending() {
        if (level == null || level.isClientSide()) {
            return;
        }
        pendingPackages.forEach(box -> Block.popResource(level, worldPosition, box));
        pendingPackages.clear();
        pendingCount = 0;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        update();
    }

    private void update() {
        if (level == null || level.isClientSide()) {
            return;
        }
        List<PackagePortBlockEntity> connected = connectedPorts();
        while (!pendingPackages.isEmpty() && deliver(pendingPackages.get(0), connected)) {
            pendingPackages.remove(0);
        }
        if (connected.size() != portCount || pendingPackages.size() != pendingCount) {
            portCount = connected.size();
            pendingCount = pendingPackages.size();
            notifyUpdate();
        }
    }

    private static boolean deliver(ItemStack box, List<PackagePortBlockEntity> ports) {
        for (PackagePortBlockEntity port : ports) {
            if (ItemHandlerHelper.insertItem(port.inventory, box.copy(), false).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        if (portCount == 0) {
            CreateLang.builder()
                    .add(ComponentFormatting.errorTranslatable("goggle.createkontor.buyer_desk.no_postbox"))
                    .forGoggles(tooltip, 1);
            CreateLang.builder()
                    .add(ComponentFormatting.standardTranslatable("goggle.createkontor.buyer_desk.connect_hint"))
                    .forGoggles(tooltip, 2);
        } else {
            CreateLang.builder()
                    .add(Component.translatable("goggle.createkontor.buyer_desk.postboxes",
                            ComponentFormatting.highlightStandard(String.valueOf(portCount))).withStyle(ComponentFormatting.DEFAULT))
                    .forGoggles(tooltip, 1);
        }
        if (pendingCount > 0) {
            CreateLang.builder()
                    .add(Component.translatable("goggle.createkontor.buyer_desk.pending",
                            ComponentFormatting.highlightError(String.valueOf(pendingCount))).withStyle(ComponentFormatting.ERROR))
                    .forGoggles(tooltip, 1);
        }
        return true;
    }


    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString(TAG_ADDRESS, address);
        if (clientPacket) {
            tag.putInt(TAG_PORT_COUNT, portCount);
            tag.putInt(TAG_PENDING_COUNT, pendingCount);
            return;
        }
        ListTag list = new ListTag();
        for (ItemStack box : pendingPackages) {
            list.add(box.saveOptional(registries));
        }
        tag.put(TAG_PENDING, list);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        address = tag.getString(TAG_ADDRESS);
        if (clientPacket) {
            portCount = tag.getInt(TAG_PORT_COUNT);
            pendingCount = tag.getInt(TAG_PENDING_COUNT);
            return;
        }
        pendingPackages.clear();
        ListTag list = tag.getList(TAG_PENDING, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ItemStack box = ItemStack.parseOptional(registries, list.getCompound(i));
            if (!box.isEmpty()) {
                pendingPackages.add(box);
            }
        }
        pendingCount = pendingPackages.size();
    }
}
