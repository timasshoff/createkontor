package com.timder.kontor.game.block.employee;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.employee.Employee;
import com.timder.kontor.core.company.employee.EmployeeRules;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.EmployeeContracts;
import com.timder.kontor.game.block.company.AbstractCompanyBlockEntity;
import com.timder.kontor.util.ComponentFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

public class EmployeeDeskBlockEntity extends AbstractCompanyBlockEntity {

    public static final String RESOURCE_KEY = "employee_desk";

    public static final int CHECK_INTERVAL_TICKS = 20;
    public static final int EMPTY_CHECKS_BEFORE_DISMISSAL = 5;

    private static final long NO_EMPLOYEE = 0L;

    private static final String TAG_EMPLOYEE = "EmployeeNumber";
    private static final String TAG_SEATED = "Seated";
    private static final String TAG_STATUS = "Status";
    private static final String TAG_SALARY = "SalaryCents";
    private static final String TAG_BONUS = "HireBonusCents";

    public enum Status {
        NO_COMPANY,
        INACTIVE,
        VACANT,
        NO_FREE_SLOT,
        NOT_OPERATIONAL,
        NOT_AFFORDABLE,
        EMPLOYED
    }

    private long employeeNumber = NO_EMPLOYEE;
    private int emptyChecks = 0;

    private boolean seated = false;
    private Status status = Status.NO_COMPANY;
    private long salaryCents = 0;
    private long bonusCents = 0;

