package com.timder.kontor.game.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.market.MarketHistoryEntry;
import com.timder.kontor.core.market.MarketParticipant;
import com.timder.kontor.core.market.MarketShareBreakdown;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.util.CsvWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.ModList;

public class MarketExportCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kontor")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("market")
                        .then(Commands.literal("export")
                                .then(Commands.argument("product", ResourceLocationArgument.id())
                                        .executes(context -> execute(context, false))
                                        .then(Commands.argument("company", StringArgumentType.string())
                                                .executes(context -> execute(context, true)))))));
    }

    private static int execute(CommandContext<CommandSourceStack> context, boolean includeCompany) {
        CommandSourceStack source = context.getSource();
        Economy economy = EconomySavedData.get(source.getServer()).getEconomy();
        ResourceLocation location = ResourceLocationArgument.getId(context, "product");
        ItemId product = new ItemId(location.toString());

        if (!economy.isMarket(product)) {
            source.sendFailure(Component.literal("There is no market for " + location));
            return 0;
        }

        Company company = null;
        if (includeCompany) {
            company = CommandSupport.findCompany(context, "company");
            if (company == null) {
                return 0;
            }
        }

        List<MarketHistoryEntry> history = economy.marketHistory(product);

        Path directory = source.getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("kontor")
                .resolve("exports");
        Path file = directory.resolve(fileName(product, economy.currentDay(), company, ZonedDateTime.now()));
        try (CsvWriter csv = CsvWriter.create(file)) {
            csv.comment("Create: Kontor Market Export");

            // Meta data

            csv.section("context", "field", "value");
            csv.keyValue(
                    "exported_at",
                    DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT).format(ZonedDateTime.now()));
            csv.keyValue("exported_by", source.getTextName());
            csv.keyValue(
                    "mod_version",
                    ModList.get()
                            .getModContainerById(CreateKontor.MODID)
                            .map(container ->
                                    container.getModInfo().getVersion().toString())
                            .orElse("unknown"));
            csv.keyValue(
                    "world_time", String.valueOf(source.getServer().overworld().getGameTime()));

            // Company data

            if (includeCompany) {
                csv.keyValue("company", company.toString());

                Optional<MarketParticipant> stored = economy.storedParticipant(product, company.id());
                boolean active = economy.isParticipant(product, company.id());
                String participation = stored.isEmpty() ? "not_registered" : active ? "active" : "paused";
                MarketShareBreakdown share = active ? economy.marketShare(product, company.id()) : null;

                csv.section("company", "field", "value");
                csv.keyValue("company_id", company.id().value());
                csv.keyValue("balance", company.account().getBalance());
                csv.keyValue("open_requests_total", company.requestBoard().openRequestsTotal());
                csv.keyValue("open_requests_this", company.requestBoard().openRequestsForProduct(product));
                csv.keyValue("open_orders_total", company.orderBook().openOrders());
                csv.keyValue("participation", participation);
                csv.keyValue("inactive", economy.isInactive(company.id()));
                csv.keyValue(
                        "list_price", stored.map(MarketParticipant::listPrice).orElse(null));
                csv.keyValue(
                        "reputation", stored.map(MarketParticipant::reputation).orElse(null));
                csv.keyValue(
                        "stars",
                        stored.map(MarketParticipant::reputationInStars).orElse(null));

                csv.keyValue("share_query_day", economy.currentDay());
                csv.keyValue("share_own", share == null ? null : share.own());
                csv.keyValue("share_other_companies", share == null ? null : share.otherCompanies());
                csv.keyValue("share_competition", share == null ? null : share.competition());
                csv.keyValue(
                        "expected_daily_quantity",
                        active ? economy.expectedDailyQuantity(product, company.id()) : null);
                csv.keyValue("overflow_share", active ? economy.overflowShare(product, company.id()) : null);
            }

            // Market history

            csv.section(
                    "history",
                    "line",
                    "tick",
                    "day",
                    "price_level",
                    "deviation",
                    "displayed_price",
                    "competitors",
                    "delivered_this_tick",
                    "demand",
                    "overflow");
            long line = 0;
            for (MarketHistoryEntry entry : history) {
                csv.row(
                        line++,
                        entry.tick(),
                        entry.day(),
                        entry.priceLevel(),
                        entry.deviation(),
                        entry.displayedPrice(),
                        entry.competitors(),
                        entry.deliveredThisTick(),
                        entry.demand(),
                        entry.overflow());
            }

            // Finishing up

            source.sendSuccess(
                    () -> Component.literal(String.format(
                            Locale.ROOT,
                            "Exported %s: %d trading ticks.%nFile: %s",
                            location,
                            history.size(),
                            file.toAbsolutePath().normalize())),
                    false);
            return 1;
        } catch (IOException e) {
            source.sendFailure(Component.literal("Could not write the export: " + e.getMessage()));
            return 0;
        }
    }

    private static String fileName(ItemId market, long day, Company company, ZonedDateTime time) {
        String safeMarket = market.value().replaceAll("[^A-Za-z0-9._-]", "_");
        String companyPart = company == null ? "" : "_company" + company.id().value();
        return "market_" + safeMarket + "_day" + day + companyPart + "_"
                + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT).format(time) + ".csv";
    }
}
