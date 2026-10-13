package com.timder.kontor.game.block;

import com.simibubi.create.foundation.utility.CreateLang;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.logistics.LogisticsConnectionRules;
import com.timder.kontor.core.economy.Economy;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class LogisticsManagerDeskBlockEntity extends EmployeeDeskBlockEntity {

    private static final String TAG_CONNECTION_STATUS = "ConnectionStatus";

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

        public boolean isRefusal() {
            return switch (this) {
                case SEVERAL_LINKS, LEGAL_FORM_NOT_ALLOWED, COMPANY_HAS_OTHER_NETWORK,
                     NETWORK_USED_BY_OTHER_COMPANY, NETWORK_OWNER_NOT_MEMBER -> true;
                case WAITING, CONNECTED, NO_EMPLOYEE, NO_LINK -> false;
            };
        }

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

    private ConnectionStatus connectionStatus = ConnectionStatus.WAITING;

    public LogisticsManagerDeskBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected @Nullable String boundResourceKey() {
        return LogisticsManagerDeskBlock.RESOURCE_KEY;
    }

    public ConnectionStatus connectionStatus() {
        return connectionStatus;
    }

    @Override
    protected void onCompanyChanged(@Nullable CompanyId oldId, @Nullable CompanyId newId) {
        if (oldId != null && level instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            CompanySavedData data = CompanySavedData.get(server);
            data.getRegistry().get(oldId).ifPresent(company -> disconnect(server, data, company, DisconnectReason.DESK_REMOVED));
        }
        connectionStatus = ConnectionStatus.WAITING;
        super.onCompanyChanged(oldId, newId);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level instanceof ServerLevel serverLevel) {
            checkConnection(serverLevel);
        }
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
        PlayerNotifications.sendLogisticsDisconnectedNotification(server, company, Component.translatable("message.createkontor.logistics.reason." + reason.name().toLowerCase()), booked);
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
        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (clientPacket) {
            tag.putString(TAG_CONNECTION_STATUS, connectionStatus.name());
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (clientPacket) {
            try {
                connectionStatus = ConnectionStatus.valueOf(tag.getString(TAG_CONNECTION_STATUS));
            } catch (IllegalArgumentException e) {
                connectionStatus = ConnectionStatus.WAITING;
            }
        }
    }
}
