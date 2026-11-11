package com.timder.kontor.game.ui;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SimpleBinding;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Selector;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.block.PaymentTerminalBlockEntity;
import com.timder.kontor.game.network.ToastPayload;
import com.timder.kontor.game.ui.element.*;
import com.timder.kontor.util.ComponentFormatting;
import com.timder.kontor.util.ObservableValue;
import dev.vfyjxf.taffy.style.FlexDirection;
import java.util.List;
import java.util.Locale;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PaymentTerminalUi {

    public static ModularUI create(BlockUIMenuType.BlockUIHolder holder, PaymentTerminalBlockEntity be) {
        Company viewer = null; // Only on the server
        Economy economy = null; // Only on the server
        if (holder.player.level() instanceof ServerLevel level) {
            viewer = CompanySavedData.get(level.getServer())
                    .getRegistry()
                    .companyOf(holder.player.getUUID())
                    .orElse(null);
            economy = EconomySavedData.get(level.getServer()).getEconomy();
        }

        UIElement operatorView = operatorView(holder, be);
        UIElement payerView = payerView(holder, be);
        operatorView.setDisplay(false);

        ObservableValue<Boolean> isOperator = new ObservableValue<>(false);
        SimpleBinding<Boolean> isOperatorBinding = DataBindingBuilder.boolS2C(() -> isOperator(holder.player, be))
                .onRemoteSyncReceived(isOperator::set)
                .build();
        isOperator.addListener(() -> {
            operatorView.setDisplay(isOperator.get());
            payerView.setDisplay(!isOperator.get());
        });

        ScrollerView root = UiContainer.smallScroller();
        root.addSyncValue(isOperatorBinding.getSyncValue());
        root.addScrollViewChildren(operatorView, payerView);

        UIElement withHud = UiContainer.withHud(root, UiContainer.hudBox(viewer, economy));
        return new ModularUI(UiStyles.ui(withHud), holder.player);
    }

    private static UIElement operatorView(BlockUIMenuType.BlockUIHolder holder, PaymentTerminalBlockEntity be) {
        UIElement content =
                new UIElement().layout(layout -> layout.widthPercent(100).gapAll(4));
        content.addChild(
                UiLabels.h1(Component.translatable("ui.createkontor.payment_terminal.configure"), Horizontal.CENTER));

        // TODO: name, price, pulse mode, length, open free

        UiTextFields.LabeledTextField name = UiTextFields.labeled(
                Component.translatable("ui.createkontor.payment_terminal.name"),
                Component.translatable("ui.createkontor.payment_terminal.name"),
                Component.translatable("ui.createkontor.payment_terminal.name.tooltip"),
                be.getTerminalName(),
                text -> {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("Name", text);
                    content.sendMessage("c2s_set_name", tag);
                });
        name.addSyncValue(DataBindingBuilder.stringS2C(be::getTerminalName)
                .onRemoteSyncReceived(value -> name.getTextField().setText(value, false))
                .build()
                .getSyncValue());
        content.addChild(name);
        content.onMessage("c2s_set_name", tag -> {
            if (!isOperator(holder.player, be)) {
                return;
            }
            be.setTerminalName(tag.getString("Name"));
        });

        UiTextFields.LabeledTextField price = UiTextFields.labeled(
                Component.translatable("ui.createkontor.payment_terminal.price"),
                Component.translatable("ui.createkontor.payment_terminal.price"),
                Component.translatable("ui.createkontor.payment_terminal.price.tooltip"),
                String.format(Locale.ROOT, "%.2f", be.getPriceCents() / 100.0),
                text -> {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("Price", text);
                    content.sendMessage("c2s_set_price", tag);
                });
        price.getTextField().setTextValidator(text -> text.matches("\\d{0,10}(\\.\\d{0,2})?"));
        price.addSyncValue(
                DataBindingBuilder.stringS2C(() -> String.format(Locale.ROOT, "%.2f", be.getPriceCents() / 100.0))
                        .onRemoteSyncReceived(value -> price.getTextField().setText(value, false))
                        .build()
                        .getSyncValue());
        content.addChild(price);
        content.onMessage("c2s_set_price", tag -> {
            if (!isOperator(holder.player, be)) {
                return;
            }
            try {
                be.setPriceCents(Money.fromDollar(Double.parseDouble(tag.getString("Price")))
                        .cents());
            } catch (NumberFormatException e) {
                // Empty text
            }
        });

        Label lengthLabel = UiLabels.primary(
                Component.translatable("ui.createkontor.payment_terminal.length."
                        + be.getSignalMode().name().toLowerCase(Locale.ROOT)),
                Horizontal.LEFT);

        Selector<PaymentTerminalBlockEntity.SignalMode> modeSelector = new Selector<>();
        modeSelector.setCandidateUIProvider(UIElementProvider.text(mode -> mode == null
                ? Component.literal("-")
                : Component.translatable("enum.createkontor.payment_terminal.signal_mode."
                        + mode.name().toLowerCase())));
        modeSelector.setCandidates(List.of(PaymentTerminalBlockEntity.SignalMode.values()));
        modeSelector.setSelected(be.getSignalMode(), false);
        modeSelector.layout(layout -> layout.width(140));
        modeSelector.setOnValueChanged(mode -> {
            if (mode == null) {
                return;
            }
            lengthLabel.setText(Component.translatable(
                    "ui.createkontor.payment_terminal.length." + mode.name().toLowerCase(Locale.ROOT)));
            CompoundTag tag = new CompoundTag();
            tag.putString("Mode", mode.name());
            content.sendMessage("c2s_set_mode", tag);
        });
        content.addChild(new UIElement()
                .layout(layout -> layout.flexDirection(FlexDirection.COLUMN).gapAll(2))
                .addChildren(
                        UiLabels.primary(
                                Component.translatable("ui.createkontor.payment_terminal.signal_mode"),
                                Horizontal.LEFT),
                        modeSelector));
        content.onMessage("c2s_set_mode", tag -> {
            if (!isOperator(holder.player, be)) {
                return;
            }
            try {
                be.setSignalMode(PaymentTerminalBlockEntity.SignalMode.valueOf(tag.getString("Mode")));
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        });

        int minSeconds = CompanyConfig.PAYMENT_TERMINAL_MIN_SIGNAL_SECONDS.get();
        int maxSeconds = CompanyConfig.PAYMENT_TERMINAL_MAX_SIGNAL_SECONDS.get();
        UIElement lengthSlider = UiSliders.labeledInline(
                minSeconds,
                Math.max(minSeconds + 1, maxSeconds),
                be.getSignalSeconds(),
                1f,
                0,
                false,
                400,
                value -> Component.translatable("ui.createkontor.payment_terminal.seconds", Math.round(value)),
                value -> {},
                value -> {
                    CompoundTag tag = new CompoundTag();
                    tag.putInt("Seconds", Math.round(value));
                    content.sendMessage("c2s_set_length", tag);
                });
        content.addChild(new UIElement()
                .layout(layout ->
                        layout.flexDirection(FlexDirection.COLUMN).gapAll(2).widthPercent(100))
                .addChildren(lengthLabel, lengthSlider));
        content.onMessage("c2s_set_length", tag -> {
            if (isOperator(holder.player, be)) {
                be.setSignalSeconds(tag.getInt("Seconds"));
            }
        });

        content.addChild(UiButtons.secondary(Component.translatable("ui.createkontor.payment_terminal.activate_free"))
                .setOnServerClick(e -> {
                    if (isOperator(holder.player, be)) {
                        ToastPayload.send(holder.player, be.activateFree((ServerPlayer) holder.player));
                    }
                }));
        return content;
    }

    private static UIElement payerView(BlockUIMenuType.BlockUIHolder holder, PaymentTerminalBlockEntity be) {
        UIElement content =
                new UIElement().layout(layout -> layout.widthPercent(100).gapAll(4));

        Label title = UiLabels.h1(
                be.getTerminalName().isEmpty()
                        ? Component.translatable("block.createkontor.payment_terminal")
                        : Component.literal(be.getTerminalName()),
                Horizontal.CENTER);
        title.textStyle(style -> style.adaptiveWidth(false));
        title.layout(layout -> layout.widthPercent(100));
        title.addSyncValue(
                DataBindingBuilder.componentS2C(() -> be.getTerminalName().isEmpty()
                                ? Component.translatable("block.createkontor.payment_terminal")
                                : Component.literal(be.getTerminalName()))
                        .onRemoteSyncReceived(title::setText)
                        .build()
                        .getSyncValue());
        content.addChild(title);

        Label owner = UiLabels.primary(ComponentFormatting.error("Unknown"), Horizontal.RIGHT);
        owner.addSyncValue(DataBindingBuilder.componentS2C(() -> CompanySavedData.get(holder.player.getServer())
                        .getRegistry()
                        .get(be.getCompanyId())
                        .map(company -> ComponentFormatting.highlightStandard(company.name()))
                        .orElseGet(() -> ComponentFormatting.error("Unknown")))
                .onRemoteSyncReceived(owner::setText)
                .build()
                .getSyncValue());
        content.addChild(UiLabels.labelValue(Component.translatable("ui.createkontor.payment_terminal.owner"), owner));

        ObservableValue<Long> price = new ObservableValue<>(be.getPriceCents());
        Label priceLabel = UiLabels.primary(
                ComponentFormatting.moneyNegative(Money.ofCents(be.getPriceCents())), Horizontal.RIGHT);
        priceLabel.addSyncValue(DataBindingBuilder.longValS2C(be::getPriceCents)
                .onRemoteSyncReceived(price::set)
                .build()
                .getSyncValue());
        price.addListener(
                () -> priceLabel.setText(ComponentFormatting.moneyNegative(Money.ofCents(be.getPriceCents()))));
        content.addChild(UiLabels.labelValue(
                Component.translatable("ui.createkontor.payment_terminal.price_label"), priceLabel));

        Button pay = UiButtons.primary(Component.translatable("ui.createkontor.payment_terminal.pay"));
        pay.setActive(false);
        pay.addSyncValue(DataBindingBuilder.boolS2C(() -> be.isPriceSet() && !be.isCycleRunning())
                .onRemoteSyncReceived(pay::setActive)
                .build()
                .getSyncValue());
        pay.setOnClick(e -> {
            CompoundTag tag = new CompoundTag();
            tag.putLong("PriceCents", price.get());
            content.sendMessage("c2s_pay", tag);
        });
        content.onMessage(
                "c2s_pay",
                tag -> ToastPayload.send(
                        holder.player, be.pay((ServerPlayer) holder.player, tag.getLong("PriceCents"))));
        content.addChild(pay);

        return content;
    }

    private static boolean isOperator(Player player, PaymentTerminalBlockEntity be) {
        if (!(player.level() instanceof ServerLevel level) || be.getCompanyId() == null) {
            return false;
        }
        return CompanySavedData.get(level.getServer())
                .getRegistry()
                .companyOf(player.getUUID())
                .map(company -> company.id().equals(be.getCompanyId()))
                .orElse(false);
    }
}