    public EmployeeDeskBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(CHECK_INTERVAL_TICKS);
    }

    @Override
    protected @Nullable String boundResourceKey() {
        return RESOURCE_KEY;
    }

    @Override
    protected boolean loadsChunks() {
        return true;
    }

    @Override
    protected void onCompanyChanged(@Nullable CompanyId oldId, @Nullable CompanyId newId) {
        if (oldId != null && employeeNumber != NO_EMPLOYEE && level instanceof ServerLevel serverLevel) {
            CompanySavedData data = CompanySavedData.get(serverLevel.getServer());
            long day = EconomySavedData.get(serverLevel.getServer()).getEconomy().currentDay();
            CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));
            data.getRegistry().get(oldId).ifPresent(company -> {
                if (company.employee(employeeNumber).isPresent()) {
                    Employee employee = EmployeeRules.dismiss(company, employeeNumber, day);
                    EmployeeContracts.ended(serverLevel.getServer(), company, employee, day, params);
                    data.setDirty();
                }
            });
        }
        employeeNumber = NO_EMPLOYEE;
        emptyChecks = 0;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level instanceof ServerLevel serverLevel) {
            checkSeat(serverLevel);
        }
    }

    private void checkSeat(ServerLevel level) {
        boolean nowSeated = isAdultVillagerSeated(level);
        CompanyId companyId = getCompanyId();
        CompanySavedData data = CompanySavedData.get(level.getServer());
        Company company = companyId == null ? null : data.getRegistry().get(companyId).orElse(null);

        if (company == null) {
            forgetEmployee();
            show(nowSeated, Status.NO_COMPANY, 0, 0);
            return;
        }

        if (employeeNumber != NO_EMPLOYEE && company.employee(employeeNumber).isEmpty()) {
            forgetEmployee();
        }

        if (!company.isActive()) {
            show(nowSeated, Status.INACTIVE, 0, 0);
            return;
        }

        long day = EconomySavedData.get(level.getServer()).getEconomy().currentDay();
        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));

        if (nowSeated) {
            emptyChecks = 0;
            if (employeeNumber == NO_EMPLOYEE) {
                Status hireStatus = tryHire(company, day, params);
                if (hireStatus != Status.EMPLOYED) {
                    showOffer(true, hireStatus, company, day, params);
                    return;
                }
                data.setDirty();
            } else {
                Employee before = company.employee(employeeNumber).orElseThrow();
                if (!before.present() || before.lastReportDay() != day) {
                    EmployeeRules.reportPresence(company, employeeNumber, true, day);
                    data.setDirty();
                }
            }
        } else if (employeeNumber != NO_EMPLOYEE) {
            emptyChecks++;
            if (emptyChecks >= EMPTY_CHECKS_BEFORE_DISMISSAL) {
                Employee employee = EmployeeRules.dismiss(company, employeeNumber, day);
                EmployeeContracts.ended(level.getServer(), company, employee, day, params);
                data.setDirty();
                forgetEmployee();
            }
        }

        if (employeeNumber == NO_EMPLOYEE) {
            showOffer(nowSeated, Status.VACANT, company, day, params);
        } else {
            show(nowSeated, Status.EMPLOYED, salaryOf(company, params).cents(), 0);
        }
    }

    private Status tryHire(Company company, long day, CompanyParams params) {
        if (!(getBlockState().getBlock() instanceof AbstractEmployeeDeskBlock desk)) {
            return Status.VACANT;
        }
        if (EmployeeRules.freeSlots(company, params) <= 0) {
            return Status.NO_FREE_SLOT;
        }
        if (!company.isOperational()) {
            return Status.NOT_OPERATIONAL;
        }
        Money bonus = EmployeeRules.hireCost(company, desk.roleSpec(), day, params);
        if (bonus.isPositive() && !company.canSpend(bonus, params)) {
            return Status.NOT_AFFORDABLE;
        }
        Employee employee = EmployeeRules.hire(company, desk.roleSpec(), day, params);
        employeeNumber = employee.number();
        emptyChecks = 0;
        setChanged();
        return Status.EMPLOYED;
    }

    private Money salaryOf(Company company, CompanyParams params) {
        Employee employee = company.employee(employeeNumber).orElseThrow();
        return EmployeeRules.dailySalary(employee.baseSalary(), company.legalForm(params));
    }

    private void forgetEmployee() {
        if (employeeNumber != NO_EMPLOYEE) {
            employeeNumber = NO_EMPLOYEE;
            setChanged();
        }
        emptyChecks = 0;
    }

    private boolean isAdultVillagerSeated(Level level) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos seatPos = worldPosition.relative(side);
            for (SeatEntity seat : level.getEntitiesOfClass(SeatEntity.class, new AABB(seatPos))) {
                if (hasAdultVillager(seat.getPassengers()) && adjacentEmployeeDesks(level, seatPos) == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasAdultVillager(List<Entity> passengers) {
        for (Entity passenger : passengers) {
            if (passenger instanceof Villager villager && !villager.isBaby()) {
                return true;
            }
        }
        return false;
    }

    private static int adjacentEmployeeDesks(Level level, BlockPos seatPos) {
        int desks = 0;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(seatPos.relative(side)).getBlock() instanceof AbstractEmployeeDeskBlock) {
                desks++;
            }
        }
        return desks;
    }

    private void showOffer(boolean seated, Status status, Company company, long day, CompanyParams params) {
        if (!(getBlockState().getBlock() instanceof AbstractEmployeeDeskBlock desk)) {
            show(seated, status, 0, 0);
            return;
        }
        Money salary = EmployeeRules.dailySalary(desk.roleSpec().baseSalary(), company.legalForm(params));
        Money bonus = EmployeeRules.hireCost(company, desk.roleSpec(), day, params);
        show(seated, status, salary.cents(), bonus.cents());
    }

    private void show(boolean seated, Status status, long salaryCents, long bonusCents) {
        if (this.seated == seated && this.status == status && this.salaryCents == salaryCents && this.bonusCents == bonusCents) {
            return;
        }
        this.seated = seated;
        this.status = status;
        this.salaryCents = salaryCents;
        this.bonusCents = bonusCents;
        notifyUpdate();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        if (getBlockState().getBlock() instanceof AbstractEmployeeDeskBlock desk) {
            CreateLang.builder()
                    .add(Component.translatable("goggle.createkontor.employee_desk.role",
                            ComponentFormatting.highlightStandardTranslatable(desk.roleTranslationKey())).withStyle(ComponentFormatting.DEFAULT))
                    .forGoggles(tooltip, 1);
        }

        if (!seated) {
            CreateLang.builder()
                    .add(ComponentFormatting.standardTranslatable( "goggle.createkontor.employee_desk.seat_empty"))
                    .forGoggles(tooltip, 1);
        }

        switch (status) {
            case EMPLOYED -> {
                CreateLang.builder()
                        .add(Component.translatable("goggle.createkontor.employee_desk.employed",
                                ComponentFormatting.highlightStandard("#" + employeeNumber)).withStyle(ComponentFormatting.DEFAULT))
                        .forGoggles(tooltip, 1);
                CreateLang.builder()
                        .add(Component.translatable("goggle.createkontor.employee_desk.salary",
                                ComponentFormatting.highlightStandard(Money.ofCents(salaryCents).toString())).withStyle(ComponentFormatting.DEFAULT))
                        .forGoggles(tooltip, 2);
            }
            case VACANT -> {
                line(tooltip, ComponentFormatting.standardTranslatable("goggle.createkontor.employee_desk.vacant"));
                offer(tooltip);
            }
            case NO_FREE_SLOT -> {
                line(tooltip, ComponentFormatting.errorTranslatable("goggle.createkontor.employee_desk.no_free_slot"));
                offer(tooltip);
            }
            case NOT_OPERATIONAL -> {
                line(tooltip, ComponentFormatting.errorTranslatable("goggle.createkontor.employee_desk.not_operational"));
                offer(tooltip);
            }
            case NOT_AFFORDABLE -> {
                line(tooltip, ComponentFormatting.errorTranslatable("goggle.createkontor.employee_desk.not_affordable"));
                offer(tooltip);
            }
            case INACTIVE -> line(tooltip, ComponentFormatting.standardTranslatable("goggle.createkontor.employee_desk.inactive"));
            case NO_COMPANY -> {}
        }

        return true;
    }

    private static void line(List<Component> tooltip, Component text) {
        CreateLang.builder().add(text).forGoggles(tooltip, 1);
    }

    private void offer(List<Component> tooltip) {
        CreateLang.builder()
                .add(Component.translatable("goggle.createkontor.employee_desk.offer_salary",
                        ComponentFormatting.highlightStandard(Money.ofCents(salaryCents).toString())).withStyle(ComponentFormatting.DEFAULT))
                .forGoggles(tooltip, 2);
        CreateLang.builder()
                .add(Component.translatable("goggle.createkontor.employee_desk.offer_bonus",
                        ComponentFormatting.highlightStandard(Money.ofCents(bonusCents).toString())).withStyle(ComponentFormatting.DEFAULT))
                .forGoggles(tooltip, 2);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putLong(TAG_EMPLOYEE, employeeNumber);
        if (clientPacket) {
            tag.putBoolean(TAG_SEATED, seated);
            tag.putString(TAG_STATUS, status.name());
            tag.putLong(TAG_SALARY, salaryCents);
            tag.putLong(TAG_BONUS, bonusCents);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        employeeNumber = tag.getLong(TAG_EMPLOYEE);
        if (clientPacket) {
            seated = tag.getBoolean(TAG_SEATED);
            try {
                status = Status.valueOf(tag.getString(TAG_STATUS));
            } catch (IllegalArgumentException e) {
                status = Status.NO_COMPANY;
            }
            salaryCents = tag.getLong(TAG_SALARY);
            bonusCents = tag.getLong(TAG_BONUS);
        }
    }
}
