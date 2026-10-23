package com.timder.kontor.game.ui;

import static com.timder.kontor.game.ui.KontorDeskBoundUi.loansToTag;
import static com.timder.kontor.game.ui.KontorDeskBoundUi.tagToLoans;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SimpleBinding;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.*;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.macro.MacroHistoryEntry;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.block.FinancesManagerActions;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.network.ToastPayload;
import com.timder.kontor.game.ui.chart.EconomyCharts;
import com.timder.kontor.game.ui.chart.classic.ChartHost;
import com.timder.kontor.game.ui.element.UiButtons;
import com.timder.kontor.game.ui.element.UiContainer;
import com.timder.kontor.game.ui.element.UiLabels;
import com.timder.kontor.game.ui.element.UiSliders;
import com.timder.kontor.util.ComponentFormatting;
import com.timder.kontor.util.ObservableList;
import com.timder.kontor.util.ObservableValue;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public class FinancesManagerUi {
    public static ModularUI create(EmployeeDeskContext context, FinancesManagerActions actions) {
        var tabView = UiContainer.largeTabView();
        tabView.addTab(
                UiContainer.tab(Component.translatable("ui.createkontor.finances_manager.take_loan")),
                newLoanTab(context, actions));
        tabView.addTab(
                UiContainer.tab(Component.translatable("ui.createkontor.finances_manager.open_loans_tab")),
                loansTab(context));
        tabView.addTab(
                UiContainer.tab(Component.translatable("ui.createkontor.finances_manager.policy_rate_tab")),
                policyRateTab(context));

        var root = UiContainer.withHud(tabView, UiContainer.hudBox(context.company(), context.economy()));
        return new ModularUI(UI.of(root, StylesheetManager.GDP), context.player());
    }

    public static UIElement newLoanTab(EmployeeDeskContext context, FinancesManagerActions actions) {
        ObservableValue<CreditInfo> info = new ObservableValue<>(new CreditInfo(false, false, 0, 0, 0, 0, 0, 0));
        SimpleBinding<Tag> infoBinding = DataBindingBuilder.tagS2C(() -> creditInfoToTag(context))
                .onRemoteSyncReceived(tag -> info.set(tagToCreditInfo(tag)))
                .build();

        UIElement content =
                new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));
        content.addSyncValue(infoBinding.getSyncValue());

        UIElement notAllowed = UiContainer.headerWithSubtitle(
                UiLabels.h2(
                        Component.translatable("ui.createkontor.finances_manager.bank_loan_not_allowed"),
                        Horizontal.CENTER),
                UiLabels.error(
                        Component.translatable("ui.createkontor.finances_manager.legal_level_too_low")
                                .withStyle(ChatFormatting.DARK_RED),
                        Horizontal.CENTER));
        ScrollerView allowedContent = (ScrollerView) UiContainer.tabScroller().layout(layout -> layout.gapAll(4));

        content.addChildren(allowedContent, notAllowed);
        info.addListener(() -> {
            boolean allowed = info.get().allowed;
            notAllowed.setDisplay(!allowed);
            allowedContent.setDisplay(allowed);
        });

        double[] selectedDollars = {0};
        content.onMessage("c2s_set_borrow_amount", tag -> selectedDollars[0] = tag.getDouble("Dollars"));

        ObservableList<OfferView> offers = new ObservableList<>();
        SimpleBinding<Tag> offersBinding = DataBindingBuilder.tagS2C(() -> offersToTag(context, selectedDollars[0]))
                .onRemoteSyncReceived(tag -> offers.set(tagToOffers(tag).stream()
                        .sorted(Comparator.comparingInt(OfferView::termDays)
                                .thenComparingInt(OfferView::freeDays)
                                .thenComparing(OfferView::id))
                        .toList()))
                .build();
        content.addSyncValue(offersBinding.getSyncValue());

        UIElement offersGrid = UiContainer.grid(
                offers,
                (OfferView offer) -> {
                    UIElement card = new UIElement()
                            .style(style -> style.background(Sprites.BORDER_DARK))
                            .layout(layout -> layout.paddingAll(8).gapAll(2));

                    card.addChild(UiLabels.paragraphPrimary(
                            Component.literal(offer.id).withStyle(ChatFormatting.DARK_GRAY), Horizontal.LEFT));
                    card.addChild(UiLabels.paragraphPrimary(
                            Component.translatable("loan_offers.createkontor." + offer.id)
                                    .withStyle(ChatFormatting.GOLD),
                            Horizontal.LEFT));

                    card.addChild(UiLabels.labelValueNoWrap(
                            Component.translatable("ui.createkontor.finances_manager.offer.rate"),
                            Component.literal(String.format(Locale.ROOT, "%.4f %%", offer.rate() * 100))));
                    card.addChild(UiLabels.labelValueNoWrap(
                            Component.translatable("ui.createkontor.finances_manager.offer.free_days"),
                            ComponentFormatting.lengthInDays(offer.freeDays())));
                    card.addChild(UiLabels.labelValueNoWrap(
                            Component.translatable("ui.createkontor.finances_manager.offer.repayment_days"),
                            ComponentFormatting.lengthInDays(offer.termDays())));
                    card.addChild(UiLabels.labelValueNoWrap(
                            Component.translatable("ui.createkontor.finances_manager.offer.total_days"),
                            ComponentFormatting.lengthInDays(offer.totalDays())));
                    card.addChild(UiLabels.labelValueNoWrap(
                            Component.translatable("ui.createkontor.finances_manager.offer.first_payment"),
                            ComponentFormatting.moneyColored(Money.ofCents(offer.firstPaymentCents()))));
                    card.addChild(UiLabels.labelValueNoWrap(
                            Component.translatable("ui.createkontor.finances_manager.offer.total_interest"),
                            ComponentFormatting.moneyColored(Money.ofCents(offer.totalInterestCents()))));

                    Button button = UiButtons.primary(
                                    Component.translatable("ui.createkontor.finances_manager.offer.accept_offer"))
                            .setOnClick((e) -> {
                                CompoundTag tag = new CompoundTag();
                                tag.putString("Id", offer.id());
                                content.sendMessage("c2s_accept_offer", tag);
                            });
                    button.layout(layout -> layout.heightAuto()
                            .minHeight(20)
                            .marginTopAuto()
                            .alignItems(AlignItems.CENTER)
                            .paddingVertical(4));
                    button.textStyle(style ->
                            style.adaptiveWidth(false).textWrap(TextWrap.WRAP).adaptiveHeight(true));
                    button.text.layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1));
                    card.addChild(button);

                    return card;
                },
                Component.translatable("ui.createkontor.finances_manager.no_offers"),
                3,
                140);

        content.onMessage(
                "c2s_accept_offer",
                tag -> ToastPayload.send(context.player(), actions.takeLoan(tag.getString("Id"), selectedDollars[0])));

        info.addListener(() -> {
            CreditInfo i = info.get();
            if (!i.allowed()) {
                return;
            }

            allowedContent.clearAllChildren();
            allowedContent.addChild(UiLabels.h1(
                    Component.translatable("ui.createkontor.finances_manager.take_loan_title"), Horizontal.LEFT));

            Money averageRevenue = Money.ofCents(i.averageRevenueCents());
            Money min = Money.ofCents(i.minCents());
            Money max = Money.ofCents(i.maxCents);

            Label averageRevenueLabel = UiLabels.secondary(
                    Component.translatable(
                            "ui.createkontor.finances_manager.average_revenue", averageRevenue.toString()),
                    Horizontal.LEFT);
            Label reputation = UiLabels.secondary(
                    Component.translatable(
                            "ui.createkontor.finances_manager.reputation",
                            ComponentFormatting.starsWithValue(i.reputation())),
                    Horizontal.LEFT);
            Label policyRate = UiLabels.secondary(
                    Component.translatable(
                            "ui.createkontor.finances_manager.policy_rate",
                            Component.literal(String.format(Locale.ROOT, "%.4f %%", i.policyRate() * 100))
                                    .withStyle(ChatFormatting.WHITE)),
                    Horizontal.LEFT);
            Label legalFormCreditLimit = UiLabels.secondary(
                    Component.translatable(
                            "ui.createkontor.finances_manager.legal_form_credit_limit",
                            Money.ofCents(i.legalFormLimitCents()).toString()),
                    Horizontal.LEFT);

            allowedContent.addChild(UiLabels.seperatedLabelRow(
                            List.of(averageRevenueLabel, reputation, policyRate, legalFormCreditLimit))
                    .layout(layout -> layout.justifyContent(AlignContent.FLEX_START)));

            if (max.cents() < min.cents() || !i.available()) {
                allowedContent.addChild(UiLabels.paragraphError(
                        Component.translatable("ui.createkontor.finances_manager.no_credit"), Horizontal.LEFT));
                return;
            }

            allowedContent.addChild(UiSliders.labeledInlineLeft(
                    (float) min.toDollars(),
                    (float) max.toDollars(),
                    (float) selectedDollars[0],
                    1f,
                    0,
                    false,
                    400,
                    value -> ComponentFormatting.moneyColored(Money.fromDollar((double) value)),
                    value -> {},
                    value -> {
                        selectedDollars[0] = Math.round(value);
                        CompoundTag tag = new CompoundTag();
                        tag.putDouble("Dollars", selectedDollars[0]);
                        content.sendMessage("c2s_set_borrow_amount", tag);
                    }));

            allowedContent.addChild(offersGrid);
        });

        info.runListenersOnce();

        return content;
    }

    public static UIElement loansTab(EmployeeDeskContext context) {
        ScrollerView content = UiContainer.tabScroller();

        ObservableList<Loan> loans = new ObservableList<>();
        SimpleBinding<Tag> loansBinding = DataBindingBuilder.tagS2C(
                        () -> loansToTag(context.company().loans()))
                .onRemoteSyncReceived(tag -> loans.set(tagToLoans(tag)))
                .build();

        Label title =
                UiLabels.h1(Component.translatable("ui.createkontor.kontor_desk.tab.open_loans"), Horizontal.LEFT);
        loans.addListener(() -> title.setText(Component.translatable(
                "ui.createkontor.kontor_desk.loans_count", loans.get().size())));
        title.addSyncValue(loansBinding.getSyncValue());
        content.addScrollViewChildren(title);

        Label totalVolume = UiLabels.secondary(
                Component.translatable("ui.createkontor.finances_manager.loans_total", "-"), Horizontal.CENTER);
        Label outstandingVolume = UiLabels.secondary(
                Component.translatable("ui.createkontor.finances_manager.loans_outstanding", "-"), Horizontal.CENTER);
        content.addScrollViewChildren(UiLabels.seperatedLabelRow(List.of(totalVolume, outstandingVolume))
                .layout(layout -> layout.justifyContent(AlignContent.FLEX_START)));

        UIElement loanBox = new UIElement().addSyncValue(loansBinding.getSyncValue());
        loans.addListener(() -> {
            Money total = Money.ZERO;
            Money outstanding = Money.ZERO;
            for (Loan loan : loans.get()) {
                if (loan.isRepaid()) continue;
                total = total.plus(loan.principal());
                outstanding = outstanding.plus(loan.outstanding());
            }
            totalVolume.setText(Component.translatable(
                    "ui.createkontor.finances_manager.loans_total",
                    Component.literal(total.toString()).withStyle(ChatFormatting.WHITE)));
            outstandingVolume.setText(Component.translatable(
                    "ui.createkontor.finances_manager.loans_outstanding",
                    Component.literal(outstanding.toString()).withStyle(ChatFormatting.WHITE)));

            loanBox.clearAllChildren();
            for (Loan loan : loans.get()) {
                if (loan.isRepaid()) continue;

                UIElement loanElement = new UIElement()
                        .style(style -> style.background(Sprites.BORDER_DARK))
                        .layout(layout -> layout.paddingAll(8));

                loanElement.addChild(UiLabels.primary(
                        Component.translatable(
                                "ui.createkontor.kontor_desk.loan.principal",
                                Component.translatable("loan.createkontor."
                                        + loan.kind().toString().toLowerCase()),
                                ComponentFormatting.moneyColored(loan.principal())),
                        Horizontal.LEFT));

                loanElement.addChild(UiLabels.seperatedLabelRow(List.of(
                                UiLabels.secondary(
                                        Component.translatable(
                                                "ui.createkontor.finances_manager.loan_days",
                                                ComponentFormatting.highlightStandard(
                                                        String.valueOf(loan.elapsedDays())),
                                                ComponentFormatting.highlightStandard(
                                                        String.valueOf(loan.freeDays() + loan.termDays()))),
                                        Horizontal.CENTER),
                                UiLabels.secondary(
                                        Component.translatable(
                                                "ui.createkontor.kontor_desk.loan.outstanding",
                                                loan.outstanding().toString()),
                                        Horizontal.CENTER),
                                UiLabels.secondary(
                                        Component.translatable(
                                                "ui.createkontor.kontor_desk.loan.rate",
                                                String.format(Locale.ROOT, "%.4f%%", loan.rate() * 100)),
                                        Horizontal.CENTER),
                                UiLabels.secondary(
                                        Component.translatable(
                                                "ui.createkontor.kontor_desk.loan.repayment",
                                                loan.repayment().toString()),
                                        Horizontal.CENTER),
                                UiLabels.secondary(
                                        Component.translatable(
                                                "ui.createkontor.kontor_desk.loan.term_days", loan.termDays()),
                                        Horizontal.CENTER),
                                UiLabels.secondary(
                                        Component.translatable(
                                                "ui.createkontor.kontor_desk.loan.free_days", loan.freeDays()),
                                        Horizontal.CENTER)))
                        .layout(layout ->
                                layout.justifyContent(AlignContent.FLEX_START).gapAll(0)));

                loanBox.addChild(loanElement);
            }
        });

        content.addScrollViewChildren(loanBox);
        return content;
    }

    public static UIElement policyRateTab(EmployeeDeskContext context) {
        UIElement content = new UIElement()
                .layout(layout -> layout.widthPercent(100).heightPercent(100).gapAll(4));

        content.addChild(UiLabels.h1(
                Component.translatable("ui.createkontor.finances_manager.policy_rate_tab"), Horizontal.LEFT));

        Label currentPolicyRate = UiLabels.secondary(
                Component.translatable(
                        "ui.createkontor.finances_manager.current_policy_rate",
                        Component.literal("-").withStyle(ChatFormatting.WHITE)),
                Horizontal.LEFT);

        content.addChild(UiLabels.seperatedLabelRow(List.of(currentPolicyRate))
                .layout(layout -> layout.justifyContent(AlignContent.FLEX_START)));

        ChartHost policyRateChartHost = new ChartHost();
        policyRateChartHost.layout(
                layout -> layout.widthPercent(100).flexBasis(0).flexGrow(1).minHeight(0));
        policyRateChartHost.setDisplay(false);
        content.addChild(policyRateChartHost);

        SimpleBinding<Tag> historyBinding = DataBindingBuilder.tagS2C(
                        () -> macroHistoryToTag(context.economy().macroHistory()))
                .onRemoteSyncReceived(tag -> {
                    List<MacroHistoryEntry> history = tagToMacroHistory(tag);
                    policyRateChartHost.show(EconomyCharts.policyRate(history));
                    policyRateChartHost.setDisplay(!history.isEmpty());
                    currentPolicyRate.setText(Component.translatable(
                            "ui.createkontor.finances_manager.policy_rate",
                            Component.literal(String.format(
                                            Locale.ROOT,
                                            "%.4f %%",
                                            history.getLast().policyRate() * 100))
                                    .withStyle(ChatFormatting.WHITE)));
                })
                .build();
        content.addSyncValue(historyBinding.getSyncValue());

        return content;
    }

    private static Tag creditInfoToTag(EmployeeDeskContext context) {
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        Company company = context.company();

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Allowed", BankLoanRules.isAllowed(company, params));
        tag.putBoolean(
                "Available",
                BankLoanRules.isAllowed(company, params)
                        && BankLoanRules.bankLoanCount(company)
                                < params.bankLoans().maxLoans());
        tag.putLong(
                "AverageRevenueCents",
                company.averageDailyRevenue(params.bankLoans().creditLimitRevenueDays())
                        .cents());
        tag.putDouble("PolicyRate", context.economy().policyRate());
        tag.putLong("MinCents", BankLoanRules.minimumAmount(company, params).cents());
        tag.putLong("MaxCents", BankLoanRules.getCreditLimit(company, params).cents());
        tag.putLong(
                "LegalFormLimitCents", company.legalForm(params).bankLoanLimit().cents());
        tag.putDouble("Reputation", context.economy().overallReputationInStars(company.id()));
        return tag;
    }

    public record CreditInfo(
            boolean allowed,
            boolean available,
            long averageRevenueCents,
            double policyRate,
            long minCents,
            long maxCents,
            long legalFormLimitCents,
            double reputation) {}

    private static CreditInfo tagToCreditInfo(Tag tag) {
        CompoundTag t = (CompoundTag) tag;
        return new CreditInfo(
                t.getBoolean("Allowed"),
                t.getBoolean("Available"),
                t.getLong("AverageRevenueCents"),
                t.getDouble("PolicyRate"),
                t.getLong("MinCents"),
                t.getLong("MaxCents"),
                t.getLong("LegalFormLimitCents"),
                t.getDouble("Reputation"));
    }

    private static Tag offersToTag(EmployeeDeskContext context, double requestedDollars) {
        ListTag list = new ListTag();
        Company company = context.company();
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));

        Money amount = requestedDollars > 0
                ? Money.fromDollar(requestedDollars)
                : BankLoanRules.minimumAmount(company, params);

        try {
            for (BankLoanQuote q : BankLoanRules.quotes(
                    company,
                    amount,
                    context.economy().policyRate(),
                    context.economy().overallReputationInStars(company.id()),
                    params)) {
                CompoundTag t = new CompoundTag();
                t.putString("Id", q.offer().id());
                t.putDouble("Rate", q.rate());
                t.putLong("FirstPaymentCents", q.firstPayment().cents());
                t.putLong("TotalInterestCents", q.totalInterest().cents());
                t.putInt("TermDays", q.offer().termDays());
                t.putInt("FreeDays", q.offer().freeDays());
                t.putInt("TotalDays", q.totalDays());
                list.add(t);
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            // No offers
        }
        return list;
    }

    public record OfferView(
            String id,
            double rate,
            long firstPaymentCents,
            long totalInterestCents,
            int termDays,
            int freeDays,
            int totalDays) {}

    private static List<OfferView> tagToOffers(Tag tag) {
        ListTag list = (ListTag) tag;
        List<OfferView> offers = new ArrayList<>(list.size());
        for (Tag e : list) {
            CompoundTag t = (CompoundTag) e;
            offers.add(new OfferView(
                    t.getString("Id"),
                    t.getDouble("Rate"),
                    t.getLong("FirstPaymentCents"),
                    t.getLong("TotalInterestCents"),
                    t.getInt("TermDays"),
                    t.getInt("FreeDays"),
                    t.getInt("TotalDays")));
        }
        return offers;
    }

    public static Tag macroHistoryToTag(List<MacroHistoryEntry> history) {
        ListTag list = new ListTag();
        for (MacroHistoryEntry entry : history) {
            CompoundTag e = new CompoundTag();
            e.putLong("day", entry.day());
            e.putDouble("index", entry.index());
            e.putDouble("policyRate", entry.policyRate());
            list.add(e);
        }
        return list;
    }

    public static List<MacroHistoryEntry> tagToMacroHistory(Tag tag) {
        ListTag list = (ListTag) tag;
        List<MacroHistoryEntry> history = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            history.add(new MacroHistoryEntry(e.getLong("day"), e.getDouble("index"), e.getDouble("policyRate")));
        }
        return history;
    }
}
