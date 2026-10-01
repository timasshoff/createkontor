package com.timder.kontor.game.block.employee;

import com.simibubi.create.foundation.block.IBE;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.core.company.employee.RoleSpec;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.game.block.company.AbstractCompanyBlock;
import com.timder.kontor.game.chunk.ChunkLoadingDesk;
import com.timder.kontor.registry.KontorBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractEmployeeDeskBlock extends AbstractCompanyBlock implements IBE<EmployeeDeskBlockEntity>, ChunkLoadingDesk {

    private final RoleId role;
    private final Supplier<Integer> baseSalaryInDollars;

    protected AbstractEmployeeDeskBlock(Properties properties, RoleId role, Supplier<Integer> baseSalaryInDollars) {
        super(properties);
        this.role = Objects.requireNonNull(role, "role must not be null.");
        this.baseSalaryInDollars = Objects.requireNonNull(baseSalaryInDollars, "baseSalaryInDollars must not be null.");
    }

    public RoleId role() {
        return role;
    }

    public RoleSpec roleSpec() {
        return new RoleSpec(role, Money.ofDollars(baseSalaryInDollars.get()));
    }

    public String roleTranslationKey() {
        return "employee_role.createkontor." + role.value();
    }

    @Override
    public Class<EmployeeDeskBlockEntity> getBlockEntityClass() {
        return EmployeeDeskBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EmployeeDeskBlockEntity> getBlockEntityType() {
        return KontorBlockEntities.EMPLOYEE_DESK.get();
    }
}
