package org.ulyxspigot.ulyxspigot.async;

import ca.spottedleaf.moonrise.common.util.TickThread;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public final class UlyxAsyncWorldTicking {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();

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
}
