package org.ulyxspigot.ulyxspigot.limiters;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.ulyxspigot.ulyxspigot.UlyxConfig;

public final class UlyxVehicleLimiter {

    private static final double COLLISION_SCAN_RANGE = 0.35D;

    private UlyxVehicleLimiter() {
    }

    public static boolean tryCullExcessMinecart(final AbstractMinecart self, final AbstractMinecart other) {
        if (!UlyxConfig.isLimitersRemoveExcessMinecarts()) {
            return false;
        }

        return tryCull(
            self,
            other,
            UlyxConfig.getLimitersExcessMinecartsLimit(),
            AbstractMinecart.class
        );
    }

    public static boolean tryCullExcessBoat(final AbstractBoat self, final AbstractBoat other) {
        if (!UlyxConfig.isLimitersRemoveExcessBoats()) {
            return false;
        }

        return tryCull(
            self,
            other,
            UlyxConfig.getLimitersExcessBoatsLimit(),
            AbstractBoat.class
        );
    }

    private static <T extends Entity> boolean tryCull(
        final Entity self,
        final Entity other,
        final int limit,
        final Class<T> vehicleClass
    ) {
        if (limit < 1 || self == null || other == null || self.level().isClientSide()) {
            return false;
        }

        final List<T> nearby = self.level().getEntitiesOfClass(
            vehicleClass,
            self.getBoundingBox().inflate(COLLISION_SCAN_RANGE),
            entity -> !entity.isRemoved() && entity.isAlive()
        );

        if (nearby.size() <= limit) {
            return false;
        }

        final Entity target = pickCullTarget(self, other);
        if (target == null) {
            return false;
        }

        target.discard();
        return true;
    }

    private static Entity pickCullTarget(final Entity self, final Entity other) {
        if (!isProtectedForRemoval(other)) {
            return other;
        }
        if (!isProtectedForRemoval(self)) {
            return self;
        }
        return null;
    }

    private static boolean isProtectedForRemoval(final Entity entity) {
        return entity.isPassenger() || entity.isVehicle();
    }
}
