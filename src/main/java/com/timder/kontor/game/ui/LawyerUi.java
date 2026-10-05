package com.timder.kontor.game.ui;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SimpleBinding;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.*;
import com.timder.kontor.core.company.license.*;
import com.timder.kontor.core.company.order.Order;
import com.timder.kontor.core.company.order.OrderPhase;
import com.timder.kontor.core.company.order.RequestOrigin;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.block.LawyerActions;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.network.ToastPayload;
import com.timder.kontor.game.ui.element.UiButtons;
import com.timder.kontor.game.ui.element.UiContainer;
import com.timder.kontor.game.ui.element.UiLabels;
import com.timder.kontor.util.ComponentFormatting;
import com.timder.kontor.util.LicenseNames;
import com.timder.kontor.util.ObservableList;
import com.timder.kontor.util.ObservableValue;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

public final class LawyerUi {

    public static ModularUI create(EmployeeDeskContext context, LawyerActions actions) {
        var tabView = UiContainer.largeTabView();
        tabView.addTab(UiContainer.tab(Component.translatable("ui.createkontor.lawyer_desk.legal_form")), legalFormTab(context, actions));
        tabView.addTab(UiContainer.tab(Component.literal("Licenses")), licensesTab(context, actions));
        tabView.addTab(UiContainer.tab(Component.literal("License Catalog")), licenseCatalogTab(context, actions));

        var root = UiContainer.withHud(tabView, UiContainer.hudBox(context.company(), context.economy()));
        return new ModularUI(UI.of(root, StylesheetManager.GDP), context.player());
    }

