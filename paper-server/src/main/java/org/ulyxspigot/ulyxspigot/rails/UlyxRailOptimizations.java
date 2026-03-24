package org.ulyxspigot.ulyxspigot.rails;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public final class UlyxRailOptimizations {

    private static final int DEFAULT_RAIL_POWER_LIMIT = 8;

    private UlyxRailOptimizations() {
    }

    public static boolean computePoweredState(final PoweredRailBlock self, final Level level, final BlockPos pos, final BlockState state) {
        final Map<BlockPos, Boolean> checkedPos = new HashMap<>();
        return level.hasNeighborSignal(pos)
            || findPoweredRailSignalFaster(self, level, pos, state, true, 0, checkedPos)
            || findPoweredRailSignalFaster(self, level, pos, state, false, 0, checkedPos);
    }

    private static boolean findPoweredRailSignalFaster(
        final PoweredRailBlock self,
        final Level level,
        final BlockPos pos,
        final boolean searchForward,
        final int distance,
        RailShape expectedShape,
        final Map<BlockPos, Boolean> checkedPos
    ) {
        final BlockState current = level.getBlockState(pos);
        final Boolean cached = checkedPos.get(pos);
        if (cached != null) {
            if (!cached.booleanValue()) {
                return false;
            }
            return level.hasNeighborSignal(pos)
                || findPoweredRailSignalFaster(self, level, pos, current, searchForward, distance + 1, checkedPos);
        }

        if (!current.is(self)) {
            checkedPos.put(pos, false);
            return false;
        }

        final RailShape shape = current.getValue(PoweredRailBlock.SHAPE);
        if (isIncompatible(expectedShape, shape)) {
            checkedPos.put(pos, false);
            return false;
        }

        if (!current.getValue(PoweredRailBlock.POWERED)) {
            checkedPos.put(pos, false);
            return false;
        }

        final boolean result = level.hasNeighborSignal(pos)
            || findPoweredRailSignalFaster(self, level, pos, current, searchForward, distance + 1, checkedPos);
        checkedPos.put(pos, result);
        return result;
    }

    private static boolean findPoweredRailSignalFaster(
        final PoweredRailBlock self,
        final Level level,
        final BlockPos pos,
        final BlockState state,
        final boolean searchForward,
        final int distance,
        final Map<BlockPos, Boolean> checkedPos
    ) {
        if (distance >= DEFAULT_RAIL_POWER_LIMIT) {
            return false;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean checkBelow = true;

        RailShape railShape = state.getValue(PoweredRailBlock.SHAPE);
        switch (railShape) {
            case NORTH_SOUTH -> {
                if (searchForward) {
                    ++z;
                } else {
                    --z;
                }
            }
            case EAST_WEST -> {
                if (searchForward) {
                    --x;
                } else {
                    ++x;
                }
            }
            case ASCENDING_EAST -> {
                if (searchForward) {
                    --x;
                } else {
                    ++x;
                    ++y;
                    checkBelow = false;
                }
                railShape = RailShape.EAST_WEST;
            }
            case ASCENDING_WEST -> {
                if (searchForward) {
                    --x;
                    ++y;
                    checkBelow = false;
                } else {
                    ++x;
                }
                railShape = RailShape.EAST_WEST;
            }
            case ASCENDING_NORTH -> {
                if (searchForward) {
                    ++z;
                } else {
                    --z;
                    ++y;
                    checkBelow = false;
                }
                railShape = RailShape.NORTH_SOUTH;
            }
            case ASCENDING_SOUTH -> {
                if (searchForward) {
                    ++z;
                    ++y;
                    checkBelow = false;
                } else {
                    --z;
                }
                railShape = RailShape.NORTH_SOUTH;
            }
            default -> {
                return false;
            }
        }

        final BlockPos next = new BlockPos(x, y, z);
        return findPoweredRailSignalFaster(self, level, next, searchForward, distance, railShape, checkedPos)
            || (checkBelow && findPoweredRailSignalFaster(self, level, next.below(), searchForward, distance, railShape, checkedPos));
    }

    private static boolean isIncompatible(final RailShape expectedShape, final RailShape actualShape) {
        if (expectedShape == RailShape.EAST_WEST) {
            return actualShape == RailShape.NORTH_SOUTH
                || actualShape == RailShape.ASCENDING_NORTH
                || actualShape == RailShape.ASCENDING_SOUTH;
        }

        if (expectedShape == RailShape.NORTH_SOUTH) {
            return actualShape == RailShape.EAST_WEST
                || actualShape == RailShape.ASCENDING_EAST
                || actualShape == RailShape.ASCENDING_WEST;
        }

        return false;
    }
}
