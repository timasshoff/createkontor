package com.timder.kontor.game.block.power;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.*;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.power.PowerKind;
import com.timder.kontor.core.company.power.PowerRules;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.block.company.CompanyBlockSupport;
import com.timder.kontor.game.block.company.CompanyBound;
import com.timder.kontor.util.ComponentFormatting;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PowerOutletBlockEntity extends GeneratingKineticBlockEntity implements CompanyBound {

    private static final int DEFAULT_RPM = 16;
    private static final int DEFAULT_SU = 512;

    private static final int FALLBACK_MAX_SU = 1_024;
    private static final int FALLBACK_MAX_RPM = 256;

    private static final int FLUSH_INTERVAL = 20;

    private StressUnitsBehaviour stressUnits;
    private ScrollValueBehaviour rotationSpeed;
    private long pendingSuTicks;

    @Nullable
    private CompanyId companyId;

    private String companyName = "";

    public PowerOutletBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        int maxSu = CompanyConfig.SPEC.isLoaded() ? CompanyConfig.ANDESITE_OUTLET_MAX_SU.get() : FALLBACK_MAX_SU;
        stressUnits = new StressUnitsBehaviour(this, maxSu);
        stressUnits.value = Math.min(DEFAULT_SU, maxSu);
        stressUnits.withCallback(value -> updateGeneratedRotation());
        behaviours.add(stressUnits);

        int maxRpm = CompanyConfig.SPEC.isLoaded() ? CompanyConfig.OUTLET_MAX_RPM.get() : FALLBACK_MAX_RPM;
        rotationSpeed = new KineticScrollValueBehaviour(
                Component.translatable("ui.createkontor.power_outlet.rotation_speed"),
                this,
                new CenteredSideValueBoxTransform((state, direction) ->
                        direction == state.getValue(PowerOutletBlock.FACING).getOpposite()));
        rotationSpeed.between(-maxRpm, maxRpm);
        rotationSpeed.value = Math.min(DEFAULT_RPM, maxRpm);
        rotationSpeed.withCallback(value -> updateGeneratedRotation());
        behaviours.add(rotationSpeed);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!hasSource() || Math.abs(getGeneratedSpeed()) > Math.abs(getTheoreticalSpeed())) {
            updateGeneratedRotation();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide() || companyId == null) {
            return;
        }
        if (getGeneratedSpeed() != 0) {
            pendingSuTicks += stressUnits.getValue();
        }
        if ((level.getGameTime() & FLUSH_INTERVAL) == 0) {
            flushPower();
        }
    }

    private void flushPower() {
        if (pendingSuTicks == 0 || companyId == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        CompanySavedData data = CompanySavedData.get(serverLevel.getServer());
        Company company = data.getRegistry().get(companyId).orElse(null);
        if (company != null) {
            PowerRules.add(company, PowerKind.FIXED, pendingSuTicks);
            data.setDirty();
        }
        pendingSuTicks = 0;
    }

    @Override
    public void onChunkUnloaded() {
        flushPower();
        super.onChunkUnloaded();
    }

    @Override
    public float getGeneratedSpeed() {
        BlockState state = getBlockState();
        if (rotationSpeed == null
                || !(state.getBlock() instanceof PowerOutletBlock)
                || !state.getValue(PowerOutletBlock.POWERED)) {
            return 0;
        }
        return convertToDirection(rotationSpeed.getValue(), state.getValue(PowerOutletBlock.FACING));
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = 0;
        if (stressUnits != null && rotationSpeed != null && getGeneratedSpeed() != 0) {
            capacity = capacityPerRpm(stressUnits.getValue(), rotationSpeed.getValue());
        }
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Nullable
    @Override
    public CompanyId getCompanyId() {
        return companyId;
    }

    @Override
    public void setCompanyId(@Nullable CompanyId id) {
        if (Objects.equals(companyId, id)) {
            return;
        }
        flushPower();
        updateBoundResourceCount(companyId, id);
        companyId = id;
        companyName = resolveCompanyName();
        CompanyBlockSupport.afterCompanyIdChanged(this);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level != null && !level.isClientSide()) {
            String resolved = resolveCompanyName();
            if (!resolved.equals(companyName)) {
                companyName = resolved;
                notifyUpdate();
            }
        }
    }

    @Override
    public void remove() {
        super.remove();
        setCompanyId(null);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (getGeneratedSpeed() != 0) {
            tooltip.add(Component.literal(""));
        }

        if (companyId == null || companyName.isEmpty()) {
            CreateLang.builder(CreateKontor.MODID)
                    .add(ComponentFormatting.errorTranslatable(CompanyBlockSupport.GOGGLE_COMPANY_BLOCK_UNBOUND))
                    .forGoggles(tooltip, 0);
            return true;
        }

        CreateLang.builder()
                .add(Component.translatable(CompanyBlockSupport.GOGGLE_COMPANY_BLOCK)
                        .withStyle(ChatFormatting.WHITE))
                .forGoggles(tooltip, 0);

        CreateLang.builder()
                .add(ComponentFormatting.highlightStandard(companyName))
                .forGoggles(tooltip, 1);

        CreateLang.builder()
                .add(ComponentFormatting.standardTranslatable("goggle.createkontor.power_outlet.daily_cost"))
                .forGoggles(tooltip, 0);
        Money perDay = getGeneratedSpeed() <= 0
                ? Money.ZERO
                : PowerRules.cost(
                        (long) stressUnits.getValue() * Economy.DAY_LENGTH,
                        PowerKind.FIXED,
                        CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()))
                                .power());
        CreateLang.builder(CreateKontor.MODID)
                .add(Component.translatable(
                                "goggle.createkontor.power_outlet.daily_cost.value",
                                ComponentFormatting.moneyNegative(perDay))
                        .withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        return true;
    }

    private void updateBoundResourceCount(@Nullable CompanyId oldId, @Nullable CompanyId newId) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        CompanySavedData data = CompanySavedData.get(serverLevel.getServer());
        CompanyRegistry registry = data.getRegistry();
        boolean changedAny = false;
        if (oldId != null) {
            Company old = registry.get(oldId).orElse(null);
            if (old != null) {
                old.unbindResource(PowerOutletBlock.RESOURCE_KEY);
                changedAny = true;
            }
        }
        if (newId != null) {
            Company next = registry.get(newId).orElse(null);
            if (next != null) {
                next.bindResource(PowerOutletBlock.RESOURCE_KEY);
                changedAny = true;
            }
        }
        if (changedAny) {
            data.setDirty();
        }
    }

    private String resolveCompanyName() {
        if (companyId == null || !(level instanceof ServerLevel serverLevel)) {
            return "";
        }
        return CompanySavedData.get(serverLevel.getServer())
                .getRegistry()
                .get(companyId)
                .map(Company::name)
                .orElse("");
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        CompanyBlockSupport.writeCompanyId(companyId, tag);
        CompanyBlockSupport.writeCompanyName(companyName, tag);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        companyId = CompanyBlockSupport.readCompanyId(tag);
        companyName = CompanyBlockSupport.readCompanyName(tag);
    }

    private static float capacityPerRpm(int stressUnits, int rpm) {
        if (stressUnits <= 0 || rpm <= 0) {
            return 0f;
        }
        float capacity = (float) stressUnits / rpm;
        while (capacity * rpm < stressUnits) {
            capacity = Math.nextUp(capacity);
        }
        return capacity;
    }

    // Necessary because large Max SU values would make the value bar overflow the screen
    private static class StressUnitsBehaviour extends ScrollValueBehaviour {

        private static final BehaviourType<StressUnitsBehaviour> TYPE = new BehaviourType<>("outlet_stress_units");
        private static final String NBT_KEY = "StressUnits";
        private static final int MAX_COLUMNS = 256;

        private final int step;
        private final int columns;

        StressUnitsBehaviour(SmartBlockEntity be, int maximum) {
            super(
                    Component.translatable("ui.createkontor.power_outlet.stress_units"),
                    be,
                    new CenteredSideValueBoxTransform((state, direction) -> direction.getAxis()
                            != state.getValue(PowerOutletBlock.FACING).getAxis()));
            between(1, maximum);
            step = (maximum + MAX_COLUMNS - 1) / MAX_COLUMNS;
            columns = (maximum + step - 1) / step;
        }

        private int stressUnitsOf(int column) {
            return Mth.clamp(column * step, 1, max);
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            return new ValueSettingsBoard(
                    label,
                    columns,
                    Math.min(32, columns),
                    ImmutableList.of(Component.literal("SU")),
                    new ValueSettingsFormatter(settings -> CreateLang.number(stressUnitsOf(settings.value()))
                            .add(CreateLang.text(" SU"))
                            .component()));
        }

        @Override
        public ValueSettings getValueSettings() {
            return new ValueSettings(0, value >= max ? columns : (value + step / 2) / step);
        }

        @Override
        public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
            if (valueSetting.equals(getValueSettings())) {
                return;
            }
            setValue(stressUnitsOf(valueSetting.value()));
            playFeedbackSound(this);
        }

        @Override
        public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
            CompoundTag own = new CompoundTag();
            super.write(own, registries, clientPacket);
            nbt.put(NBT_KEY, own);
        }

        @Override
        public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
            if (!nbt.contains(NBT_KEY)) {
                return;
            }
            super.read(nbt.getCompound(NBT_KEY), registries, clientPacket);
            value = Mth.clamp(value, 1, max);
        }

        @Override
        public int netId() {
            return 1;
        }

        @Override
        public BehaviourType<?> getType() {
            return TYPE;
        }
    }
}
