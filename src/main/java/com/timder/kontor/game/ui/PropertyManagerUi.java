package com.timder.kontor.game.ui;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SimpleBinding;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.SplitView;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.property.*;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.block.PropertyManagerActions;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.network.ToastPayload;
import com.timder.kontor.game.property.BiomeMeasurer;
import com.timder.kontor.game.property.BiomeSampling;
import com.timder.kontor.game.ui.element.UiButtons;
import com.timder.kontor.game.ui.element.UiContainer;
import com.timder.kontor.game.ui.element.UiLabels;
import com.timder.kontor.game.ui.element.WorldMapElement;
import com.timder.kontor.util.ComponentFormatting;
import com.timder.kontor.util.ObservableValue;
import dev.vfyjxf.taffy.style.FlexDirection;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public class PropertyManagerUi {

    private static final int MAP_RADIUS_CHUNKS = 8;

    public static ModularUI create(EmployeeDeskContext context, PropertyManagerActions actions) {
        var tabView = UiContainer.largeTabView();
        tabView.addTab(
                UiContainer.tab(Component.translatable("ui.createkontor.property_manager.chunk_map")),
                acquireTab(context, actions));

        var root = UiContainer.withHud(tabView, UiContainer.hudBox(context.company(), context.economy()));
        return new ModularUI(UI.of(root, StylesheetManager.GDP), context.player());
    }

    public static UIElement acquireTab(EmployeeDeskContext context, PropertyManagerActions actions) {
        UIElement content =
                new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100));

        ObservableValue<ChunkPos> selectedChunk = new ObservableValue<>(null);
        ObservableValue<Boolean> measurementActive = new ObservableValue<>(false);
        SimpleBinding<Boolean> measurementBinding = DataBindingBuilder.boolS2C(() -> BiomeMeasurer.isRunning(
                        BiomeSampling.dimensionId((ServerLevel) context.desk().getLevel())))
                .onRemoteSyncReceived(measurementActive::set)
                .build();
        content.addSyncValue(measurementBinding.getSyncValue());

        SplitView.Horizontal split = new SplitView.Horizontal();
        split.layout(layout -> layout.widthPercent(100).heightPercent(100));
        split.setPercentage(65);

        split.left(chunkMap(context, selectedChunk));
        split.right(selectedChunk(context, actions, selectedChunk));

        UIElement measurementError = UiContainer.headerWithSubtitle(
                UiLabels.h2(
                        Component.translatable("ui.createkontor.property_manager.measurement_active"),
                        Horizontal.CENTER),
                UiLabels.error(
                        Component.translatable("ui.createkontor.property_manager.measurement_active.sub"),
                        Horizontal.CENTER));
        content.addChildren(split, measurementError);

        measurementActive.addListener(() -> {
            split.setDisplay(!measurementActive.get());
            measurementError.setDisplay(measurementActive.get());
        });
        measurementActive.runListenersOnce();

        return content;
    }

    public static UIElement chunkMap(EmployeeDeskContext context, ObservableValue<ChunkPos> selectedChunk) {
        UIElement content = new UIElement()
                .layout(layout -> layout.widthPercent(100).heightPercent(100))
                .style(style -> style.background(Sprites.BORDER_DARK))
                .layout(layout -> layout.paddingAll(8));

        ObservableValue<ChunkOwnership> ownership =
                new ObservableValue<>(new ChunkOwnership(Set.of(), Set.of(), Set.of()));
        SimpleBinding<Tag> ownershipBinding = DataBindingBuilder.tagS2C(() -> chunkOwnershipToTag(context))
                .onRemoteSyncReceived(tag -> ownership.set(tagToChunkOwnership(tag)))
                .build();
        content.addSyncValue(ownershipBinding.getSyncValue());

        ownership.addListener(() -> {
            content.clearAllChildren();
            WorldMapElement map = new WorldMapElement(MAP_RADIUS_CHUNKS)
                    .setCenter(
                            context.desk().getBlockPos().getX(),
                            context.desk().getBlockPos().getZ())
                    .setSliceY(context.desk().getBlockPos().getY());
            map.layout(layout -> layout.widthPercent(100).heightPercent(100));
            map.onSelectedChunkChanged(selectedChunk::set);

            map.setChunkColor(pos -> {
                if (ownership.get().own().contains(pos)) {
                    return 0x5000FF00;
                }
                if (ownership.get().cancelled().contains(pos)) {
                    return 0x50F6FF00;
                }
                if (ownership.get().foreign().contains(pos)) {
                    return 0x50FF0000;
                }
                return 0;
            });

            content.addChild(map);
        });
        ownership.runListenersOnce();

        return content;
    }

    public static UIElement selectedChunk(
            EmployeeDeskContext context, PropertyManagerActions actions, ObservableValue<ChunkPos> selectedChunk) {
        ScrollerView content = (ScrollerView) UiContainer.tabScroller()
                .layout(layout -> layout.widthPercent(100).heightPercent(100))
                .style(style -> style.background(Sprites.BORDER_DARK))
                .layout(layout -> layout.paddingAll(8));

        ChunkPos[] selected = {null}; // server
        selectedChunk.addListener(() -> {
            ChunkPos chunk = selectedChunk.get();
            CompoundTag tag = new CompoundTag();
            tag.putInt("X", chunk.x);
            tag.putInt("Z", chunk.z);
            content.sendMessage("c2s_select_chunk", tag);
        });
        content.onMessage("c2s_select_chunk", tag -> selected[0] = new ChunkPos(tag.getInt("X"), tag.getInt("Z")));

        content.onMessage("c2s_acquire_chunk", tag -> {
            if (selected[0] == null) return;
            PropertyRules.Mode mode = tag.getBoolean("Buy") ? PropertyRules.Mode.BUY : PropertyRules.Mode.RENT;
            ToastPayload.send(context.player(), actions.acquire(selected[0], mode));
        });

        content.onMessage("c2s_cancel_rent", tag -> {
            if (selected[0] == null) return;
            ToastPayload.send(context.player(), actions.cancel(selected[0]));
        });

        content.onMessage("c2s_sell_chunk", tag -> {
            if (selected[0] == null) return;
            ToastPayload.send(context.player(), actions.sell(selected[0]));
        });

        SimpleBinding<Tag> chunkBinding = DataBindingBuilder.tagS2C(() -> chunkInfoToTag(context, selected[0]))
                .onRemoteSyncReceived(tag -> showChunk(content, tagToChunkInfo(tag)))
                .build();
        content.addSyncValue(chunkBinding.getSyncValue());

        return content;
    }

    private static void showChunk(ScrollerView content, @Nullable ChunkInfo info) {
        content.clearAllScrollViewChildren();
        if (info == null) return;

        // TODO Block any actions here when BiomeMeasurer.isRunning() is true for current dimension

        content.addScrollViewChildren(UiLabels.h2(
                Component.translatable(
                        "ui.createkontor.property_manager.selected_property",
                        info.chunk().toString()),
                Horizontal.LEFT));

        content.addScrollViewChildren(UiLabels.labelValueNoWrap(
                Component.translatable("ui.createkontor.property_manager.biome"),
                Component.translatable(Util.makeDescriptionId(
                        "biome", ResourceLocation.parse(info.biome().value())))));
        content.addScrollViewChildren(UiLabels.labelValueNoWrap(
                Component.translatable("ui.createkontor.property_manager.biome_rarity"),
                Component.translatable("enum.createkontor.biome_rarity."
                        + info.rarity().toString().toLowerCase())));
        content.addScrollViewChildren(UiLabels.labelValueNoWrap(
                Component.translatable("ui.createkontor.property_manager.value"),
                info.price() == null
                        ? Component.translatable("ui.createkontor.property_manager.unpriceable")
                        : Component.literal(info.resaleValue().toString())));

        Component ownership = Component.literal("-");
        switch (info.ownership()) {
            case FREE -> ownership = Component.translatable("ui.createkontor.property_manager.free");
            case FOREIGN -> ownership = Component.translatable("ui.createkontor.property_manager.occupied");
            case OWNED -> ownership = Component.translatable("ui.createkontor.property_manager.owned");
            case RENTED -> ownership = Component.translatable("ui.createkontor.property_manager.rented");
            case RENTED_CANCELLED ->
                ownership = Component.translatable("ui.createkontor.property_manager.rented_cancelled");
        }
        content.addScrollViewChildren(UiLabels.labelValueNoWrap(
                Component.translatable("ui.createkontor.property_manager.status"), ownership));

        content.addScrollViewChildren(UiLabels.labelValueNoWrap(
                Component.translatable("ui.createkontor.property_manager.can_rent"),
                info.price() == null
                        ? Component.translatable("ui.createkontor.property_manager.unpriceable")
                        : Component.translatable("ui.createkontor.property_manager.preview."
                                + info.rentStatus().toString().toLowerCase())));
        content.addScrollViewChildren(UiLabels.labelValueNoWrap(
                Component.translatable("ui.createkontor.property_manager.daily_rent"),
                info.price() == null
                        ? Component.translatable("ui.createkontor.property_manager.unpriceable")
                        : ComponentFormatting.moneyColored(info.dailyRent())));

        content.addScrollViewChildren(UiLabels.labelValueNoWrap(
                Component.translatable("ui.createkontor.property_manager.can_buy"),
                info.price() == null
                        ? Component.translatable("ui.createkontor.property_manager.unpriceable")
                        : Component.translatable("ui.createkontor.property_manager.preview."
                                + info.buyStatus().toString().toLowerCase())));
        content.addScrollViewChildren(UiLabels.labelValueNoWrap(
                Component.translatable("ui.createkontor.property_manager.buy_price"),
                info.price() == null
                        ? Component.translatable("ui.createkontor.property_manager.unpriceable")
                        : ComponentFormatting.moneyColored(info.price())));

        if ((info.ownership() == Ownership.FREE || info.ownership() == Ownership.RENTED_CANCELLED)
                && info.price() != null) {
            UIElement buttons = new UIElement()
                    .layout(layout -> layout.widthPercent(100)
                            .flexDirection(FlexDirection.ROW)
                            .gapAll(4));

            Button rent = UiButtons.wrapping(UiButtons.primary(Component.translatable(
                    "ui.createkontor.property_manager.rent", ComponentFormatting.moneyColored(info.dailyRent()))));
            rent.layout(layout -> layout.flexBasis(0).flexGrow(1));
            rent.setActive(info.canRent());
            rent.setOnClick(e -> {
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("Buy", false);
                content.sendMessage("c2s_acquire_chunk", tag);
            });
            buttons.addChild(rent);

            Button buy = UiButtons.wrapping(UiButtons.primary(Component.translatable(
                    "ui.createkontor.property_manager.buy", ComponentFormatting.moneyColored(info.price()))));
            buy.layout(layout -> layout.flexBasis(0).flexGrow(1));
            buy.setActive(info.canBuy());
            buy.setOnClick(e -> {
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("Buy", true);
                content.sendMessage("c2s_acquire_chunk", tag);
            });

            buttons.addChild(buy);
            content.addScrollViewChildren(buttons);
        }

        if (info.ownership() == Ownership.RENTED) {
            UIElement buttons = new UIElement()
                    .layout(layout -> layout.widthPercent(100)
                            .flexDirection(FlexDirection.ROW)
                            .gapAll(4));

            Button cancel = UiButtons.wrapping(
                    UiButtons.critical(Component.translatable("ui.createkontor.property_manager.cancel")));
            cancel.layout(layout -> layout.flexBasis(0).flexGrow(1));
            cancel.setOnClick(e -> content.sendMessage("c2s_cancel_rent"));
            buttons.addChild(cancel);

            Button buy = UiButtons.wrapping(UiButtons.primary(Component.translatable(
                    "ui.createkontor.property_manager.buy", ComponentFormatting.moneyColored(info.price()))));
            buy.layout(layout -> layout.flexBasis(0).flexGrow(1));
            buy.setActive(info.canBuy());
            buy.setOnClick(e -> {
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("Buy", true);
                content.sendMessage("c2s_acquire_chunk", tag);
            });
            buttons.addChild(buy);
            content.addScrollViewChildren(buttons);
        }

        if (info.ownership() == Ownership.OWNED) {
            UIElement buttons = new UIElement()
                    .layout(layout -> layout.widthPercent(100)
                            .flexDirection(FlexDirection.ROW)
                            .gapAll(4));

            Button sell = UiButtons.wrapping(UiButtons.primary(Component.translatable(
                    "ui.createkontor.property_manager.sell", ComponentFormatting.moneyColored(info.resaleValue()))));
            sell.layout(layout -> layout.flexBasis(0).flexGrow(1));
            sell.setOnClick(e -> content.sendMessage("c2s_sell_chunk"));
            buttons.addChild(sell);
            content.addScrollViewChildren(buttons);
        }
    }

    private static Tag chunkInfoToTag(EmployeeDeskContext context, @Nullable ChunkPos chunk) {
        CompoundTag tag = new CompoundTag();
        if (chunk == null || context.company() == null || !(context.player().level() instanceof ServerLevel level))
            return tag;

        CompanyParams companyParams =
                CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
        PropertyParams params = companyParams.property();
        Collection<Company> companies =
                CompanySavedData.get(level.getServer()).getRegistry().all();

        BiomeId biome = BiomeSampling.biomeAt(level, chunk);
        ChunkKey key = new ChunkKey(BiomeSampling.dimensionId(level), chunk.x, chunk.z);
        Optional<Money> price = PropertyRules.priceOf(key, biome, params);

        tag.putInt("X", chunk.x);
        tag.putInt("Z", chunk.z);
        tag.putString("Biome", biome.value());
        tag.putString(
                "Rarity", params.frequenciesOf(key.dimension()).rarity(biome).name());
        PropertyHolding holding = context.company().property(key).orElse(null);
        Ownership ownership;
        if (holding != null) {
            ownership = holding.isOwned()
                    ? Ownership.OWNED
                    : holding.cancelled() ? Ownership.RENTED_CANCELLED : Ownership.RENTED;
        } else {
            ownership = PropertyRules.ownerOf(companies, key).isPresent() ? Ownership.FOREIGN : Ownership.FREE;
        }
        tag.putString("Ownership", ownership.name());
        tag.putString(
                "BuyStatus",
                PropertyRules.preview(context.company(), companies, key, biome, PropertyRules.Mode.BUY, companyParams)
                        .status()
                        .name());
        tag.putString(
                "RentStatus",
                PropertyRules.preview(context.company(), companies, key, biome, PropertyRules.Mode.RENT, companyParams)
                        .status()
                        .name());
        price.ifPresent(p -> {
            tag.putLong("Price", p.cents());
            tag.putLong("ResaleValue", params.resaleValue(p).cents());
            tag.putLong("DailyRent", params.dailyRent(p).cents());
        });
        return tag;
    }

    public enum Ownership {
        FREE,
        OWNED,
        RENTED,
        RENTED_CANCELLED,
        FOREIGN
    }

    public record ChunkInfo(
            ChunkPos chunk,
            BiomeId biome,
            BiomeFrequencies.Rarity rarity,
            Ownership ownership,
            @Nullable Money price,
            @Nullable Money resaleValue,
            @Nullable Money dailyRent,
            PropertyRules.AcquireResult.Status buyStatus,
            PropertyRules.AcquireResult.Status rentStatus) {

        public boolean canBuy() {
            return buyStatus == PropertyRules.AcquireResult.Status.BOUGHT
                    || buyStatus == PropertyRules.AcquireResult.Status.CONVERTED;
        }

        public boolean canRent() {
            return rentStatus == PropertyRules.AcquireResult.Status.RENTED
                    || rentStatus == PropertyRules.AcquireResult.Status.REACTIVATED;
        }
    }

    @Nullable
    private static ChunkInfo tagToChunkInfo(Tag tag) {
        CompoundTag data = (CompoundTag) tag;
        if (!data.contains("Biome")) return null;
        return new ChunkInfo(
                new ChunkPos(data.getInt("X"), data.getInt("Z")),
                new BiomeId(data.getString("Biome")),
                BiomeFrequencies.Rarity.valueOf(data.getString("Rarity")),
                Ownership.valueOf(data.getString("Ownership")),
                data.contains("Price") ? Money.ofCents(data.getLong("Price")) : null,
                data.contains("ResaleValue") ? Money.ofCents(data.getLong("ResaleValue")) : null,
                data.contains("DailyRent") ? Money.ofCents(data.getLong("DailyRent")) : null,
                PropertyRules.AcquireResult.Status.valueOf(data.getString("BuyStatus")),
                PropertyRules.AcquireResult.Status.valueOf(data.getString("RentStatus")));
    }

    private static Tag chunkOwnershipToTag(EmployeeDeskContext context) {
        CompoundTag tag = new CompoundTag();
        if (!(context.player().level() instanceof ServerLevel level) || context.company() == null) return tag;

        DimensionId dimension = BiomeSampling.dimensionId(level);
        ChunkPos desk = new ChunkPos(context.holder().pos);
        List<Long> own = new ArrayList<>();
        List<Long> cancelled = new ArrayList<>();
        List<Long> foreign = new ArrayList<>();
        for (Company company :
                CompanySavedData.get(level.getServer()).getRegistry().all()) {
            boolean isOwn = company.id().equals(context.company().id());
            for (PropertyHolding holding : company.properties()) {
                ChunkKey chunk = holding.chunk();
                if (!chunk.dimension().equals(dimension)
                        || Math.abs(chunk.x() - desk.x) > MAP_RADIUS_CHUNKS
                        || Math.abs(chunk.z() - desk.z) > MAP_RADIUS_CHUNKS) {
                    continue;
                }

                if (isOwn) {
                    if (holding.cancelled()) {
                        cancelled.add(ChunkPos.asLong(chunk.x(), chunk.z()));
                    } else {
                        own.add(ChunkPos.asLong(chunk.x(), chunk.z()));
                    }
                } else {
                    foreign.add(ChunkPos.asLong(chunk.x(), chunk.z()));
                }
            }
        }
        tag.putLongArray("Own", own);
        tag.putLongArray("Cancelled", cancelled);
        tag.putLongArray("Foreign", foreign);
        return tag;
    }

    public record ChunkOwnership(Set<ChunkPos> own, Set<ChunkPos> cancelled, Set<ChunkPos> foreign) {}

    private static ChunkOwnership tagToChunkOwnership(Tag tag) {
        CompoundTag data = (CompoundTag) tag;
        return new ChunkOwnership(
                toChunks(data.getLongArray("Own")),
                toChunks(data.getLongArray("Cancelled")),
                toChunks(data.getLongArray("Foreign")));
    }

    private static Set<ChunkPos> toChunks(long[] packed) {
        Set<ChunkPos> chunks = new HashSet<>();
        for (long value : packed) {
            chunks.add(new ChunkPos(value));
        }
        return Set.copyOf(chunks);
    }
}
