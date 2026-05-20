package org.ulyxspigot.ulyxspigot.limiters;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.level.Level;
import org.ulyxspigot.ulyxspigot.UlyxConfig;

public final class UlyxItemMergeLimiter {

    private static final class TickState {
        private long tick = Long.MIN_VALUE;
        private int attempts;

        private void reset(final long gameTick) {
            this.tick = gameTick;
            this.attempts = 0;
        }
    }

    private static final Map<Level, TickState> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private UlyxItemMergeLimiter() {
    }

    public static boolean tryAcquireMerge(final Level level) {
        if (level == null || level.isClientSide()) {
            return true;
        }

        final int limit = UlyxConfig.getLimitersItemMaxMergeAttemptsPerTick();
        if (limit < 0) {
            return true;
        }

        final TickState state;
        synchronized (STATES) {
            state = STATES.computeIfAbsent(level, ignored -> new TickState());
        }

        synchronized (state) {
            final long currentTick = level.getGameTime();
            if (state.tick != currentTick) {
                state.reset(currentTick);
            }

            if (state.attempts >= limit) {
                return false;
            }

            state.attempts++;
            return true;
        }
    }
}