    public static UIElement legalFormTab(EmployeeDeskContext context, LawyerActions actions) {
        ScrollerView content = UiContainer.tabScroller();

        Label legalFormLabel = UiLabels.h2(Component.empty(), Horizontal.CENTER);
        legalFormLabel.addSyncValue(
                DataBindingBuilder.componentS2C(() -> Component.translatable("legal_forms.createkontor." + context.company().legalForm(CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()))).toString()))
                        .onRemoteSyncReceived(legalFormLabel::setText)
                        .build().getSyncValue()
        );

        Label runningUpgrade = UiLabels.primary(Component.empty(), Horizontal.CENTER);
        runningUpgrade.addSyncValue(
                DataBindingBuilder.boolS2C(() -> context.company().upgradeApplication().isPresent())
                        .onRemoteSyncReceived((applicationPresent) -> {
                            if (applicationPresent) {
                                runningUpgrade.setText(Component.translatable("ui.createkontor.lawyer_desk.legal_form.upgrade_in_progress"));
                            } else {
                                runningUpgrade.setText(Component.translatable("ui.createkontor.lawyer_desk.legal_form.no_upgrade"));
                            }
                        }).build().getSyncValue()
        );

        content.addScrollViewChildren(UiContainer.headerWithSubtitle(legalFormLabel, runningUpgrade));

        ObservableValue<LegalFormDef> currentLegalForm = new ObservableValue<>(null);
        SimpleBinding<Tag> currentLegalFormBinding = DataBindingBuilder.tagS2C(() -> legalFormToTag(context.company().legalForm(CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions())))))
                .onRemoteSyncReceived(tag -> currentLegalForm.set(tagToLegalForm(tag).get()))
                .build();

        UIElement currentLegalFormStatsElement = new UIElement()
                .layout(layout -> layout.paddingHorizontal(32).marginVertical(8))
                .addSyncValue(currentLegalFormBinding.getSyncValue());

        currentLegalForm.addListener(() -> {
            currentLegalFormStatsElement.clearAllChildren();
            currentLegalFormStatsElement.addChild(statsElement(currentLegalForm.get(), null));
        });
        content.addScrollViewChildren(currentLegalFormStatsElement);

        ObservableValue<LegalFormDef> nextLegalForm = new ObservableValue<>(null);
        SimpleBinding<Tag> nextLegalFormBinding = DataBindingBuilder.tagS2C(() -> {
                    CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
                    LegalForms forms = params.legalForms();
                    if (!forms.hasNext(context.company().legalLevel())) return new CompoundTag();
                    return legalFormToTag(forms.next(context.company().legalLevel()));
                })
                .onRemoteSyncReceived(tag -> nextLegalForm.set(tagToLegalForm(tag).orElse(null)))
                .build();

        ObservableValue<UpgradeChecklist> checklist = new ObservableValue<>(null);
        SimpleBinding<Tag> checklistBinding = DataBindingBuilder.tagS2C(() -> {
                    CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
                    LegalForms forms = params.legalForms();
                    if (!forms.hasNext(context.company().legalLevel())) return new CompoundTag();
                    return checklistToTag(UpgradeRules.checkApplication(
                            context.company(),
                            forms.next(context.company().legalLevel()),
                            context.economy().overallReputationInStars(context.company().id())));
                })
                .onRemoteSyncReceived(tag -> checklist.set(tagToChecklist(tag).orElse(null)))
                .build();

        ObservableValue<ApplicationStatus> applicationStatus = new ObservableValue<>(null);
        SimpleBinding<Tag> applicationBinding = DataBindingBuilder.tagS2C(
                        () -> applicationToTag(context.company().upgradeApplication()))
                .onRemoteSyncReceived(tag -> applicationStatus.set(tagToApplicationStatus(tag)))
                .build();

        UIElement upgradeContainer = new UIElement()
                .style(style -> style.background(Sprites.BORDER_DARK))
                .layout(layout -> layout.paddingAll(8).paddingHorizontal(32))
                .addSyncValue(currentLegalFormBinding.getSyncValue())
                .addSyncValue(nextLegalFormBinding.getSyncValue())
                .addSyncValue(checklistBinding.getSyncValue())
                .addSyncValue(applicationBinding.getSyncValue());
        upgradeContainer.onMessage("c2s_apply_upgrade", tag -> ToastPayload.send(context.player(), actions.applyForUpgrade()));

        nextLegalForm.addListener(() -> buildUpgradeContainer(currentLegalForm.get(), nextLegalForm.get(), checklist.get(), applicationStatus.get(), upgradeContainer));
        currentLegalForm.addListener(() -> buildUpgradeContainer(currentLegalForm.get(), nextLegalForm.get(), checklist.get(), applicationStatus.get(), upgradeContainer));
        checklist.addListener(() -> buildUpgradeContainer(currentLegalForm.get(), nextLegalForm.get(), checklist.get(), applicationStatus.get(), upgradeContainer));
        applicationStatus.addListener(() -> buildUpgradeContainer(currentLegalForm.get(), nextLegalForm.get(), checklist.get(), applicationStatus.get(), upgradeContainer));

        content.addScrollViewChildren(upgradeContainer);

        return content;
    }

    public static void buildUpgradeContainer(LegalFormDef current, LegalFormDef next, UpgradeChecklist checklist, ApplicationStatus status, UIElement parent) {
        parent.clearAllChildren();
        if (current == null) {
            return;
        }
        if (next == null) {
            parent.addChild(UiLabels.secondary(Component.translatable("ui.createkontor.lawyer_desk.legal_form.no_next_legal_form"), Horizontal.CENTER));
            return;
        }

        parent.addChild(UiContainer.headerWithSubtitle(
                UiLabels.h2(Component.translatable("ui.createkontor.lawyer_desk.legal_form.upgrade_to"), Horizontal.CENTER),
                UiLabels.h2(Component.translatable("legal_forms.createkontor." + next), Horizontal.CENTER)
        ));

        parent.addChild(UiLabels.h2(Component.translatable("ui.createkontor.lawyer_desk.legal_form.stats"), Horizontal.LEFT).layout(layout -> layout.marginTop(12)));
        parent.addChild(new UIElement()
                .layout(layout -> layout.marginBottom(12))
                .addChild(statsElement(current, next)));

        if (checklist != null && status == null) {
            parent.addChild(UiLabels.h2(Component.translatable("ui.createkontor.lawyer_desk.legal_form.upgrade_requirements"), Horizontal.LEFT));
            for (UpgradeChecklist.Row row : checklist.rows()) {
                switch (row) {
                    case UpgradeChecklist.MoneyRow moneyRow:
                        parent.addChild(UiLabels.labelValue(
                                Component.translatable("enum.createkontor.upgrade_criterion." + moneyRow.criterion().name().toLowerCase()),
                                ComponentFormatting.progress(moneyRow.actual().toString(), moneyRow.required().toString(), moneyRow.met()))
                        );
                        break;
                    case UpgradeChecklist.FlagRow flagRow:
                        parent.addChild(UiLabels.labelValue(
                                Component.translatable("enum.createkontor.upgrade_criterion." + flagRow.criterion().name().toLowerCase()),
                                ComponentFormatting.check(flagRow.fulfilled()))
                        );
                        break;
                    case UpgradeChecklist.StarsRow starsRow:
                        double floored = Math.floor((starsRow.actual() + UpgradeChecklist.StarsRow.TOLERANCE) * 100) / 100;
                        parent.addChild(UiLabels.labelValue(
                                Component.translatable("enum.createkontor.upgrade_criterion." + starsRow.criterion().name().toLowerCase()),
                                ComponentFormatting.progress(String.format(Locale.ROOT, "%.2f", floored), String.valueOf(starsRow.required()), starsRow.met()))
                        );
                        break;
                    case UpgradeChecklist.CountRow countRow:
                        parent.addChild(UiLabels.labelValue(
                                Component.translatable("enum.createkontor.upgrade_criterion." + countRow.criterion().name().toLowerCase()),
                                ComponentFormatting.progress(String.valueOf(countRow.actual()), String.valueOf(countRow.required()), countRow.met()))
                        );
                        break;
                }
            }

            if (checklist.allMet()) {
                parent.addChild(UiLabels.paragraphPrimary(Component.translatable("ui.createkontor.lawyer_desk.legal_form.all_requirements_met").withStyle(ChatFormatting.GREEN), Horizontal.LEFT));
            } else {
                parent.addChild(UiLabels.paragraphPrimary(Component.translatable("ui.createkontor.lawyer_desk.legal_form.not_all_requirements_met").withStyle(ChatFormatting.RED), Horizontal.LEFT));
            }

            parent.addChild(UiLabels.paragraphPrimary(
                    Component.translatable("ui.createkontor.lawyer_desk.legal_form.application_fee",
                    ComponentFormatting.moneyNegative(next.entryRequirements().fee())), Horizontal.LEFT).layout(layout -> layout.marginTop(12)));

            parent.addChild(UiLabels.paragraphSecondary(Component.translatable("ui.createkontor.lawyer_desk.legal_form.upgrade_info"), Horizontal.LEFT).layout(layout -> layout.marginTop(4)));
            parent.addChild(UiLabels.paragraphSecondary(Component.translatable("ui.createkontor.lawyer_desk.legal_form.upgrade_lawyer_lost"), Horizontal.LEFT).layout(layout -> layout.marginTop(4)));

            parent.addChild(UiButtons.primary(Component.translatable("ui.createkontor.lawyer_desk.legal_form.apply_for_upgrade", ComponentFormatting.moneyNegative(next.entryRequirements().fee())))
                            .setOnClick((e) -> parent.sendMessage("c2s_apply_upgrade")).layout(layout -> layout.marginTop(4)).setActive(checklist.allMet()));
        }

        if (status != null) {
            if (!status.processing()) {
                parent.addChild(UiLabels.paragraphError(Component.translatable(
                        "ui.createkontor.lawyer_desk.legal_form.waiting_for_net_worth",
                        ComponentFormatting.moneyNegative(next.entryRequirements().minNetWorth())
                ), Horizontal.LEFT));
                parent.addChild(UiLabels.paragraphError(Component.translatable(
                        "ui.createkontor.lawyer_desk.legal_form.resting_days_remaining",
                        ComponentFormatting.highlightError(String.valueOf(status.restingDaysLeft))
                ), Horizontal.LEFT));
            } else {
                parent.addChild(UiLabels.paragraphPrimary(Component.translatable(
                        "ui.createkontor.lawyer_desk.legal_form.upgrade_to_in_progress",
                        Component.translatable("legal_forms.createkontor." + status.targetId)
                ), Horizontal.LEFT));
            }

            parent.addChild(
                    new ProgressBar()
                            .setRange(0, 100)
                            .setProgress(status.progressPercent())
                            .label(label -> label.setText(Component.literal(status.progressPercent() + " %").withStyle(ChatFormatting.GOLD)))
            );
        }
    }

    public static UIElement licensesTab(EmployeeDeskContext context, LawyerActions actions) {
        ScrollerView content = UiContainer.tabScroller();

        ObservableList<HeldLicense> heldLicenses = new ObservableList<>();
        SimpleBinding<Tag> heldLicensesBinding = DataBindingBuilder.tagS2C(() -> heldLicensesToTag(context))
                .onRemoteSyncReceived(tag -> heldLicenses.set(tagToHeldLicenses(tag)))
                .build();

        Label title = UiLabels.h1(Component.translatable("ui.createkontor.lawyer_desk.licenses"), Horizontal.LEFT);
        title.addSyncValue(heldLicensesBinding.getSyncValue());
        heldLicenses.addListener(() -> title.setText(Component.translatable("ui.createkontor.lawyer_desk.licenses_count", heldLicenses.get().size())));
        content.addScrollViewChildren(title);

        UIElement licensesBox = new UIElement().addSyncValue(heldLicensesBinding.getSyncValue());
        licensesBox.onMessage("c2s_cancel_license", tag -> ToastPayload.send(context.player(), actions.cancelLicense(LicenseKeyCodec.decode(tag.getString("Key")))));
        heldLicenses.addListener(() -> {
            licensesBox.clearAllChildren();

            if (heldLicenses.get().isEmpty()) {
                licensesBox.addChild(UiLabels.paragraphSecondary(Component.translatable("ui.createkontor.lawyer_desk.licenses.empty"), Horizontal.LEFT));
                return;
            }

            for (HeldLicense license : heldLicenses.get()) {
                UIElement element = new UIElement()
                        .style(style -> style.background(Sprites.BORDER_DARK))
                        .layout(layout -> layout.paddingAll(8));

                element.addChild(UiLabels.paragraphTertiary(Component.literal(license.key().toString()), Horizontal.LEFT));
                element.addChild(UiLabels.paragraphPrimary(LicenseNames.of(license.key()).withStyle(ChatFormatting.GOLD), Horizontal.LEFT));
                element.addChild(UiContainer.itemSlotRow(license.markets().stream().map((id) -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(id.value())).asItem().getDefaultInstance()).toList())
                        .layout(layout -> layout.marginBottom(2)));

                double revenueShare;
                if (license.definition().isPresent()) {
                    revenueShare = license.definition().get().revenueShare();
                } else {
                    revenueShare = 0.0;
                }
                element.addChild(UiLabels.seperatedLabelRow(List.of(
                        UiLabels.primary(Component.translatable("ui.createkontor.lawyer_desk.licenses.daily_fee", license.dailyFee().toString()), Horizontal.CENTER),
                        UiLabels.primary(Component.translatable("ui.createkontor.lawyer_desk.licenses.revenue_share", revenueShare * 100 + "%"), Horizontal.CENTER)
                )).layout(layout -> layout.justifyContent(AlignContent.FLEX_START).gapAll(0)));

                if (!license.cancelled()) {
                    element.addChild(
                            UiButtons.critical(Component.translatable("ui.createkontor.lawyer_desk.licenses.cancel"))
                                    .setOnClick((e) -> {
                                        CompoundTag tag = new CompoundTag();
                                        tag.putString("Key", LicenseKeyCodec.encode(license.key()));
                                        licensesBox.sendMessage("c2s_cancel_license", tag);
                                    })
                                    .layout(layout -> layout.widthPercent(30))
                    );
                } else {
                    element.addChild(UiLabels.paragraphError(Component.translatable("ui.createkontor.lawyer_desk.licenses.cancelled"), Horizontal.LEFT));
                }

                licensesBox.addChild(element);
            }
        });

        content.addScrollViewChildren(licensesBox);
        return content;
    }

    public static UIElement licenseCatalogTab(EmployeeDeskContext context, LawyerActions actions) {
        UIElement content = new UIElement()
                .layout(layout -> layout.widthPercent(100).heightPercent(100));

        ObservableList<LicenseCard> catalog = new ObservableList<>();
        SimpleBinding<Tag> catalogBinding = DataBindingBuilder.tagS2C(() -> licenseOffersToTag(context))
                .onRemoteSyncReceived(tag -> catalog.set(tagToLicenseCards(tag)
                        .stream()
                        .sorted(Comparator
                                .comparing((LicenseCard card) -> !isPurchasable(card))
                                .thenComparing(card -> LicenseNames.of(card.key()).getString(), String.CASE_INSENSITIVE_ORDER))
                        .toList()))
                .build();

        Label title = UiLabels.h1(Component.translatable("ui.createkontor.lawyer_desk.license_catalog"), Horizontal.LEFT);
        title.addSyncValue(catalogBinding.getSyncValue());
        catalog.addListener(() -> title.setText(Component.translatable("ui.createkontor.lawyer_desk.license_catalog_count", catalog.get().size())));
        content.addChild(title);

        content.addSyncValue(catalogBinding.getSyncValue());
        content.onMessage("c2s_buy_license", tag -> ToastPayload.send(context.player(), actions.buyLicense(LicenseKeyCodec.decode(tag.getString("Key")))));
        content.addChild(UiContainer.searchableGrid(
                catalog,
                (LicenseCard offer) -> {
                    UIElement element = new UIElement()
                            .style(style -> style.background(Sprites.BORDER_DARK))
                            .layout(layout -> layout.paddingAll(8));

                    element.addChild(UiLabels.paragraphTertiary(Component.literal(offer.key().toString()), Horizontal.LEFT));
                    element.addChild(UiLabels.paragraphPrimary(LicenseNames.of(offer.key()).withStyle(ChatFormatting.GOLD), Horizontal.LEFT));
                    element.addChild(UiContainer.itemSlotRow(offer.markets().stream().map((id) -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(id.value())).asItem().getDefaultInstance()).toList())
                            .layout(layout -> layout.marginBottom(2)));

                    element.addChild(UiLabels.labelValueNoWrap(Component.translatable("ui.createkontor.lawyer_desk.license_catalog.daily_fee"), Component.literal(offer.dailyFee().toString())));
                    element.addChild(UiLabels.labelValueNoWrap(Component.translatable("ui.createkontor.lawyer_desk.license_catalog.revenue_share"), Component.literal(offer.revenueShare() * 100 + "%")));
                    element.addChild(UiLabels.labelValueNoWrap(Component.translatable("ui.createkontor.lawyer_desk.license_catalog.application_fee"), Component.literal(offer.applicationFee().toString())));

                    element.addChild(new UIElement().layout(layout -> layout.flexGrow(1))); // Spacer

                    if (offer.status() != AcquireResult.Status.ACQUIRED && offer.status() != AcquireResult.Status.REACTIVATED) {
                        element.addChild(UiLabels.paragraphError(Component.translatable("ui.createkontor.lawyer_desk.license_catalog." + offer.status().toString().toLowerCase()), Horizontal.LEFT));
                    } else {
                        List<Component> lines = new ArrayList<>();
                        lines.add(Component.translatable("ui.createkontor.lawyer_desk.license_catalog.covered_markets").withStyle(ChatFormatting.GRAY));
                        for (ItemId market : offer.coveredMarkets()) {
                            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(market.value()));
                            lines.add(Component.literal("• ").append(item.getDescription()));
                        }
                        Component label = offer.status() == AcquireResult.Status.ACQUIRED
                                ? Component.translatable("ui.createkontor.lawyer_desk.license_catalog.buy")
                                : Component.translatable("ui.createkontor.lawyer_desk.license_catalog.reactivate");
                        Button button = UiButtons.primary(label).setOnClick((e) -> {
                                    CompoundTag tag = new CompoundTag();
                                    tag.putString("Key", LicenseKeyCodec.encode(offer.key()));
                                    content.sendMessage("c2s_buy_license", tag);
                                });
                        button.layout(layout -> layout.heightAuto().minHeight(20).marginTopAuto()
                                .alignItems(AlignItems.CENTER).paddingVertical(4));
                        button.textStyle(style -> style
                                .adaptiveWidth(false)
                                .textWrap(TextWrap.WRAP)
                                .adaptiveHeight(true));
                        button.text.layout(layout -> layout.widthAuto().flexBasis(0).flexGrow(1));
                        if (!offer.coveredMarkets().isEmpty()) {
                            button.style(style -> style.tooltips(lines.toArray(Component[]::new)));
                        }
                        element.addChild(button);
                    }

                    return element;
                },
                (LicenseCard offer) -> {
                    List<String> list = new ArrayList<>();
                    list.add(LicenseNames.of(offer.key).getString());
                    for (ItemId market : offer.markets()) {
                        list.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse(market.value())).asItem().getDescription().getString());
                    }
                    return list;
                },
                Component.translatable("ui.createkontor.lawyer_desk.license_catalog.search"),
                Component.translatable("ui.createkontor.lawyer_desk.license_catalog.no_result"),
                5,
                128));

        return content;
    }

    private static UIElement statsElement(LegalFormDef current, @Nullable LegalFormDef next) {
        List<UIElement> left = List.of(
                statRow("Max. Employees", count(current.maxEmployees(), nextOf(next, LegalFormDef::maxEmployees), true)),
                statRow("Employee Salary %", percent(current.employeeSalaryFactor(), nextOf(next, LegalFormDef::employeeSalaryFactor), false)),
                statRow("Overdraft Limit", money(current.overdraftLimit(), nextOf(next, LegalFormDef::overdraftLimit), true)),
                statRow("Bank Loan Limit", money(current.bankLoanLimit(), nextOf(next, LegalFormDef::bankLoanLimit), true)),
                statRow("Logistics Network Integration", flag(current.logisticsNetwork(), nextOf(next, LegalFormDef::logisticsNetwork)))
        );

        List<UIElement> right = List.of(
                statRow("Max. Licenses", count(current.maxProductLicenses(), nextOf(next, LegalFormDef::maxProductLicenses), true)),
                statRow("Max. Open Requests Total", count(current.maxOpenRequestsTotal(), nextOf(next, LegalFormDef::maxOpenRequestsTotal), true)),
                statRow("Max. Open Requests Per Product", count(current.maxOpenRequestsPerProduct(), nextOf(next, LegalFormDef::maxOpenRequestsPerProduct), true)),
                statRow("Order Book Size", count(current.maxOpenOrders(), nextOf(next, LegalFormDef::maxOpenOrders), true)),
                statRow("Max. Shipping Exits", count(current.maxShippingExits(), nextOf(next, LegalFormDef::maxShippingExits), true))
        );

        return UiContainer.twoColumns(left, right);
    }

    private static UIElement statRow(String label, Component value) {
        return UiLabels.labelValue(Component.literal(label), value);
    }

    private static <T> T nextOf(@Nullable LegalFormDef next, Function<LegalFormDef, T> getter) {
        return next == null ? null : getter.apply(next);
    }

    private static Component count(int current, @Nullable Integer next, boolean higherIsBetter) {
        return compared(formatCount(current), next == null ? null : formatCount(next),
                next == null ? 0 : Integer.compare(next, current), higherIsBetter);
    }

    private static Component money(Money current, @Nullable Money next, boolean higherIsBetter) {
        return compared(current.toString(), next == null ? null : next.toString(),
                next == null ? 0 : next.compareTo(current), higherIsBetter);
    }

    private static Component percent(double current, @Nullable Double next, boolean higherIsBetter) {
        return compared(formatPercent(current), next == null ? null : formatPercent(next),
                next == null ? 0 : Double.compare(next, current), higherIsBetter);
    }

    private static Component flag(boolean current, @Nullable Boolean next) {
        if (next == null || next == current) {
            return ComponentFormatting.check(current);
        }
        return Component.empty()
                .append(ComponentFormatting.check(current))
                .append(Component.literal(" >>> ").withStyle(ChatFormatting.DARK_GRAY))
                .append(ComponentFormatting.check(next));
    }

    private static Component compared(String oldText, @Nullable String newText, int cmp, boolean higherIsBetter) {
        if (newText == null || cmp == 0) {
            return Component.literal(oldText); // unchanged values are shown once
        }
        boolean improved = (cmp > 0) == higherIsBetter;
        return Component.empty()
                .append(Component.literal(oldText).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(" >>> ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(newText).withStyle(improved ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private static String formatCount(int value) {
        return value == LegalFormDef.UNLIMITED ? "Unlimited" : String.valueOf(value);
    }

    private static String formatPercent(double factor) {
        return BigDecimal.valueOf(factor).movePointRight(2).stripTrailingZeros().toPlainString() + "%";
    }

    private static Optional<LegalFormDef> tagToLegalForm(Tag tag) {
        CompoundTag t = (CompoundTag) tag;
        if (!((CompoundTag) tag).contains("Id")) {
            return Optional.empty();
        }
        int level = t.getInt("Level");
        UpgradeRequirements requirements = level == 1 ? null : tagToRequirements(t.getCompound("Requirements"));
        return Optional.of(new LegalFormDef(
                        level,
                        t.getString("Id"),
                        t.getInt("MaxEmployees"),
                        t.getDouble("EmployeeSalaryFactor"),
                        t.getInt("MaxProductLicenses"),
                        t.getInt("MaxOpenRequestsPerProduct"),
                        t.getInt("MaxOpenRequestsTotal"),
                        t.getInt("MaxOpenOrders"),
                        t.getInt("MaxOpenContracts"),
                        t.getInt("MaxOrderQuantity"),
                        t.getDouble("DeadlineFactor"),
                        t.getInt("MaxGridConnection"),
                        t.getInt("FeedInLicenseTier"),
                        Money.ofCents(t.getLong("OverdraftLimitCents")),
                        Money.ofCents(t.getLong("BankLoanLimitCents")),
                        t.getBoolean("AutoAcceptRequests"),
                        t.getBoolean("LogisticsNetwork"),
                        Money.ofCents(t.getLong("FreeStorageCents")),
                        t.getBoolean("FounderProtection"),
                        t.getInt("MaxShippingExits"),
                        requirements
                )
        );
    }

    private static Tag legalFormToTag(LegalFormDef legalForm) {
        CompoundTag t = new CompoundTag();
        t.putInt("Level", legalForm.level());
        t.putString("Id", legalForm.id());
        t.putInt("MaxEmployees", legalForm.maxEmployees());
        t.putDouble("EmployeeSalaryFactor", legalForm.employeeSalaryFactor());
        t.putInt("MaxProductLicenses", legalForm.maxProductLicenses());
        t.putInt("MaxOpenRequestsPerProduct", legalForm.maxOpenRequestsPerProduct());
        t.putInt("MaxOpenRequestsTotal", legalForm.maxOpenRequestsTotal());
        t.putInt("MaxOpenOrders", legalForm.maxOpenOrders());
        t.putInt("MaxOpenContracts", legalForm.maxOpenContracts());
        t.putInt("MaxOrderQuantity", legalForm.maxOrderQuantity());
        t.putDouble("DeadlineFactor", legalForm.deadlineFactor());
        t.putInt("MaxGridConnection", legalForm.maxGridConnection());
        t.putInt("FeedInLicenseTier", legalForm.feedInLicenseTier());
        t.putLong("OverdraftLimitCents", legalForm.overdraftLimit().cents());
        t.putLong("BankLoanLimitCents", legalForm.bankLoanLimit().cents());
        t.putBoolean("AutoAcceptRequests", legalForm.autoAcceptRequests());
        t.putBoolean("LogisticsNetwork", legalForm.logisticsNetwork());
        t.putLong("FreeStorageCents", legalForm.freeStorage().cents());
        t.putBoolean("FounderProtection", legalForm.founderProtection());
        t.putInt("MaxShippingExits", legalForm.maxShippingExits());
        if (legalForm.hasEntryRequirements()) {
            t.put("Requirements", requirementsToTag(legalForm.entryRequirements()));
        }
        return t;
    }

    private static CompoundTag requirementsToTag(UpgradeRequirements requirements) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("FeeCents", requirements.fee().cents());
        tag.putLong("MinNetWorthCents", requirements.minNetWorth().cents());
        tag.putInt("MinFulfilledOrders", requirements.minFulfilledOrders());
        tag.putDouble("MinReputationStars", requirements.minReputationStars());
        tag.putLong("ProcessingTicks", requirements.processingTicks());
        tag.putInt("RestingDays", requirements.restingDays());
        return tag;
    }

    private static UpgradeRequirements tagToRequirements(CompoundTag tag) {
        return new UpgradeRequirements(
                Money.ofCents(tag.getLong("FeeCents")),
                Money.ofCents(tag.getLong("MinNetWorthCents")),
                tag.getInt("MinFulfilledOrders"),
                tag.getDouble("MinReputationStars"),
                tag.getLong("ProcessingTicks"),
                tag.getInt("RestingDays"));
    }

    private static Tag checklistToTag(UpgradeChecklist checklist) {
        CompoundTag t = new CompoundTag();
        t.putString("Mode", checklist.mode().name());
        t.putInt("TargetLevel", checklist.targetLevel());

        ListTag rows = new ListTag();
        for (UpgradeChecklist.Row row : checklist.rows()) {
            CompoundTag r = new CompoundTag();
            r.putString("Criterion", row.criterion().name());
            switch (row) {
                case UpgradeChecklist.MoneyRow money -> {
                    r.putString("Type", "MONEY");
                    r.putLong("ActualCents", money.actual().cents());
                    r.putLong("RequiredCents", money.required().cents());
                }
                case UpgradeChecklist.CountRow count -> {
                    r.putString("Type", "COUNT");
                    r.putInt("Actual", count.actual());
                    r.putInt("Required", count.required());
                }
                case UpgradeChecklist.StarsRow stars -> {
                    r.putString("Type", "STARS");
                    r.putDouble("Actual", stars.actual());
                    r.putDouble("Required", stars.required());
                }
                case UpgradeChecklist.FlagRow flag -> {
                    r.putString("Type", "FLAG");
                    r.putBoolean("Fulfilled", flag.fulfilled());
                }
            }
            rows.add(r);
        }
        t.put("Rows", rows);
        return t;
    }

    private static Optional<UpgradeChecklist> tagToChecklist(Tag tag) {
        CompoundTag t = (CompoundTag) tag;
        if (!t.contains("Mode")) {
            return Optional.empty();
        }

        List<UpgradeChecklist.Row> rows = new ArrayList<>();
        for (Tag rowTag : t.getList("Rows", Tag.TAG_COMPOUND)) {
            CompoundTag r = (CompoundTag) rowTag;
            UpgradeCriterion criterion = UpgradeCriterion.valueOf(r.getString("Criterion"));
            rows.add(switch (r.getString("Type")) {
                case "MONEY" -> new UpgradeChecklist.MoneyRow(criterion,
                        Money.ofCents(r.getLong("ActualCents")),
                        Money.ofCents(r.getLong("RequiredCents")));
                case "COUNT" -> new UpgradeChecklist.CountRow(criterion, r.getInt("Actual"), r.getInt("Required"));
                case "STARS" -> new UpgradeChecklist.StarsRow(criterion, r.getDouble("Actual"), r.getDouble("Required"));
                case "FLAG" -> new UpgradeChecklist.FlagRow(criterion, r.getBoolean("Fulfilled"));
                default -> throw new IllegalArgumentException("Unknown checklist row type: " + r.getString("Type"));
            });
        }

        return Optional.of(new UpgradeChecklist(
                UpgradeCheckMode.valueOf(t.getString("Mode")),
                t.getInt("TargetLevel"),
                rows
        ));
    }

    private record ApplicationStatus(String targetId, boolean processing, int progressPercent, int restingDaysLeft) {
    }

    private static Tag applicationToTag(Optional<UpgradeApplication> application) {
        CompoundTag tag = new CompoundTag();
        if (application.isEmpty()) {
            return tag;
        }
        UpgradeApplication a = application.get();
        LegalFormDef target = KontorData.getLegalFormDefinitions().stream()
                .filter(form -> form.level() == a.targetLevel())
                .findFirst().orElse(null);

        tag.putInt("TargetLevel", a.targetLevel());
        tag.putString("TargetId", target == null ? "" : target.id());
        tag.putBoolean("Processing", a.isProcessing());
        tag.putInt("Progress", target == null ? 0 : progressPercent(a, target));
        tag.putInt("RestingDaysLeft", a.restingDaysLeft());
        return tag;
    }

    private static int progressPercent(UpgradeApplication application, LegalFormDef target) {
        if (!application.isProcessing()) {
            return 100;
        }
        long total = target.entryRequirements().processingTicks();
        if (total < 1) {
            return 100;
        }
        long percent = (total - application.ticksLeft()) * 100 / total;
        return (int) Math.max(0, Math.min(100, percent));
    }

    @Nullable
    private static ApplicationStatus tagToApplicationStatus(Tag tag) {
        if (!(tag instanceof CompoundTag compound) || !compound.contains("TargetLevel", Tag.TAG_INT)) {
            return null;
        }
        return new ApplicationStatus(
                compound.getString("TargetId"),
                compound.getBoolean("Processing"),
                compound.getInt("Progress"),
                compound.getInt("RestingDaysLeft"));
    }

    private record LicenseCard(LicenseKey key, List<ItemId> markets, List<ItemId> coveredMarkets, Money dailyFee, Money applicationFee, @Nullable Money lockedDailyFee, AcquireResult.Status status, int minLegalLevel, double revenueShare) {}

    private static Tag licenseOffersToTag(EmployeeDeskContext context) {
        ListTag list = new ListTag();
        if (context.company() == null || context.economy() == null) {
            return list;
        }
        List<LicenseOffer> offers;
        try {
            CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
            offers = LicenseOffers.list(context.company(), context.economy(), params);
        } catch (IllegalArgumentException e) {
            return list;
        }
        for (LicenseOffer offer : offers) {
            LicenseDef license = offer.license();
            CompoundTag entry = new CompoundTag();
            entry.putString("Key", LicenseKeyCodec.encode(license.key()));
            entry.put("Markets", itemIdsToTag(offer.license().markets()));
            entry.put("Covered", itemIdsToTag(offer.alreadyCovered()));
            entry.putLong("DailyFeeCents", offer.dailyFee().cents());
            entry.putLong("ApplicationFeeCents", offer.applicationFee().cents());
            offer.lockedDailyFee().ifPresent(fee -> entry.putLong("LockedDailyFeeCents", fee.cents()));
            entry.putString("Status", offer.status().name());
            entry.putInt("MinLegalLevel", license.minLegalLevel());
            entry.putDouble("RevenueShare", license.revenueShare());
            list.add(entry);
        }
        return list;
    }

    public static ListTag itemIdsToTag(Collection<ItemId> ids) {
        ListTag list = new ListTag();
        for (ItemId id : ids) {
            list.add(StringTag.valueOf(id.value()));
        }
        return list;
    }

    private static List<LicenseCard> tagToLicenseCards(Tag tag) {
        if (!(tag instanceof ListTag list)) {
            return List.of();
        }
        List<LicenseCard> cards = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ListTag marketTags = entry.getList("Markets", Tag.TAG_STRING);
            List<ItemId> markets = new ArrayList<>();
            for (int j = 0; j < marketTags.size(); j++) {
                markets.add(new ItemId(marketTags.getString(j)));
            }
            cards.add(new LicenseCard(
                    LicenseKeyCodec.decode(entry.getString("Key")),
                    tagToItemIds(entry.getList("Markets", Tag.TAG_STRING)),
                    tagToItemIds(entry.getList("Covered", Tag.TAG_STRING)),
                    Money.ofCents(entry.getLong("DailyFeeCents")),
                    Money.ofCents(entry.getLong("ApplicationFeeCents")),
                    entry.contains("LockedDailyFeeCents", Tag.TAG_LONG) ? Money.ofCents(entry.getLong("LockedDailyFeeCents")) : null,
                    AcquireResult.Status.valueOf(entry.getString("Status")),
                    entry.getInt("MinLegalLevel"),
                    entry.getDouble("RevenueShare")));
        }
        return cards;
    }

    public static List<ItemId> tagToItemIds(ListTag list) {
        List<ItemId> ids = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ids.add(new ItemId(list.getString(i)));
        }
        return ids;
    }

    private static boolean isPurchasable(LicenseCard card) {
        return card.status() == AcquireResult.Status.ACQUIRED
                || card.status() == AcquireResult.Status.REACTIVATED;
    }

    private static Tag heldLicensesToTag(EmployeeDeskContext context) {
        ListTag list = new ListTag();
        Company company = context.company();
        if (company == null) return list;

        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        for (HeldLicense held : LicenseHoldingRules.holdings(company, params.licenses())) {
            CompoundTag tag = new CompoundTag();
            tag.putString("Key", LicenseKeyCodec.encode(held.key()));
            tag.putLong("AcquiredDay", held.acquiredDay());
            tag.putLong("DailyFeeCents", held.dailyFee().cents());
            tag.putBoolean("Cancelled", held.cancelled());
            held.definition().ifPresent(def -> {
                tag.put("Markets", itemIdsToTag(def.markets()));
                tag.putDouble("FeeFactor", def.feeFactor());
                tag.putDouble("DailyFraction", def.dailyFraction());
                tag.putDouble("RevenueShare", def.revenueShare());
                tag.putInt("MinLegalLevel", def.minLegalLevel());
            });
            list.add(tag);
        }
        return list;
    }

    private static List<HeldLicense> tagToHeldLicenses(Tag tag) {
        List<HeldLicense> held = new ArrayList<>();
        if (!(tag instanceof ListTag list)) return held;

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            LicenseKey key = LicenseKeyCodec.decode(entry.getString("Key"));
            LicenseHolding holding = new LicenseHolding(
                    key,
                    entry.getLong("AcquiredDay"),
                    Money.ofCents(entry.getLong("DailyFeeCents")),
                    entry.getBoolean("Cancelled"));
            Optional<LicenseDef> definition = entry.contains("Markets")
                    ? Optional.of(new LicenseDef(
                    key,
                    new LinkedHashSet<>(tagToItemIds(entry.getList("Markets", Tag.TAG_STRING))),
                    entry.getDouble("FeeFactor"),
                    entry.getDouble("DailyFraction"),
                    entry.getDouble("RevenueShare"),
                    entry.getInt("MinLegalLevel")))
                    : Optional.empty();
            held.add(new HeldLicense(holding, definition));
        }
        return held;
    }
}
