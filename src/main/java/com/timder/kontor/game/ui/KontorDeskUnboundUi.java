package com.timder.kontor.game.ui;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.game.block.KontorDeskBlockEntity;
import com.timder.kontor.game.network.ToastPayload;
import com.timder.kontor.game.ui.element.UiButtons;
import com.timder.kontor.game.ui.element.UiContainer;
import com.timder.kontor.game.ui.element.UiLabels;
import com.timder.kontor.util.ComponentFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class KontorDeskUnboundUi {

    public static ModularUI create(BlockUIMenuType.BlockUIHolder holder, KontorDeskBlockEntity be) {
        ScrollerView root = UiContainer.smallScroller();

        String[] name = { "" };
        boolean[] takeLoan = { false };
        Button button = UiButtons.primary(Component.translatable("ui.createkontor.kontor_desk.founding"))
                .setOnServerClick(e -> ToastPayload.send(holder.player, be.foundNewCompany((ServerPlayer) holder.player, name[0], takeLoan[0])));
        button.setActive(false);

        root.addScrollViewChildren(UiLabels.h1(Component.translatable("ui.createkontor.kontor_desk.founding_title"), Horizontal.CENTER));

        TextField nameField = new TextField();
        nameField.textFieldStyle(style -> style.placeholder(Component.translatable("ui.createkontor.kontor_desk.company_name")));
        nameField.setTextValidator(s -> {
            boolean valid;
            try {
                Company.requireValidName(s);
                valid = true;
            } catch (IllegalArgumentException e) {
                valid = false;
            }
            button.setActive(valid);
            return valid;
        });
        var nameBinding = DataBindingBuilder.stringC2S(v -> name[0] = v) // Sync client name field value to server string array
                .remoteGetter(nameField::getValue)
                .build();
        nameField.addSyncValue(nameBinding.getSyncValue());
        root.addScrollViewChildren(nameField);

        root.addScrollViewChildren(UiLabels.paragraphSecondary(Component.translatable(
                "ui.createkontor.kontor_desk.business_fee.info",
                ComponentFormatting.moneyNegative(Money.ofDollars(CompanyConfig.BUSINESS_LICENSE_FEE_IN_DOLLARS.get()))
        ), Horizontal.LEFT));

        root.addScrollViewChildren(UiLabels.paragraphSecondary(Component.translatable(
                "ui.createkontor.kontor_desk.deposit.info",
                ComponentFormatting.moneyColored(Money.ofDollars(CompanyConfig.START_DEPOSIT_IN_DOLLARS.get()))
        ), Horizontal.LEFT));

        root.addScrollViewChildren(UiLabels.paragraphSecondary(Component.translatable(
                        "ui.createkontor.kontor_desk.founders_loan.info",
                        ComponentFormatting.moneyColored(Money.ofDollars(CompanyConfig.FOUNDER_LOAN_IN_DOLLARS.get())),
                        ComponentFormatting.highlightStandard(CompanyConfig.FOUNDER_LOAN_FREE_DAYS.get().toString())
        ), Horizontal.LEFT));

        Toggle loanToggle = new Toggle();
        loanToggle.toggleLabel.setText(Component.literal("Take founders loan"));
        var loanBinding = DataBindingBuilder.boolC2S(v -> takeLoan[0] = v) // Sync client toggle value to server bool array
                .remoteGetter(loanToggle::getValue)
                .build();
        nameField.addSyncValue(loanBinding.getSyncValue());
        root.addScrollViewChildren(loanToggle);

        root.addScrollViewChildren(button);

        return new ModularUI(UI.of(root, StylesheetManager.GDP), holder.player);
    }
}
