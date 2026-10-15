package com.timder.kontor.game.block.buyer;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.CompanyParams;
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
import com.timder.kontor.game.LogisticsNetworkAccess;
import com.timder.kontor.game.block.employee.EmployeeDeskBlockEntity;
import com.timder.kontor.game.network.S2CActionResult;
import com.timder.kontor.util.ComponentFormatting;
import java.util.*;
import javax.annotation.Nullable;
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

public class BuyerDeskBlockEntity extends EmployeeDeskBlockEntity {

    private static final int MAX_STACK_IN_PACKAGE = 64;
    public static final int MAX_GOOD_SIZE = 9999;
    public static final int MAX_ADDRESS_LENGTH = 25;
    public static final int MAX_REFILL_ENTRIES = 32;

    private static final String TAG_PORT_COUNT = "ConnectedPorts";
    private static final String TAG_PENDING = "PendingPackages";
    private static final String TAG_PENDING_COUNT = "PendingCount";
    private static final String TAG_ADDRESS = "Address";
    private static final String TAG_REFILL_ADDRESS = "RefillAddress";
    private static final String TAG_REFILL = "RefillTargets";
    private static final String TAG_REFILL_ITEM = "Item";
    private static final String TAG_REFILL_MIN = "MinStock";

    private String address = "";
    private String refillAddress = "";
    private final Map<ItemId, Integer> refillTargets = new LinkedHashMap<>();

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

    public Map<ItemId, Integer> getRefillTargets() {
        return Collections.unmodifiableMap(refillTargets);
    }

    public boolean setRefillTarget(ItemId item, int minStock) {
        if (level == null || level.isClientSide()) return false;
        Objects.requireNonNull(item, "item must not be null.");
        if (minStock < 0 || minStock > MAX_GOOD_SIZE) return false;
        if (!refillTargets.containsKey(item) && refillTargets.size() >= MAX_REFILL_ENTRIES) return false;
        Integer previous = refillTargets.put(item, minStock);
        if (previous == null || previous != minStock) {
            setChanged();
        }
        return true;
    }

    public boolean removeRefillTarget(ItemId item) {
        if (level == null || level.isClientSide()) return false;
        if (refillTargets.remove(item) == null) return false;
        setChanged();
        return true;
    }

    public String getRefillAddress() {
        return refillAddress;
    }

