package com.timder.kontor.game.block;

import com.simibubi.create.foundation.utility.CreateLang;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.logistics.LogisticsDispatchRules;
import com.timder.kontor.core.company.logistics.LogisticsConnectionRules;
import com.timder.kontor.core.company.order.Order;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.LogisticsNetworkAccess;
import com.timder.kontor.game.block.employee.EmployeeDeskBlockEntity;
import com.timder.kontor.util.ComponentFormatting;
import com.timder.kontor.util.PlayerNotifications;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LogisticsManagerDeskBlockEntity extends EmployeeDeskBlockEntity {

    private static final String TAG_CONNECTION_STATUS = "ConnectionStatus";
    private static final String TAG_ORDERING_ENABLED = "OrderingEnabled";
    private static final String TAG_ADDRESS = "OrderAddress";
    private static final String TAG_ORDERED = "OrderedQuantities";
    private static final String TAG_ORDER_NUMBER = "Order";
    private static final String TAG_ORDERED_QUANTITY = "Ordered";
    private static final String TAG_TICKS_UNTIL_ORDER = "TicksUntilOrder";
    private static final String TAG_ORDERING_STATUS = "OrderingStatus";
    private static final String TAG_MINIMUM_STOCK = "MinimumStock";

    public static final int MAX_ADDRESS_LENGTH = 25;
    public static final int ORDER_INTERVAL_TICKS = 200;
    public static final int MAX_MINIMUM_STOCK = 4096;

    public enum ConnectionStatus {
        WAITING,
        CONNECTED,
        NO_EMPLOYEE,
        NO_LINK,
        SEVERAL_LINKS,
        LEGAL_FORM_NOT_ALLOWED,
        COMPANY_HAS_OTHER_NETWORK,
        NETWORK_USED_BY_OTHER_COMPANY,
        NETWORK_OWNER_NOT_MEMBER;

        public String translationKey() {
            return "enum.createkontor.logistics_manager_desk_status." + name().toLowerCase(Locale.ROOT);
        }
    }

    public enum DisconnectReason {
        NO_EMPLOYEE,
        NO_LINK,
        SEVERAL_LINKS,
        LINK_CHANGED,
        LEGAL_FORM_NOT_ALLOWED,
        NETWORK_USED_BY_OTHER_COMPANY,
        NETWORK_OWNER_NOT_MEMBER,
        DESK_REMOVED;
    }

    public enum OrderingStatus {
        OFF,
        NOT_CONNECTED,
        NO_ADDRESS,
        WAITING,
        NOTHING_TO_ORDER,
        ORDERED,
        BUSY;

        public String translationKey() {
            return "enum.createkontor.logistics_manager_desk_ordering." + name().toLowerCase(Locale.ROOT);
        }
    }

    private boolean orderingEnabled = false;
    private String orderAddress = "";
    private int minimumStock = 0;

    private final Map<Long, Integer> orderedQuantities = new LinkedHashMap<>();
    private int ticksUntilOrder = 0;
    private OrderingStatus orderingStatus = OrderingStatus.NOT_CONNECTED;

    private ConnectionStatus connectionStatus = ConnectionStatus.WAITING;

    public LogisticsManagerDeskBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected @Nullable String boundResourceKey() {
        return LogisticsManagerDeskBlock.RESOURCE_KEY;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public boolean isOrderingEnabled() {
        return orderingEnabled;
    }

    public String getOrderAddress() {
        return orderAddress;
    }

    public int getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(int value) {
        if (level == null || level.isClientSide()) {
            return;
        }
        int clamped = Math.max(0, Math.min(MAX_MINIMUM_STOCK, value));
        if (clamped == minimumStock) {
            return;
        }
        minimumStock = clamped;
        notifyUpdate();
    }

    public Map<Order, Double> getOrderedQuantitiesFractions() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Map.of();
        }
        Company company = CompanySavedData.get(serverLevel.getServer()).getRegistry().get(getCompanyId()).orElse(null);
        if (company == null) {
            return Map.of();
        }

        Map<Order, Double> percentMap = new LinkedHashMap<>();
        for (long orderNumber : orderedQuantities.keySet()) {
            Order order;
            try {
                order = company.orderBook().get(orderNumber);
            } catch (NoSuchElementException e) {
                continue;
            }
            if (order.getQuantity() > 0) {
                percentMap.put(order, (double) orderedQuantities.get(orderNumber) / order.getQuantity());
            }
        }
        return percentMap;
    }

    public OrderingStatus getOrderingStatus() {
        return orderingStatus;
    }

    public void setOrderingEnabled(boolean enabled) {
        if (level == null || level.isClientSide() || enabled == orderingEnabled) {
            return;
        }
        orderingEnabled = enabled;
        notifyUpdate();
    }

    public void setOrderAddress(@Nullable String value) {
        if (level == null || level.isClientSide()) {
            return;
        }
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.length() > MAX_ADDRESS_LENGTH) {
            cleaned = cleaned.substring(0, MAX_ADDRESS_LENGTH);
        }
        if (cleaned.equals(orderAddress)) {
            return;
        }
        orderAddress = cleaned;
        notifyUpdate();
    }

    public void resetOrderProgress() {
        if (level == null || level.isClientSide()) {
            return;
        }
        boolean anyProgress = orderedQuantities.values().stream().anyMatch(ordered -> ordered != 0);
        if (!anyProgress) {
            return;
        }
        orderedQuantities.replaceAll((order, ordered) -> 0);
        setChanged();
    }

    public boolean resetOrderProgress(long orderNumber) {
        if (level == null || level.isClientSide()) {
            return false;
        }
        Integer ordered = orderedQuantities.get(orderNumber);
        if (ordered == null || ordered == 0) {
            return false;
        }
        orderedQuantities.put(orderNumber, 0);
        setChanged();
        return true;
    }

    @Override
    protected void onCompanyChanged(@Nullable CompanyId oldId, @Nullable CompanyId newId) {
        if (oldId != null && level instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            CompanySavedData data = CompanySavedData.get(server);
            data.getRegistry().get(oldId).ifPresent(company -> disconnect(server, data, company, DisconnectReason.DESK_REMOVED));
        }
        connectionStatus = ConnectionStatus.WAITING;
        orderingStatus = OrderingStatus.NOT_CONNECTED;
        ticksUntilOrder = 0;
        orderedQuantities.clear();
        super.onCompanyChanged(oldId, newId);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level instanceof ServerLevel serverLevel) {
            checkConnection(serverLevel);
            checkOrdering(serverLevel);
        }
    }

    private void checkOrdering(ServerLevel level) {
        CompanyId companyId = getCompanyId();
        if (companyId == null) {
            updateOrderingStatus(OrderingStatus.NOT_CONNECTED);
            return;
        }
        MinecraftServer server = level.getServer();
        Company company = CompanySavedData.get(server).getRegistry().get(companyId).orElse(null);
        if (company == null || !company.isActive()) {
            return;
        }

        Set<Long> openOrders = new HashSet<>();
        for (Order order : company.orderBook().allOrders()) {
            openOrders.add(order.getNumber());
        }
        if (orderedQuantities.keySet().retainAll(openOrders)) {
            setChanged();
        }

        UUID network = company.logistics().networkId();
        if (!orderingEnabled) {
            updateOrderingStatus(OrderingStatus.OFF);
            return;
        }
        if (network == null) {
            updateOrderingStatus(OrderingStatus.NOT_CONNECTED);
            return;
        }
        if (orderAddress.isBlank()) {
            updateOrderingStatus(OrderingStatus.NO_ADDRESS);
            return;
        }

        if (ticksUntilOrder > 0) {
            ticksUntilOrder = Math.max(0, ticksUntilOrder - CHECK_INTERVAL_TICKS);
        }
        if (ticksUntilOrder > 0) {
            if (orderingStatus == OrderingStatus.OFF || orderingStatus == OrderingStatus.NOT_CONNECTED || orderingStatus == OrderingStatus.NO_ADDRESS) {
                updateOrderingStatus(OrderingStatus.WAITING);
            }
            return;
        }

        Economy economy = EconomySavedData.get(server).getEconomy();
        Set<ItemId> products = new LinkedHashSet<>();
        for (Order order : company.orderBook().allOrders()) {
            products.add(order.getProduct());
        }
        Map<ItemId, Integer> stock = products.isEmpty() ? Map.of() : LogisticsNetworkAccess.stockOf(network, products);
        List<LogisticsDispatchRules.Line> plan = LogisticsDispatchRules.plan(company, economy, stock, orderedQuantities, minimumStock);

        ticksUntilOrder = ORDER_INTERVAL_TICKS;
        setChanged();
        if (plan.isEmpty()) {
            updateOrderingStatus(OrderingStatus.NOTHING_TO_ORDER);
            return;
        }
        Map<ItemId, Integer> totals = LogisticsDispatchRules.totalsByProduct(plan);
        if (!LogisticsNetworkAccess.requestPackages(network, orderAddress, totals)) {
            updateOrderingStatus(OrderingStatus.BUSY);
            ticksUntilOrder = 0;
            return;
        }
        for (LogisticsDispatchRules.Line line : plan) {
            orderedQuantities.merge(line.orderNumber(), line.quantity(), Integer::sum);
        }
        setChanged();
        CreateKontor.LOGGER.info("Logistics manager of {} ordered {} to '{}'.", company.name(), totals, orderAddress);
        updateOrderingStatus(OrderingStatus.ORDERED);
    }

    private void updateOrderingStatus(OrderingStatus newStatus) {
        if (newStatus == orderingStatus) {
            return;
        }
        orderingStatus = newStatus;
        notifyUpdate();
    }

    private void checkConnection(ServerLevel level) {
        CompanyId companyId = getCompanyId();
        if (companyId == null) {
            updateStatus(ConnectionStatus.WAITING);
            return;
        }

        MinecraftServer server = level.getServer();
        CompanySavedData data = CompanySavedData.get(server);
        Company company = data.getRegistry().get(companyId).orElse(null);
        if (company == null || !company.isActive()
                || company.boundResourceCount(LogisticsManagerDeskBlock.RESOURCE_KEY) != 1) {
            updateStatus(ConnectionStatus.WAITING);
            return;
        }
        if (!allNeighborsLoaded(level)) {
            return;
        }

        UUID wanted = null;
        ConnectionStatus problem = null;
        List<UUID> links = LogisticsNetworkAccess.networksOfLinksAttachedTo(level, getBlockPos());
        if (!hasEmployee()) {
            problem = ConnectionStatus.NO_EMPLOYEE;
        } else if (links.isEmpty()) {
            problem = ConnectionStatus.NO_LINK;
        } else if (links.size() > 1) {
            problem = ConnectionStatus.SEVERAL_LINKS;
        } else {
            wanted = links.get(0);
        }

        UUID current = company.logistics().networkId();
        if (current != null && !current.equals(wanted)) {
            disconnect(server, data, company, wanted == null ? reasonOf(problem) : DisconnectReason.LINK_CHANGED);
        }
        if (wanted == null) {
            updateStatus(problem);
            return;
        }

        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        boolean wasConnected = company.logistics().isConnected();
        UUID owner = LogisticsNetworkAccess.ownerOf(wanted);
        LogisticsConnectionRules.Check check = LogisticsConnectionRules.connect(data.getRegistry(), company, wanted, owner, params);

        if (check.isOk()) {
            if (!wasConnected) {
                data.setDirty();
                PlayerNotifications.sendLogisticsConnectedNotification(server, company);
            }
            updateStatus(ConnectionStatus.CONNECTED);
            return;
        }
        ConnectionStatus refused = switch (check) {
            case OK -> ConnectionStatus.CONNECTED;
            case LEGAL_FORM_NOT_ALLOWED -> ConnectionStatus.LEGAL_FORM_NOT_ALLOWED;
            case COMPANY_HAS_OTHER_NETWORK -> ConnectionStatus.COMPANY_HAS_OTHER_NETWORK;
            case NETWORK_USED_BY_OTHER_COMPANY -> ConnectionStatus.NETWORK_USED_BY_OTHER_COMPANY;
            case NETWORK_OWNER_NOT_MEMBER -> ConnectionStatus.NETWORK_OWNER_NOT_MEMBER;
        };
        if (wasConnected) {
            disconnect(server, data, company, reasonOf(refused));
        }
        updateStatus(refused);
    }

    private static DisconnectReason reasonOf(ConnectionStatus status) {
        return switch (status) {
            case NO_EMPLOYEE -> DisconnectReason.NO_EMPLOYEE;
            case NO_LINK -> DisconnectReason.NO_LINK;
            case SEVERAL_LINKS -> DisconnectReason.SEVERAL_LINKS;
            case LEGAL_FORM_NOT_ALLOWED -> DisconnectReason.LEGAL_FORM_NOT_ALLOWED;
            case NETWORK_USED_BY_OTHER_COMPANY -> DisconnectReason.NETWORK_USED_BY_OTHER_COMPANY;
            case NETWORK_OWNER_NOT_MEMBER -> DisconnectReason.NETWORK_OWNER_NOT_MEMBER;
            case COMPANY_HAS_OTHER_NETWORK -> DisconnectReason.LINK_CHANGED;
            case WAITING, CONNECTED -> throw new IllegalArgumentException(status + " is not a reason to disconnect.");
        };
    }

    private void updateStatus(ConnectionStatus newStatus) {
        if (newStatus == connectionStatus) {
            return;
        }
        connectionStatus = newStatus;
        notifyUpdate();
    }

    private static void disconnect(MinecraftServer server, CompanySavedData data, Company company, DisconnectReason reason) {
        if (!company.logistics().isConnected()) {
            return;
        }
        Economy economy = EconomySavedData.get(server).getEconomy();
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        Money booked = LogisticsConnectionRules.disconnect(company, economy.currentDay(), economy.ticksElapsed(), params);
        data.setDirty();
        PlayerNotifications.sendLogisticsDisconnectedNotification(server, company, Component.translatable("message.createkontor.logistics.reason." + reason.name().toLowerCase(Locale.ROOT)), booked);
    }

    private boolean allNeighborsLoaded(ServerLevel level) {
        for (Direction direction : Direction.values()) {
            if (!level.isLoaded(getBlockPos().relative(direction))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (!isBoundToCompany()) {
            return true;
        }

        Component value = switch (connectionStatus) {
            case CONNECTED -> ComponentFormatting.highlightStandardTranslatable(connectionStatus.translationKey());
            case WAITING -> ComponentFormatting.standardTranslatable(connectionStatus.translationKey());
            default -> ComponentFormatting.highlightErrorTranslatable(connectionStatus.translationKey());
        };
        CreateLang.builder()
                .add(Component.translatable("goggle.createkontor.logistics_manager_desk.status", value).withStyle(ComponentFormatting.DEFAULT))
                .forGoggles(tooltip, 1);

        Component ordering = switch (orderingStatus) {
            case ORDERED -> ComponentFormatting.highlightStandardTranslatable(orderingStatus.translationKey());
            case NO_ADDRESS, BUSY -> ComponentFormatting.highlightErrorTranslatable(orderingStatus.translationKey());
            default -> ComponentFormatting.standardTranslatable(orderingStatus.translationKey());
        };
        CreateLang.builder()
                .add(Component.translatable("goggle.createkontor.logistics_manager_desk.ordering", ordering).withStyle(ComponentFormatting.DEFAULT))
                .forGoggles(tooltip, 1);
        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putBoolean(TAG_ORDERING_ENABLED, orderingEnabled);
        tag.putString(TAG_ADDRESS, orderAddress);
        tag.putInt(TAG_MINIMUM_STOCK, minimumStock);
        if (clientPacket) {
            tag.putString(TAG_CONNECTION_STATUS, connectionStatus.name());
            tag.putString(TAG_ORDERING_STATUS, orderingStatus.name());
        } else {
            tag.putInt(TAG_TICKS_UNTIL_ORDER, ticksUntilOrder);
            ListTag ordered = new ListTag();
            for (Map.Entry<Long, Integer> entry : orderedQuantities.entrySet()) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putLong(TAG_ORDER_NUMBER, entry.getKey());
                entryTag.putInt(TAG_ORDERED_QUANTITY, entry.getValue());
                ordered.add(entryTag);
            }
            tag.put(TAG_ORDERED, ordered);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        if (tag.contains(TAG_ORDERING_ENABLED)) {
            orderingEnabled = tag.getBoolean(TAG_ORDERING_ENABLED);
        }
        if (tag.contains(TAG_ADDRESS)) {
            orderAddress = tag.getString(TAG_ADDRESS);
        }
        if (tag.contains(TAG_MINIMUM_STOCK)) {
            minimumStock = Math.max(0, Math.min(MAX_MINIMUM_STOCK, tag.getInt(TAG_MINIMUM_STOCK)));
        }

        if (clientPacket) {
            try {
                connectionStatus = ConnectionStatus.valueOf(tag.getString(TAG_CONNECTION_STATUS));
            } catch (IllegalArgumentException e) {
                connectionStatus = ConnectionStatus.WAITING;
            }
            try {
                orderingStatus = OrderingStatus.valueOf(tag.getString(TAG_ORDERING_STATUS));
            } catch (IllegalArgumentException e) {
                orderingStatus = OrderingStatus.NOT_CONNECTED;
            }
        } else {
            if (tag.contains(TAG_TICKS_UNTIL_ORDER)) {
                ticksUntilOrder = Math.max(0, Math.min(ORDER_INTERVAL_TICKS, tag.getInt(TAG_TICKS_UNTIL_ORDER)));
            }
            orderedQuantities.clear();
            ListTag ordered = tag.getList(TAG_ORDERED, Tag.TAG_COMPOUND);
            for (int i = 0; i < ordered.size(); i++) {
                CompoundTag entryTag = ordered.getCompound(i);
                int quantity = entryTag.getInt(TAG_ORDERED_QUANTITY);
                if (quantity > 0) {
                    orderedQuantities.put(entryTag.getLong(TAG_ORDER_NUMBER), quantity);
                }
            }
        }
    }
}
