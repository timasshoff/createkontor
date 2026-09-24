package com.timder.kontor.game;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyHistoryEntry;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.core.company.financial.*;
import com.timder.kontor.core.company.order.Order;
import com.timder.kontor.core.company.order.OrderBook;
import com.timder.kontor.core.company.order.OrderPhase;
import com.timder.kontor.core.company.order.RequestOrigin;
import com.timder.kontor.core.company.request.Request;
import com.timder.kontor.core.company.request.RequestArrivals;
import com.timder.kontor.core.company.request.RequestBoard;
import com.timder.kontor.core.port.SeededRng;
import com.timder.kontor.core.value.ItemId;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class CompanySavedData extends SavedData {

    private static final String NAME = "kontor_companies";
    private final CompanyRegistry registry;
    private RequestArrivals arrivals;

    private CompanySavedData(CompanyRegistry registry) {
        this.registry = registry;
    }

    public CompanyRegistry getRegistry() {
        return registry;
    }

    public static CompanySavedData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        SavedData.Factory<CompanySavedData> factory = new SavedData.Factory<>(
                CompanySavedData::create,
                (tag, registries) -> load(tag)
        );
        return overworld.getDataStorage().computeIfAbsent(factory, NAME);
    }

    /**
     * @param server The server
     * @return The countdowns of the requests of all companies
     */
    public RequestArrivals getArrivals(MinecraftServer server) {
        if (arrivals == null) {
            ServerLevel overworld = server.overworld();
            arrivals = new RequestArrivals(SeededRng.forName(overworld.getSeed(), "request_arrivals:" + overworld.getGameTime()));
        }
        return arrivals;
    }

    private static CompanySavedData create() {
        return new CompanySavedData(new CompanyRegistry());
    }

    private static CompanySavedData load(CompoundTag tag) {
        CompanyRegistry.SaveState saveState = readSaveState(tag);
        return new CompanySavedData(CompanyRegistry.restore(saveState));
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        writeSaveState(compoundTag, registry.getSaveState());
        return compoundTag;
    }

    /*
    NBT <-> CompanyRegistry
     */

    private static void writeSaveState(CompoundTag tag, CompanyRegistry.SaveState saveState) {
        tag.putInt("NextId", saveState.nextId());

        ListTag companies = new ListTag();
        for (Company.SaveState companyState : saveState.companies()) {
            companies.add(writeCompany(companyState));
        }
        tag.put("Companies", companies);
    }

    private static CompanyRegistry.SaveState readSaveState(CompoundTag tag) {
        List<Company.SaveState> companies = new ArrayList<>();
        for (Tag t : tag.getList("Companies", Tag.TAG_COMPOUND)) {
            companies.add(readCompany((CompoundTag) t));
        }
        return new CompanyRegistry.SaveState(companies, tag.getInt("NextId"));
    }

    private static CompoundTag writeCompany(Company.SaveState state) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Id", state.id().value());
        tag.putString("Name", state.name());
        tag.putLong("FoundingDay", state.foundingDay());
        tag.putUUID("Owner", state.owner());

        ListTag managers = new ListTag();
        for (UUID manager : state.managers()) {
            managers.add(NbtUtils.createUUID(manager));
        }
        tag.put("Managers", managers);

        tag.putInt("LegalLevel", state.legalLevel());
        tag.put("Account", writeAccount(state.account()));

        ListTag loans = new ListTag();
        for (Loan.SaveState loan : state.loans()) {
            loans.add(writeLoan(loan));
        }
        tag.put("Loans", loans);

        tag.putString("Liquidity", state.liquidity().name());
        tag.putInt("DaysInTrouble", state.daysInTrouble());
        tag.putLong("NextRequestNumber", state.nextRequestNumber());
        tag.putLong("NextOrderNumber", state.nextOrderNumber());

        ListTag history = new ListTag();
        for (CompanyHistoryEntry entry : state.history()) {
            history.add(writeHistoryEntry(entry));
        }
        tag.put("History", history);

        tag.put("RequestBoard", writeRequestBoard(state.requestBoard()));
        tag.put("OrderBook", writeOrderBook(state.orderBook()));

        return tag;
    }

    private static Company.SaveState readCompany(CompoundTag tag) {
        CompanyId id = new CompanyId(tag.getInt("Id"));
        String name = tag.getString("Name");
        long foundingDay = tag.getLong("FoundingDay");
        UUID owner = tag.getUUID("Owner");

        Set<UUID> managers = new LinkedHashSet<>();
        for (Tag t : tag.getList("Managers", Tag.TAG_INT_ARRAY)) {
            managers.add(NbtUtils.loadUUID(t));
        }

        int legalLevel = tag.getInt("LegalLevel");
        Account.SaveState account = readAccount(tag.getCompound("Account"));

        List<Loan.SaveState> loans = new ArrayList<>();
        for (Tag t : tag.getList("Loans", Tag.TAG_COMPOUND)) {
            loans.add(readLoan((CompoundTag) t));
        }

        Liquidity liquidity = Liquidity.valueOf(tag.getString("Liquidity"));
        int daysInTrouble = tag.getInt("DaysInTrouble");
        long nextRequestNumber = tag.getLong("NextRequestNumber");
        long nextOrderNumber = tag.getLong("NextOrderNumber");

        List<CompanyHistoryEntry> history = new ArrayList<>();
        for (Tag t : tag.getList("History", Tag.TAG_COMPOUND)) {
            history.add(readHistoryEntry((CompoundTag) t));
        }

        RequestBoard.SaveState requestBoard = readRequestBoard(tag.getCompound("RequestBoard"));
        OrderBook.SaveState orderBook = readOrderBook(tag.getCompound("OrderBook"));

        return new Company.SaveState(
                id,
                name,
                foundingDay,
                owner,
                managers,
                legalLevel,
                account,
                loans,
                liquidity,
                daysInTrouble,
                nextRequestNumber,
                nextOrderNumber,
                history,
                requestBoard,
                orderBook
        );
    }

    private static CompoundTag writeAccount(Account.SaveState state) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Balance", state.balance().cents());

        ListTag bookings = new ListTag();
        for (Booking booking : state.bookings()) {
            CompoundTag bookingTag = new CompoundTag();
            bookingTag.putLong("Day", booking.day());
            bookingTag.putString("Kind", booking.kind().name());
            bookingTag.putLong("Amount", booking.amount().cents());
            bookingTag.putString("Reference", booking.reference());
            bookingTag.putLong("BalanceAfter", booking.balanceAfter().cents());
            bookings.add(bookingTag);
        }
        tag.put("Bookings", bookings);
        return tag;
    }

    private static Account.SaveState readAccount(CompoundTag tag) {
        Money balance = Money.ofCents(tag.getLong("Balance"));

        List<Booking> bookings = new ArrayList<>();
        for (Tag t : tag.getList("Bookings", Tag.TAG_COMPOUND)) {
            CompoundTag bookingTag = (CompoundTag) t;
            bookings.add(new Booking(
                    bookingTag.getLong("Day"),
                    BookingKind.valueOf(bookingTag.getString("Kind")),
                    Money.ofCents(bookingTag.getLong("Amount")),
                    bookingTag.getString("Reference"),
                    Money.ofCents(bookingTag.getLong("BalanceAfter"))));
        }
        return new Account.SaveState(balance, bookings);
    }

    private static CompoundTag writeLoan(Loan.SaveState state) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Kind", state.kind().name());
        tag.putLong("Principal", state.principal().cents());
        tag.putLong("Outstanding", state.outstanding().cents());
        tag.putDouble("Rate", state.rate());
        tag.putInt("TermDays", state.termDays());
        tag.putInt("FreeDays", state.freeDays());
        tag.putLong("Repayment", state.repayment().cents());
        tag.putInt("ElapsedDays", state.elapsedDays());
        return tag;
    }

    private static Loan.SaveState readLoan(CompoundTag tag) {
        return new Loan.SaveState(
                Loan.Kind.valueOf(tag.getString("Kind")),
                Money.ofCents(tag.getLong("Principal")),
                Money.ofCents(tag.getLong("Outstanding")),
                tag.getDouble("Rate"),
                tag.getInt("TermDays"),
                tag.getInt("FreeDays"),
                Money.ofCents(tag.getLong("Repayment")),
                tag.getInt("ElapsedDays"));
    }

    private static CompoundTag writeHistoryEntry(CompanyHistoryEntry entry) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Day", entry.day());
        tag.putLong("Result", entry.result().cents());
        tag.putLong("Revenue", entry.revenue().cents());

        ListTag costs = new ListTag();
        for (Map.Entry<BookingKind, Money> cost : entry.costsByKind().entrySet()) {
            CompoundTag costTag = new CompoundTag();
            costTag.putString("Kind", cost.getKey().name());
            costTag.putLong("Amount", cost.getValue().cents());
            costs.add(costTag);
        }
        tag.put("Costs", costs);
        return tag;
    }

    private static CompanyHistoryEntry readHistoryEntry(CompoundTag tag) {
        Map<BookingKind, Money> costsByKind = new EnumMap<>(BookingKind.class);
        for (Tag t : tag.getList("Costs", Tag.TAG_COMPOUND)) {
            CompoundTag costTag = (CompoundTag) t;
            costsByKind.put(BookingKind.valueOf(costTag.getString("Kind")), Money.ofCents(costTag.getLong("Amount")));
        }
        return new CompanyHistoryEntry(
                tag.getLong("Day"),
                Money.ofCents(tag.getLong("Result")),
                Money.ofCents(tag.getLong("Revenue")),
                costsByKind);
    }

    private static CompoundTag writeRequestBoard(RequestBoard.SaveState state) {
        CompoundTag tag = new CompoundTag();

        ListTag requests = new ListTag();
        for (Request.SaveState request : state.requests()) {
            CompoundTag requestTag = new CompoundTag();
            requestTag.putLong("Number", request.number());
            requestTag.putString("Product", request.product().value());
            requestTag.putInt("Quantity", request.quantity());
            requestTag.putInt("QuantityFactor", request.quantityFactor());
            requestTag.putDouble("Urgency", request.urgency());
            requestTag.putDouble("UnitPrice", request.unitPrice());
            requestTag.putLong("DeadlineTicks", request.deadlineTicks());
            requestTag.putLong("RemainingOfferTicks", request.remainingOfferTicks());
            requests.add(requestTag);
        }
        tag.put("Requests", requests);

        ListTag lostToday = new ListTag();
        for (Map.Entry<ItemId, Integer> entry : state.lostToday().entrySet()) {
            CompoundTag lostTag = new CompoundTag();
            lostTag.putString("Product", entry.getKey().value());
            lostTag.putInt("Count", entry.getValue());
            lostToday.add(lostTag);
        }
        tag.put("LostToday", lostToday);

        return tag;
    }

    private static RequestBoard.SaveState readRequestBoard(CompoundTag tag) {
        List<Request.SaveState> requests = new ArrayList<>();
        for (Tag t : tag.getList("Requests", Tag.TAG_COMPOUND)) {
            CompoundTag requestTag = (CompoundTag) t;
            requests.add(new Request.SaveState(
                    requestTag.getLong("Number"),
                    new ItemId(requestTag.getString("Product")),
                    requestTag.getInt("Quantity"),
                    requestTag.getInt("QuantityFactor"),
                    requestTag.getDouble("Urgency"),
                    requestTag.getDouble("UnitPrice"),
                    requestTag.getLong("DeadlineTicks"),
                    requestTag.getLong("RemainingOfferTicks")));
        }

        Map<ItemId, Integer> lostToday = new LinkedHashMap<>();
        for (Tag t : tag.getList("LostToday", Tag.TAG_COMPOUND)) {
            CompoundTag lostTag = (CompoundTag) t;
            lostToday.put(new ItemId(lostTag.getString("Product")), lostTag.getInt("Count"));
        }

        return new RequestBoard.SaveState(requests, lostToday);
    }

    private static CompoundTag writeOrderBook(OrderBook.SaveState state) {
        ListTag orders = new ListTag();
        for (Order.SaveState order : state.orders()) {
            orders.add(writeOrder(order));
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Orders", orders);
        return tag;
    }

    private static OrderBook.SaveState readOrderBook(CompoundTag tag) {
        List<Order.SaveState> orders = new ArrayList<>();
        for (Tag t : tag.getList("Orders", Tag.TAG_COMPOUND)) {
            orders.add(readOrder((CompoundTag) t));
        }
        return new OrderBook.SaveState(orders);
    }

    private static CompoundTag writeOrder(Order.SaveState state) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Number", state.number());
        tag.putString("Product", state.product().value());
        tag.putInt("Quantity", state.quantity());
        tag.putDouble("UnitPrice", state.unitPrice());
        tag.putDouble("Urgency", state.urgency());
        tag.putLong("DeadlineTicks", state.deadlineTicks());
        tag.putLong("GracePeriodTicks", state.gracePeriodTicks());
        tag.putLong("OriginRequestNumber", ((RequestOrigin) state.origin()).requestNumber());
        tag.putInt("DeliveredQuantity", state.deliveredQuantity());
        tag.putLong("RemainingDeadlineTicks", state.remainingDeadlineTicks());
        tag.putLong("RemainingGracePeriodTicks", state.remainingGracePeriodTicks());
        tag.putString("Phase", state.phase().name());
        return tag;
    }

    private static Order.SaveState readOrder(CompoundTag tag) {
        return new Order.SaveState(
                tag.getLong("Number"),
                new ItemId(tag.getString("Product")),
                tag.getInt("Quantity"),
                tag.getDouble("UnitPrice"),
                tag.getDouble("Urgency"),
                tag.getLong("DeadlineTicks"),
                tag.getLong("GracePeriodTicks"),
                new RequestOrigin(tag.getLong("OriginRequestNumber")), // TODO needs changing when more order origins get added
                tag.getInt("DeliveredQuantity"),
                tag.getLong("RemainingDeadlineTicks"),
                tag.getLong("RemainingGracePeriodTicks"),
                OrderPhase.valueOf(tag.getString("Phase")));
    }
}
