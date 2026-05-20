package org.ulyxspigot.ulyxspigot.async;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.NaturalSpawner;
import org.bukkit.Bukkit;

public final class UlyxAsyncMobSpawning {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();
    private static final Map<ServerLevel, SpawnStateHolder> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private static volatile ExecutorService executor;
    private static volatile boolean enabled;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(UlyxAsyncMobSpawning::shutdown, "UlyxMobSpawningShutdown"));
    }

    private UlyxAsyncMobSpawning() {
    }

    public static synchronized void reconfigure(final boolean shouldEnable) {
        shutdown();
        enabled = shouldEnable;

        if (!shouldEnable) {
            return;
        }

        executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable runnable) {
                final Thread thread = new Thread(runnable, "Ulyx MobSpawning Worker - " + THREAD_COUNTER.incrementAndGet());
                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY + 1);
                return thread;
            }
        });

        Bukkit.getLogger().info("[UlyxSpigot] Async mob-spawning enabled with 1 thread");
    }

    public static NaturalSpawner.SpawnState createState(
        final ServerLevel level,
        final Supplier<NaturalSpawner.SpawnState> syncComputer
    ) {
        if (syncComputer == null) {
            return null;
        }

        final ExecutorService localExecutor = executor;
        if (!enabled || localExecutor == null || level == null) {
            return syncComputer.get();
        }

        if (level.paperConfig().entities.spawning.perPlayerMobSpawns) {
            return syncComputer.get();
        }

        final SpawnStateHolder holder;
        synchronized (STATES) {
            holder = STATES.computeIfAbsent(level, ignored -> new SpawnStateHolder());
        }

        final NaturalSpawner.SpawnState snapshot = holder.latest;
        if (snapshot == null) {
            final NaturalSpawner.SpawnState initial = syncComputer.get();
            holder.latest = initial;
            return initial;
        }

        if (holder.computing.compareAndSet(false, true)) {
            localExecutor.execute(() -> {
                try {
                    holder.latest = syncComputer.get();
                } catch (final Throwable throwable) {
                    Bukkit.getLogger().log(Level.WARNING, "[UlyxSpigot] Async mob-spawning compute failed, keeping previous spawn state", throwable);
                } finally {
                    holder.computing.set(false);
                }
            });
        }

        return snapshot;
    }

    public static synchronized void shutdown() {
        final ExecutorService localExecutor = executor;
        executor = null;
        enabled = false;

        STATES.clear();

        if (localExecutor == null) {
            return;
        }

        localExecutor.shutdown();
        try {
            if (!localExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                localExecutor.shutdownNow();
            }
        } catch (final InterruptedException ex) {
            localExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            Bukkit.getLogger().log(Level.WARNING, "Interrupted while shutting down async mob-spawning", ex);
        }
    }

    private static final class SpawnStateHolder {
        private final AtomicBoolean computing = new AtomicBoolean();
        private volatile NaturalSpawner.SpawnState latest;
    }
}
