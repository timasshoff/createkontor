package com.timder.kontor.game.ui;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SimpleBinding;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.SearchComponent;
import com.lowdragmc.lowdraglib2.gui.util.UISoundUtils;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.core.company.financial.*;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.PlayerNotifications;
import com.timder.kontor.game.network.S2CActionResult;
import com.timder.kontor.game.network.ToastPayload;
import com.timder.kontor.game.ui.element.*;
import com.timder.kontor.util.ComponentFormatting;
import com.timder.kontor.util.ObservableList;
import dev.vfyjxf.taffy.style.FlexDirection;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class CheckbookUi {

    public static ModularUI create(Player player) {
        if (player.level().isClientSide()) {
            UISoundUtils.playSound(SoundEvents.BOOK_PAGE_TURN);
        }

        UIElement content = new UIElement()
                .layout(layout -> layout.width(200)
                        .minHeight(80)
                        .flexDirection(FlexDirection.COLUMN)
                        .paddingLeft(13)
                        .paddingTop(11)
                        .paddingRight(14)
                        .paddingBottom(13)
                        .gapAll(4))
                .style(UiStyles.CHECKBOOK);

        Company viewer = null;
        Economy economy = null;
        if (player.level() instanceof ServerLevel level) {
            viewer = CompanySavedData.get(level.getServer())
                    .getRegistry()
                    .companyOf(player.getUUID())
                    .orElse(null);
            economy = EconomySavedData.get(level.getServer()).getEconomy();
        }

        Label companyLabel = UiPaper.paragraphPrimary(Component.empty(), Horizontal.LEFT);
        companyLabel.addSyncValue(DataBindingBuilder.stringS2C(() -> CompanySavedData.get(player.getServer())
                        .getRegistry()
                        .companyOf(player.getUUID())
                        .map(Company::name)
                        .orElse("Unknown"))
                .onRemoteSyncReceived(
                        s -> companyLabel.setText(Component.translatable("ui.createkontor.checkbook.company", s)))
                .build()
                .getSyncValue());
        content.addChild(companyLabel);
        content.addChild(UiPaper.doubleDivider());

        Button sign = UiButtons.primary(Component.translatable("ui.createkontor.checkbook.sign"));
        sign.setActive(false);

        CompanyEntry[] selected = {null};
        boolean[] amountPositive = {false};
        Runnable refreshSign = () -> sign.setActive(selected[0] != null && amountPositive[0]);

        ObservableList<CompanyEntry> companies = new ObservableList<>();
        SimpleBinding<Tag> companiesBinding = DataBindingBuilder.tagS2C(() -> companiesToTag(player))
                .onRemoteSyncReceived(tag -> companies.set(tagToCompanies(tag)))
                .build();
        content.addSyncValue(companiesBinding.getSyncValue());

        SearchComponent<CompanyEntry> search = new SearchComponent<>();
        search.setSearchUI(new SearchComponent.ISearchUI<>() {
            @Override
            public String resultText(CompanyEntry entry) {
                return entry.name();
            }

            @Override
            public void onResultSelected(@Nullable CompanyEntry value) {}

            @Override
            public void search(String word, IResultHandler<CompanyEntry> handler) {
                String needle = word.trim().toLowerCase(Locale.ROOT);
                for (CompanyEntry entry : companies.get()) {
                    if (Thread.currentThread().isInterrupted()) return;
                    if (needle.isEmpty()
                            || entry.name().toLowerCase(Locale.ROOT).contains(needle)) {
                        handler.acceptResult(entry);
                    }
                }
            }
        });
        search.setCandidateUIProvider(
                entry -> UiLabels.paragraphPrimary(Component.literal(entry.name()), Horizontal.LEFT)
                        .layout(layout -> layout.marginAll(0).marginLeft(2)));
        search.layout(layout -> layout.widthPercent(100));
        search.searchStyle(style -> style.maxItemCount(8).scrollerViewHeight(148));
        search.setOnValueChanged(entry -> {
            selected[0] = entry;
            refreshSign.run();
        });

        UIElement payTo = new UIElement()
                .layout(layout -> layout.flexDirection(FlexDirection.COLUMN).gapAll(2))
                .addChildren(
                        UiPaper.primary(Component.translatable("ui.createkontor.checkbook.pay_to"), Horizontal.LEFT),
                        search);
        payTo.layout(layout -> layout.flexBasis(0).flexGrow(2).minWidth(0));

        UiTextFields.LabeledTextField amount = UiTextFields.labeled(
                UiPaper.primary(Component.translatable("ui.createkontor.checkbook.amount"), Horizontal.LEFT),
                Component.translatable("ui.createkontor.checkbook.amount_short"),
                null,
                "0.00",
                text -> {});
        amount.layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1).minWidth(0));
        amount.getTextField().setTextValidator(text -> {
            if (!text.matches("\\d{0,10}(\\.\\d{0,2})?")) return false;

            boolean positive;
            try {
                positive = Double.parseDouble(text) > 0;
            } catch (NumberFormatException e) {
                positive = false;
            }
            amountPositive[0] = positive;
            refreshSign.run();
            return true;
        });

        UIElement row = new UIElement()
                .layout(layout ->
                        layout.flexDirection(FlexDirection.ROW).gapAll(4).widthPercent(100));
        row.addChildren(payTo, amount);
        content.addChild(row);
        content.addChild(UiPaper.doubleDivider());

        sign.setOnClick(e -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("Amount", amount.getTextField().getValue());
            tag.putInt("ReceiverId", selected[0].id());
            content.sendMessage("c2s_sign", tag);
        });
        content.onMessage("c2s_sign", tag -> {
            Optional<S2CActionResult> result = sign(player, tag);
            ToastPayload.send(player, result);
            if (result.isPresent()
                    && result.get().kind() == ToastPayload.ToastKind.SUCCESS
                    && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.playNotifySound(
                        SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1.0f, 1.0f);
                serverPlayer.closeContainer();
            }
        });
        content.addChild(sign);

        UIElement withHud = UiContainer.withHud(content, UiContainer.hudBox(viewer, economy));
        return new ModularUI(UiStyles.ui(withHud), player);
    }

    private static Tag companiesToTag(Player player) {
        ListTag list = new ListTag();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return list;
        }
        CompanyRegistry registry =
                CompanySavedData.get(serverPlayer.getServer()).getRegistry();
        CompanyId own =
                registry.companyOf(serverPlayer.getUUID()).map(Company::id).orElse(null);
        for (Company company : registry.all()) {
            if (company.id().equals(own)) continue;
            CompoundTag entry = new CompoundTag();
            entry.putInt("Id", company.id().value());
            entry.putString("Name", company.name());
            list.add(entry);
        }
        return list;
    }

    private record CompanyEntry(int id, String name) {}

    private static List<CompanyEntry> tagToCompanies(Tag tag) {
        List<CompanyEntry> entries = new ArrayList<>();
        if (!(tag instanceof ListTag list)) return entries;
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            entries.add(new CompanyEntry(entry.getInt("Id"), entry.getString("Name")));
        }
        entries.sort(Comparator.comparing(CompanyEntry::name, String.CASE_INSENSITIVE_ORDER));
        return entries;
    }

    private static Optional<S2CActionResult> sign(Player player, CompoundTag tag) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return S2CActionResult.illegalEnvironment();
        }
        MinecraftServer server = serverPlayer.getServer();
        CompanySavedData companyData = CompanySavedData.get(server);
        CompanyRegistry registry = companyData.getRegistry();

        Company payer = registry.companyOf(serverPlayer.getUUID()).orElse(null);
        if (payer == null) {
            return S2CActionResult.error(Component.literal("You are not in a company."));
        }

        int receiverId = tag.getInt("ReceiverId");
        Company receiver =
                receiverId < 1 ? null : registry.get(new CompanyId(receiverId)).orElse(null);
        if (receiver == null) {
            return S2CActionResult.error(Component.translatable("message.createkontor.checkbook.company_invalid"));
        }

        Money amount;
        String amountText = tag.getString("Amount");
        if (!amountText.matches("\\d{1,10}(\\.\\d{1,2})?")) {
            return S2CActionResult.error(Component.translatable("message.createkontor.checkbook.amount_invalid"));
        }
        try {
            amount = Money.fromDollar(Double.parseDouble(amountText));
        } catch (NumberFormatException | ArithmeticException e) {
            return S2CActionResult.error(Component.translatable("message.createkontor.checkbook.amount_invalid"));
        }

        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        TransferStatus status = TransferRules.preview(payer, receiver, amount, params);
        if (status != TransferStatus.OK) {
            return S2CActionResult.error(Component.translatable(
                    "enum.createkontor.transfer_status." + status.name().toLowerCase(Locale.ROOT)));
        }

        TransferBookings bookings = new TransferBookings(
                BookingKind.CHECK_PAYMENT, receiver.name(), BookingKind.CHECK_REVENUE, payer.name());
        try {
            long day = EconomySavedData.get(server).getEconomy().currentDay();
            TransferRules.transfer(payer, receiver, amount, bookings, day, params);
        } catch (IllegalStateException e) {
            return S2CActionResult.error(ComponentFormatting.error("Failed: " + e.getMessage()));
        }

        companyData.setDirty();
        PlayerNotifications.sendTransferReceivedNotification(server, payer, receiver, amount);
        return S2CActionResult.success(Component.translatable(
                "message.createkontor.checkbook.signed",
                ComponentFormatting.moneyNegative(amount),
                ComponentFormatting.highlightStandard(receiver.name())));
    }
}