    public void setRefillAddress(String value) {
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
        refillAddress = cleaned;
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

    @Override
    protected void onCompanyChanged(@Nullable CompanyId oldId, @Nullable CompanyId newId) {
        refillTargets.clear();
        super.onCompanyChanged(oldId, newId);
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
        if (connectedPorts().isEmpty())
            return S2CActionResult.error(Component.translatable("ui.createkontor.buyer_desk.no_postbox"));

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

            if (quantity < 1 || quantity > MAX_GOOD_SIZE || item.isEmpty())
                return S2CActionResult.error(Component.literal("Quantity invalid."));

            try {
                lines.add(
                        PurchaseOffers.offerFor(economy, params, entry.getKey()).lineFor(quantity));
            } catch (IllegalArgumentException e) {
                return S2CActionResult.error(Component.literal("Failed: " + e.getMessage()));
            }
            goods.add(new BuyerDeskBlockEntity.Goods(item.get(), quantity));
        }

        PurchaseQuote purchaseQuote = new PurchaseQuote(lines);
        if (purchaseQuote.total().cents() != expectedTotalCents)
            return S2CActionResult.error(Component.literal("Prices changed. Try again."));

        PurchaseStatus status = PurchaseRules.preview(company, purchaseQuote, params);
        if (status != PurchaseStatus.AFFORDABLE) {
            return S2CActionResult.error(Component.translatable(
                    "enum.createkontor.purchase_status." + status.toString().toLowerCase()));
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
            ItemStack box =
                    PackageItem.containing(stacks.subList(from, Math.min(from + PackageItem.SLOTS, stacks.size())));
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
        if (level instanceof ServerLevel serverLevel) {
            refill(serverLevel);
        }
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

    private void refill(ServerLevel serverLevel) {
        CompanyId companyId = getCompanyId();
        if (refillTargets.isEmpty() || companyId == null || !hasEmployee()) {
            return;
        }
        CompanySavedData companyData = CompanySavedData.get(serverLevel.getServer());
        Company company = companyData.getRegistry().get(companyId).orElse(null);
        if (company == null || !company.isActive() || !company.isOperational()) {
            return;
        }
        UUID network = company.logistics().networkId();
        if (network == null || connectedPorts().isEmpty()) {
            return;
        }

        EconomySavedData economyData = EconomySavedData.get(serverLevel.getServer());
        Economy economy = economyData.getEconomy();
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        Map<ItemId, Integer> stock = LogisticsNetworkAccess.stockOf(network, refillTargets.keySet());

        List<PurchaseQuote.Line> lines = new ArrayList<>();
        List<Goods> goods = new ArrayList<>();
        for (Map.Entry<ItemId, Integer> target : refillTargets.entrySet()) {
            ItemId id = target.getKey();
            ResourceLocation location = ResourceLocation.tryParse(id.value());
            Optional<Item> item = location == null ? Optional.empty() : BuiltInRegistries.ITEM.getOptional(location);
            if (item.isEmpty()) {
                continue;
            }

            int effective = stock.getOrDefault(id, 0) + LogisticsNetworkAccess.promisedOf(network, id);
            int missing = target.getValue() - effective;
            if (missing < 1) {
                continue;
            }

            // int quantity = Math.min(MAX_GOOD_SIZE, Math.max(missing, new ItemStack(item.get()).getMaxStackSize()));
            int quantity = Math.min(MAX_GOOD_SIZE, missing);
            try {
                lines.add(PurchaseOffers.offerFor(economy, params, id).lineFor(quantity));
            } catch (IllegalArgumentException e) {
                continue;
            }

            if (PurchaseRules.preview(company, new PurchaseQuote(lines), params) != PurchaseStatus.AFFORDABLE) {
                lines.remove(lines.size() - 1);
                continue;
            }

            goods.add(new Goods(item.get(), quantity));
        }

        if (lines.isEmpty()) {
            return;
        }

        PurchaseQuote quote = new PurchaseQuote(lines);
        try {
            PurchaseRules.buy(company, economy, quote, economy.currentDay(), params);
        } catch (IllegalStateException e) {
            CreateKontor.LOGGER.warn("Buyer refill of {} failed: {}", company.name(), e.getMessage());
            return;
        }

        economyData.setDirty();
        companyData.setDirty();

        queueDelivery(goods, refillAddress);
        for (PurchaseQuote.Line line : lines) {
            LogisticsNetworkAccess.promise(network, line.item(), line.quantity());
        }
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
                    .add(Component.translatable(
                                    "goggle.createkontor.buyer_desk.postboxes",
                                    ComponentFormatting.highlightStandard(String.valueOf(portCount)))
                            .withStyle(ComponentFormatting.DEFAULT))
                    .forGoggles(tooltip, 1);
        }
        if (pendingCount > 0) {
            CreateLang.builder()
                    .add(Component.translatable(
                                    "goggle.createkontor.buyer_desk.pending",
                                    ComponentFormatting.highlightError(String.valueOf(pendingCount)))
                            .withStyle(ComponentFormatting.ERROR))
                    .forGoggles(tooltip, 1);
        }
        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString(TAG_ADDRESS, address);
        tag.putString(TAG_REFILL_ADDRESS, refillAddress);
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
        ListTag targets = new ListTag();
        for (Map.Entry<ItemId, Integer> entry : refillTargets.entrySet()) {
            CompoundTag targetTag = new CompoundTag();
            targetTag.putString(TAG_REFILL_ITEM, entry.getKey().value());
            targetTag.putInt(TAG_REFILL_MIN, entry.getValue());
            targets.add(targetTag);
        }
        tag.put(TAG_REFILL, targets);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        address = tag.getString(TAG_ADDRESS);
        refillAddress = tag.getString(TAG_REFILL_ADDRESS);
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
        refillTargets.clear();
        ListTag targets = tag.getList(TAG_REFILL, Tag.TAG_COMPOUND);
        for (int i = 0; i < targets.size() && refillTargets.size() < MAX_REFILL_ENTRIES; i++) {
            CompoundTag targetTag = targets.getCompound(i);
            String raw = targetTag.getString(TAG_REFILL_ITEM);
            int min = Math.max(0, Math.min(MAX_GOOD_SIZE, targetTag.getInt(TAG_REFILL_MIN)));
            if (raw.isBlank()) continue;
            refillTargets.put(new ItemId(raw), min);
        }
    }
}
