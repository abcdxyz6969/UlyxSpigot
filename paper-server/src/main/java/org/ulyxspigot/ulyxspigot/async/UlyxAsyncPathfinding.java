package org.ulyxspigot.ulyxspigot.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public final class UlyxAsyncPathfinding {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();
    private static final ThreadLocal<Boolean> IN_WORKER = ThreadLocal.withInitial(() -> false);

    private static volatile ExecutorService executor;
    private static volatile boolean enabled;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(UlyxAsyncPathfinding::shutdown, "UlyxPathfindingShutdown"));
    }

    private UlyxAsyncPathfinding() {
    }

    public static synchronized void reconfigure(boolean shouldEnable, int configuredThreads) {
        shutdown();
        enabled = shouldEnable;

        if (!shouldEnable) {
            return;
        }

        int threads = configuredThreads;
        if (threads <= 0) {
            threads = Math.max(1, Runtime.getRuntime().availableProcessors() / 4);
        }

        executor = Executors.newFixedThreadPool(threads, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "Ulyx Pathfinding Worker - " + THREAD_COUNTER.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        });

        Bukkit.getLogger().info("[UlyxSpigot] Async pathfinding enabled with " + threads + " thread(s)");
    }

    public static boolean canOffload() {
        return enabled && executor != null && !IN_WORKER.get();
    }

    public static <T> T supply(Supplier<T> supplier) {
        if (!canOffload()) {
            return supplier.get();
        }

        final ExecutorService localExecutor = executor;
        if (localExecutor == null) {
            return supplier.get();
        }

        Future<T> future = localExecutor.submit(() -> {
            IN_WORKER.set(true);
            try {
                return supplier.get();
            } finally {
                IN_WORKER.set(false);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while running async pathfinding", ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new RuntimeException("Failed while running async pathfinding", cause);
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
        } catch (InterruptedException ex) {
            localExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            Bukkit.getLogger().log(Level.WARNING, "Interrupted while shutting down async pathfinding", ex);
        }
    }
}
