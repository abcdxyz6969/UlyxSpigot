package org.ulyxspigot.ulyxspigot.limiters;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.ulyxspigot.ulyxspigot.UlyxConfig;

public final class UlyxRedstoneLimiter {

    private enum CounterType {
        REDSTONE,
        PISTON,
        HOPPER,
        DISPENSER,
        DROPPER,
        OBSERVER
    }

    private static final class TickState {
        private long tick = Long.MIN_VALUE;
        private int redstone;
        private int piston;
        private int hopper;
        private int dispenser;
        private int dropper;
        private int observer;

        private void reset(final long gameTick) {
            this.tick = gameTick;
            this.redstone = 0;
            this.piston = 0;
            this.hopper = 0;
            this.dispenser = 0;
            this.dropper = 0;
            this.observer = 0;
        }
    }

    private static final Map<Level, TickState> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private UlyxRedstoneLimiter() {
    }

    public static boolean tryAcquireRedstone(final Level level, final BlockState state, final Block neighborBlock) {
        if (state == null || neighborBlock == null) {
            return true;
        }
        if (!isRedstoneLikeBlock(state.getBlock()) && !isRedstoneLikeBlock(neighborBlock)) {
            return true;
        }
        return tryAcquire(level, CounterType.REDSTONE);
    }

    public static boolean tryAcquirePiston(final Level level) {
        return tryAcquire(level, CounterType.PISTON);
    }

    public static boolean tryAcquireHopper(final Level level) {
        return tryAcquire(level, CounterType.HOPPER);
    }

    public static boolean tryAcquireDispenser(final Level level) {
        return tryAcquire(level, CounterType.DISPENSER);
    }

    public static boolean tryAcquireDropper(final Level level) {
        return tryAcquire(level, CounterType.DROPPER);
    }

    public static boolean tryAcquireObserver(final Level level) {
        return tryAcquire(level, CounterType.OBSERVER);
    }

    public static int getEffectiveMaxPistonPush() {
        if (!UlyxConfig.isLimitersRedstoneEnabled()) {
            return 12;
        }
        return Math.min(12, Math.max(0, UlyxConfig.getLimitersRedstoneMaxPistonPush()));
    }

    private static boolean tryAcquire(final Level level, final CounterType counterType) {
        if (level == null || level.isClientSide() || !UlyxConfig.isLimitersRedstoneEnabled()) {
            return true;
        }

        final TickState state;
        synchronized (STATES) {
            state = STATES.computeIfAbsent(level, ignored -> new TickState());
        }

        synchronized (state) {
            final long tick = level.getGameTime();
            if (state.tick != tick) {
                state.reset(tick);
            }

            int limit = resolveLimit(counterType);
            if (counterType == CounterType.OBSERVER) {
                final int threshold = UlyxConfig.getLimitersRedstoneBlockThreshold("OBSERVER");
                if (threshold >= 0) {
                    limit = Math.min(limit, threshold);
                }
            }

            if (limit < 0) {
                return true;
            }

            final int current = readCounter(state, counterType);
            if (current >= limit) {
                return false;
            }

            writeCounter(state, counterType, current + 1);
            return true;
        }
    }

    private static int resolveLimit(final CounterType counterType) {
        return switch (counterType) {
            case REDSTONE -> UlyxConfig.getLimitersRedstoneMaxRedstonePerTick();
            case PISTON -> UlyxConfig.getLimitersRedstoneMaxPistonPerTick();
            case HOPPER -> UlyxConfig.getLimitersRedstoneMaxHopperPerTick();
            case DISPENSER -> UlyxConfig.getLimitersRedstoneMaxDispenserPerTick();
            case DROPPER -> UlyxConfig.getLimitersRedstoneMaxDropperPerTick();
            case OBSERVER -> UlyxConfig.getLimitersRedstoneMaxObserverPerTick();
        };
    }

    private static boolean isRedstoneLikeBlock(final Block block) {
        final String key = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return key.contains("redstone")
            || key.contains("repeater")
            || key.contains("comparator")
            || key.contains("observer")
            || key.contains("piston")
            || key.contains("lever")
            || key.contains("button")
            || key.contains("pressure_plate")
            || key.contains("tripwire")
            || key.contains("daylight_detector")
            || key.contains("target");
    }

    private static int readCounter(final TickState state, final CounterType counterType) {
        return switch (counterType) {
            case REDSTONE -> state.redstone;
            case PISTON -> state.piston;
            case HOPPER -> state.hopper;
            case DISPENSER -> state.dispenser;
            case DROPPER -> state.dropper;
            case OBSERVER -> state.observer;
        };
    }

    private static void writeCounter(final TickState state, final CounterType counterType, final int value) {
        switch (counterType) {
            case REDSTONE -> state.redstone = value;
            case PISTON -> state.piston = value;
            case HOPPER -> state.hopper = value;
            case DISPENSER -> state.dispenser = value;
            case DROPPER -> state.dropper = value;
            case OBSERVER -> state.observer = value;
        }
    }
}
