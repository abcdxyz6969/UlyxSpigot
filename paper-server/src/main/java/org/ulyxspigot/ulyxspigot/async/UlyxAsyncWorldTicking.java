package org.ulyxspigot.ulyxspigot.async;

import ca.spottedleaf.moonrise.common.util.TickThread;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;

public final class UlyxAsyncWorldTicking {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();
    private static final long WINDOW_5S_NANOS = TimeUnit.SECONDS.toNanos(5L);
    private static final long WINDOW_10S_NANOS = TimeUnit.SECONDS.toNanos(10L);
    private static final long WINDOW_1M_NANOS = TimeUnit.MINUTES.toNanos(1L);
    private static final Map<String, WorldTickHistory> WORLD_TICK_HISTORY = new ConcurrentHashMap<>();

    private static volatile ExecutorService executor;
    private static volatile boolean enabled;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(UlyxAsyncWorldTicking::shutdown, "UlyxWorldTickingShutdown"));
    }

    private UlyxAsyncWorldTicking() {
    }

    public static synchronized void reconfigure(final boolean shouldEnable, int configuredThreads) {
        shutdown();
        enabled = shouldEnable;

        if (!shouldEnable) {
            return;
        }

        if (configuredThreads <= 0) {
            configuredThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
        }

        final int threads = Math.max(1, configuredThreads);
        executor = Executors.newFixedThreadPool(threads, new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable runnable) {
                final TickThread thread = new TickThread(runnable, "Ulyx WorldTick Worker - " + THREAD_COUNTER.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        });

        Bukkit.getLogger().warning("[UlyxSpigot] Async world-ticking enabled with " + threads + " thread(s). This mode is experimental.");
    }

    public static boolean isEnabled() {
        return enabled && executor != null;
    }

    public static Future<?> submit(final Runnable task) {
        final ExecutorService localExecutor = executor;
        if (!enabled || localExecutor == null) {
            task.run();
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }
        return localExecutor.submit(task);
    }

    public static Future<?> submitWorldTick(final ServerLevel level, final Runnable task) {
        final String worldKey = level == null ? "unknown" : String.valueOf(level.dimension().identifier());
        return submit(() -> {
            final long start = System.nanoTime();
            try {
                task.run();
            } finally {
                recordWorldTick(worldKey, System.nanoTime() - start);
            }
        });
    }

    private static void recordWorldTick(final String worldKey, final long tickNanos) {
        if (tickNanos <= 0L) {
            return;
        }

        final String key = (worldKey == null || worldKey.isBlank()) ? "unknown" : worldKey;
        WORLD_TICK_HISTORY.computeIfAbsent(key, ignored -> new WorldTickHistory()).add(System.nanoTime(), tickNanos);
    }

    public static List<WorldMsptSnapshot> getWorldMsptSnapshots() {
        if (WORLD_TICK_HISTORY.isEmpty()) {
            return List.of();
        }

        final long nowNanos = System.nanoTime();
        final List<WorldMsptSnapshot> snapshots = new ArrayList<>(WORLD_TICK_HISTORY.size());

        for (final Map.Entry<String, WorldTickHistory> entry : WORLD_TICK_HISTORY.entrySet()) {
            final WorldMsptSnapshot snapshot = entry.getValue().snapshot(entry.getKey(), nowNanos);
            if (snapshot != null && snapshot.window1m().samples() > 0) {
                snapshots.add(snapshot);
            }
        }

        snapshots.sort(Comparator.comparing(WorldMsptSnapshot::worldKey));
        return snapshots;
    }

    public static void joinAll(final Collection<? extends Future<?>> futures) {
        for (final Future<?> future : futures) {
            try {
                future.get();
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for async world tick", ex);
            } catch (final ExecutionException ex) {
                final Throwable cause = ex.getCause();
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                if (cause instanceof Error error) {
                    throw error;
                }
                throw new RuntimeException("Failed while running async world tick", cause);
            }
        }
    }

    public static synchronized void shutdown() {
        final ExecutorService localExecutor = executor;
        executor = null;
        enabled = false;

        WORLD_TICK_HISTORY.clear();

        if (localExecutor == null) {
            return;
        }

        localExecutor.shutdown();
        try {
            if (!localExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                localExecutor.shutdownNow();
            }
        } catch (final InterruptedException ex) {
            localExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            Bukkit.getLogger().log(Level.WARNING, "Interrupted while shutting down async world-ticking", ex);
        }
    }

    public record WindowMspt(int samples, double avgMspt, double minMspt, double maxMspt) {
    }

    public record WorldMsptSnapshot(String worldKey, WindowMspt window5s, WindowMspt window10s, WindowMspt window1m) {
    }

    private static final class WorldTickHistory {
        private final ArrayDeque<WorldTickSample> samples = new ArrayDeque<>();

        synchronized void add(final long nowNanos, final long tickNanos) {
            this.samples.addLast(new WorldTickSample(nowNanos, tickNanos));
            this.trim(nowNanos);
        }

        synchronized WorldMsptSnapshot snapshot(final String worldKey, final long nowNanos) {
            this.trim(nowNanos);
            if (this.samples.isEmpty()) {
                return null;
            }

            return new WorldMsptSnapshot(
                worldKey,
                this.computeWindow(nowNanos, WINDOW_5S_NANOS),
                this.computeWindow(nowNanos, WINDOW_10S_NANOS),
                this.computeWindow(nowNanos, WINDOW_1M_NANOS)
            );
        }

        private void trim(final long nowNanos) {
            final long cutoff = nowNanos - WINDOW_1M_NANOS;
            WorldTickSample first;
            while ((first = this.samples.peekFirst()) != null && first.timestampNanos() < cutoff) {
                this.samples.pollFirst();
            }
        }

        private WindowMspt computeWindow(final long nowNanos, final long windowNanos) {
            final long cutoff = nowNanos - windowNanos;
            int count = 0;
            double total = 0.0D;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;

            for (final WorldTickSample sample : this.samples) {
                if (sample.timestampNanos() < cutoff) {
                    continue;
                }

                final double mspt = sample.tickNanos() / 1_000_000.0D;
                total += mspt;
                min = Math.min(min, mspt);
                max = Math.max(max, mspt);
                ++count;
            }

            if (count == 0) {
                return new WindowMspt(0, 0.0D, 0.0D, 0.0D);
            }

            return new WindowMspt(count, total / count, min, max);
        }
    }

    private record WorldTickSample(long timestampNanos, long tickNanos) {
    }
}
