package com.timder.kontor.registry;

import com.timder.kontor.CreateKontor;
import com.timder.kontor.game.advancements.MilestoneTrigger;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.Optional;

import static com.lowdragmc.lowdraglib2.LDLib2.id;
import static com.timder.kontor.registry.KontorRegistries.REGISTRATE;

public class KontorAdvancements {
    public static final String ROOT = "root";

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/advancements/backgrounds/stone.png");

    static {
        REGISTRATE.addDataGenerator(ProviderType.ADVANCEMENT, KontorAdvancements::generate);
    }

    private static void generate(RegistrateAdvancementProvider provider) {
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                        new ItemStack(KontorBlocks.KONTOR_DESK.get()),
                        provider.title(CreateKontor.MODID, ROOT, "Create: Kontor"),
                        provider.desc(CreateKontor.MODID, ROOT, "Craft a Kontor Desk and start your own business."),
                        BACKGROUND,
                        AdvancementType.TASK,
                        true,
                        true,
                        false)
                .addCriterion(
                        "has_kontor_desk", InventoryChangeTrigger.TriggerInstance.hasItems(KontorBlocks.KONTOR_DESK.get()))
                .build(id(ROOT));
        provider.accept(root);

        AdvancementHolder foundCompany = milestone(
                provider,
                root,
                "found_company",
                "Founder",
                "Found your first company using the Kontor Desk.",
                KontorBlocks.KONTOR_DESK.get(),
                AdvancementType.GOAL);
        provider.accept(foundCompany);

        AdvancementHolder firstEmployee = milestone(
                provider,
                foundCompany,
                "first_employee",
                "Boss",
                "Hire your first employee.",
                KontorBlocks.LAWYER_DESK.get(),
                AdvancementType.TASK);
        provider.accept(firstEmployee);

        AdvancementHolder firstOrderCompleted = milestone(
                provider,
                firstEmployee,
                "first_order_completed",
                "Cash on Delivery",
                "Complete your first order.",
                KontorBlocks.SHIPPING_EXIT.get(),
                AdvancementType.GOAL);
        provider.accept(firstOrderCompleted);

        AdvancementHolder firstProfit = milestone(
                provider,
                firstOrderCompleted,
                "first_profit",
                "Money, Money, Money",
                "Close a trading day with profit.",
                Items.PAPER,
                AdvancementType.TASK);
        provider.accept(firstProfit);

        AdvancementHolder maxProfitStreak = milestone(
                provider,
                firstProfit,
                "max_profit_streak",
                "Business Pro",
                "Reach the maximum profit streak of 360 days.",
                Items.PAPER,
                AdvancementType.CHALLENGE);
        provider.accept(maxProfitStreak);

        AdvancementHolder recordDay = milestone(
                provider,
                firstProfit,
                "record_day",
                "Personal Best",
                "Beat your best trading day.",
                Items.PAPER,
                AdvancementType.GOAL);
        provider.accept(recordDay);

        AdvancementHolder firstProperty = milestone(
                provider,
                firstEmployee,
                "first_property",
                "Location, Location, Location",
                "Acquire your first property.",
                Items.MAP,
                AdvancementType.TASK);
        provider.accept(firstProperty);

        AdvancementHolder legalFormUpgrade = milestone(
                provider,
                firstEmployee,
                "legal_form_upgrade",
                "Going Corporate",
                "Upgrade your legal form",
                Items.WRITABLE_BOOK,
                AdvancementType.GOAL);
        provider.accept(legalFormUpgrade);
    }

    private static AdvancementHolder milestone(
            RegistrateAdvancementProvider provider,
            AdvancementHolder parent,
            String key,
            String title,
            String description,
            ItemLike icon,
            AdvancementType type) {
        AdvancementHolder holder = Advancement.Builder.advancement()
                .parent(parent)
                .display(
                        new ItemStack(icon),
                        provider.title(CreateKontor.MODID, key, title),
                        provider.desc(CreateKontor.MODID, key, description),
                        null,
                        type,
                        true,
                        true,
                        false)
                .addCriterion(
                        key,
                        KontorTriggers.MILESTONE.get().createCriterion(new MilestoneTrigger.Instance(Optional.empty(), key)))
                .build(id(key));
        provider.accept(holder);
        return holder;
    }

    static void touch() {}

    private static ResourceLocation id(String key) {
        return ResourceLocation.fromNamespaceAndPath(CreateKontor.MODID, key);
    }
}
