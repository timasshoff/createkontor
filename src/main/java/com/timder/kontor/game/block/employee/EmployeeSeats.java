package com.timder.kontor.game.block.employee;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public final class EmployeeSeats {

    public static Optional<BlockPos> findDeskFor(Entity passenger) {
        if (!(passenger.getRootVehicle() instanceof SeatEntity seat)) {
            return Optional.empty();
        }
        return singleAdjacentDesk(passenger.level(), seat.blockPosition());
    }

    public static Optional<BlockPos> singleAdjacentDesk(Level level, BlockPos seatPos) {
        BlockPos found = null;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos candidate = seatPos.relative(side);
            if (level.getBlockState(candidate).getBlock() instanceof AbstractEmployeeDeskBlock) {
                if (found != null) {
                    return Optional.empty();
                }
                found = candidate;
            }
        }
        return Optional.ofNullable(found);
    }
}
