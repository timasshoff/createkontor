package com.timder.kontor.game.block;

import com.simibubi.create.AllSoundEvents;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.*;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.block.company.AbstractCompanyBlockEntity;
import com.timder.kontor.game.network.S2CActionResult;
import com.timder.kontor.util.ComponentFormatting;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PaymentTerminalBlockEntity extends AbstractCompanyBlockEntity {

    public static final int DEFAULT_SIGNAL_SECONDS = 5;
    public static final SignalMode DEFAULT_SIGNAL_MODE = SignalMode.CONTINUOUS;
    public static final int PULSE_TICKS = 10;

    private String terminalName = "";
    private long priceCents = 0;

    private SignalMode signalMode = DEFAULT_SIGNAL_MODE;
    private int signalSeconds = DEFAULT_SIGNAL_SECONDS;
    private int signalTicksRemaining = 0;

    public enum SignalMode {
        CONTINUOUS,
        PULSE
    }

    public PaymentTerminalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public String getTerminalName() {
        return terminalName;
    }

    public long getPriceCents() {
        return priceCents;
    }

    public boolean isPriceSet() {
        return priceCents > 0;
    }

    public SignalMode getSignalMode() {
        return signalMode;
    }

    public int getSignalSeconds() {
        return signalSeconds;
    }

    public boolean isCycleRunning() {
        return signalTicksRemaining > 0;
    }

    public void setTerminalName(String value) {
        if (level == null || level.isClientSide()) {
            return;
        }
        String cleaned = value == null ? "" : value.trim();
        int max = CompanyConfig.PAYMENT_TERMINAL_MAX_NAME_LENGTH.get();
        if (cleaned.length() > max) {
            cleaned = cleaned.substring(0, max);
        }
        if (cleaned.equals(terminalName)) {
            return;
        }
        terminalName = cleaned;
        notifyUpdate();
    }

    public void setPriceCents(long value) {
        if (level == null || level.isClientSide()) {
            return;
        }
        long max = Money.ofDollars(CompanyConfig.PAYMENT_TERMINAL_MAX_PRICE_IN_DOLLARS.get())
                .cents();
        long cleaned = Math.max(0, Math.min(max, value));
        if (cleaned == priceCents) {
            return;
        }
        priceCents = cleaned;
        notifyUpdate();
    }

    public void setSignalMode(SignalMode mode) {
        if (level == null || level.isClientSide() || mode == null || mode == signalMode) {
            return;
        }
        signalMode = mode;
        cancelCycle();
        notifyUpdate();
    }

    public void setSignalSeconds(int value) {
        if (level == null || level.isClientSide()) {
            return;
        }
        int min = CompanyConfig.PAYMENT_TERMINAL_MIN_SIGNAL_SECONDS.get();
        int cleaned = Math.max(min, Math.min(CompanyConfig.PAYMENT_TERMINAL_MAX_SIGNAL_SECONDS.get(), value));
        if (cleaned == signalSeconds) {
            return;
        }
        signalSeconds = cleaned;
        cancelCycle();
        notifyUpdate();
    }

    public Optional<S2CActionResult> activateFree(ServerPlayer player) {
        if (!startSignal())
            return S2CActionResult.error(Component.translatable("message.createkontor.payment_terminal.busy"));
        player.closeContainer();
        return S2CActionResult.empty();
    }

    public Optional<S2CActionResult> pay(ServerPlayer player, long expectedPriceCents) {
        if (!(level instanceof ServerLevel serverLevel)) return S2CActionResult.illegalEnvironment();

        CompanySavedData companyData = CompanySavedData.get(serverLevel.getServer());
        Company owner = companyData.getRegistry().get(getCompanyId()).orElse(null);
        if (owner == null) return S2CActionResult.error(Component.literal("Company does not exist."));
        Company payer = companyData.getRegistry().companyOf(player.getUUID()).orElse(null);
        if (payer == null) return S2CActionResult.error(Component.literal("You are not in a company."));

        if (isCycleRunning())
            return S2CActionResult.error(Component.translatable("message.createkontor.payment_terminal.busy"));
        if (priceCents != expectedPriceCents) {
            return S2CActionResult.error(Component.translatable("message.createkontor.payment_terminal.price_changed"));
        }
        if (!isPriceSet()) {
            return S2CActionResult.error(Component.translatable("message.createkontor.payment_terminal.disabled"));
        }

        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        Money price = Money.ofCents(priceCents);
        TransferStatus status = TransferRules.preview(payer, owner, price, params);
        if (status != TransferStatus.OK) {
            return S2CActionResult.error(Component.translatable(
                    "enum.createkontor.transfer_status." + status.toString().toLowerCase()));
        }

        String label = terminalName.isEmpty() ? "Payment Terminal" : terminalName;
        TransferBookings bookings = new TransferBookings(
                BookingKind.TERMINAL_PAYMENT,
                label + " (" + owner.name() + ")",
                BookingKind.TERMINAL_REVENUE,
                payer.name() + " (" + label + ")");
        try {
            long day =
                    EconomySavedData.get(serverLevel.getServer()).getEconomy().currentDay();
            TransferRules.transfer(payer, owner, price, bookings, day, params);
        } catch (IllegalStateException e) {
            return S2CActionResult.error(ComponentFormatting.error("Failed: " + e.getMessage()));
        }

        companyData.setDirty();
        startSignal();
        player.closeContainer();
        level.playSound(
                null, getBlockPos(), AllSoundEvents.STOCK_TICKER_TRADE.getMainEvent(), SoundSource.BLOCKS, 0.6f, 1.0f);
        return S2CActionResult.success(Component.translatable(
                "message.createkontor.payment_terminal.paid",
                ComponentFormatting.moneyNegative(price),
                ComponentFormatting.highlightStandard(owner.name())));
    }

    private void cancelCycle() {
        signalTicksRemaining = 0;
        setState(PaymentTerminalBlock.TerminalState.IDLE);
    }

    public boolean startSignal() {
        if (level == null || level.isClientSide() || isCycleRunning()) {
            return false;
        }

        signalTicksRemaining = signalMode == SignalMode.PULSE ? signalSeconds * 20 + PULSE_TICKS : signalSeconds * 20;
        setState(stateFor(signalMode, signalSeconds, signalTicksRemaining));
        setChanged();
        return true;
    }

    static boolean isSignalOn(SignalMode mode, int seconds, int remaining) {
        if (remaining <= 0) {
            return false;
        }
        if (mode == SignalMode.CONTINUOUS) {
            return true;
        }
        return remaining > seconds * 20 || remaining <= PULSE_TICKS;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (level != null && !level.isClientSide()) {
            setState(stateFor(signalMode, signalSeconds, signalTicksRemaining));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide() || signalTicksRemaining <= 0) {
            return;
        }
        signalTicksRemaining--;
        setState(stateFor(signalMode, signalSeconds, signalTicksRemaining));
        if (signalTicksRemaining == 0 || signalTicksRemaining % 20 == 0) {
            setChanged();
        }
    }

    private static PaymentTerminalBlock.TerminalState stateFor(SignalMode mode, int seconds, int remaining) {
        if (remaining <= 0) {
            return PaymentTerminalBlock.TerminalState.IDLE;
        }
        return isSignalOn(mode, seconds, remaining)
                ? PaymentTerminalBlock.TerminalState.POWERING
                : PaymentTerminalBlock.TerminalState.ACTIVE;
    }

    private void setState(PaymentTerminalBlock.TerminalState next) {
        BlockState state = getBlockState();
        if (level == null
                || level.isClientSide()
                || !state.hasProperty(PaymentTerminalBlock.STATE)
                || state.getValue(PaymentTerminalBlock.STATE) == next) {
            return;
        }
        boolean wasPowering = state.getValue(PaymentTerminalBlock.STATE) == PaymentTerminalBlock.TerminalState.POWERING;
        boolean nowPowering = next == PaymentTerminalBlock.TerminalState.POWERING;
        level.setBlock(getBlockPos(), state.setValue(PaymentTerminalBlock.STATE, next), Block.UPDATE_ALL);
        if (wasPowering != nowPowering) {
            PaymentTerminalBlock.notifyNeighbours(level, getBlockPos(), state.getBlock());
            level.playSound(
                    null,
                    getBlockPos(),
                    SoundEvents.COMPARATOR_CLICK,
                    SoundSource.BLOCKS,
                    0.3f,
                    nowPowering ? 0.6f : 0.5f);
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString("TerminalName", terminalName);
        tag.putLong("PriceCents", priceCents);
        tag.putString("SignalMode", signalMode.name());
        tag.putInt("SignalSeconds", signalSeconds);
        if (!clientPacket) {
            tag.putInt("SignalTicksRemaining", signalTicksRemaining);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        terminalName = tag.getString("TerminalName");
        priceCents = Math.max(0, tag.getLong("PriceCents"));
        signalMode = readMode(tag.getString("SignalMode"));
        signalSeconds =
                tag.contains("SignalSeconds") ? Math.max(1, tag.getInt("SignalSeconds")) : DEFAULT_SIGNAL_SECONDS;
        if (!clientPacket) {
            signalTicksRemaining = Math.max(0, tag.getInt("SignalTicksRemaining"));
        }
    }

    private static SignalMode readMode(String name) {
        try {
            return SignalMode.valueOf(name);
        } catch (IllegalArgumentException e) {
            return DEFAULT_SIGNAL_MODE;
        }
    }
}
