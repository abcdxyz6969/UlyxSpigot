package org.ulyxspigot.ulyxspigot.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.ulyxspigot.ulyxspigot.UlyxConfig;

public final class UlyxAsyncDataSaving {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();

    private static volatile ExecutorService executor;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(UlyxAsyncDataSaving::shutdown, "UlyxDataSavingShutdown"));
    }

    private UlyxAsyncDataSaving() {
    }

    public static synchronized void reconfigure() {
        shutdown();

        if (!UlyxConfig.isAsyncDataSavingEnabled()) {
            return;
        }

        final int threads = Math.max(1, Runtime.getRuntime().availableProcessors() / 4);
        executor = Executors.newFixedThreadPool(threads, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "Ulyx DataSave Worker - " + THREAD_COUNTER.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        });

        Bukkit.getLogger().info("[UlyxSpigot] Async data-saving enabled with " + threads + " thread(s)");
    }

    public static void savePlayerData(net.minecraft.world.level.storage.PlayerDataStorage playerIo, net.minecraft.server.level.ServerPlayer player) {
        if (!UlyxConfig.isAsyncDataSavingEnabled()) {
            playerIo.save(player);
            return;
        }

        final ExecutorService localExecutor = executor;
        if (localExecutor == null) {
            playerIo.save(player);
            return;
        }

        final java.util.UUID uuid = player.getUUID();
        localExecutor.execute(() -> {
            try {
                playerIo.save(player);
            } catch (Throwable throwable) {
                Bukkit.getLogger().log(Level.SEVERE, "[UlyxSpigot] Failed async data save for player " + uuid, throwable);
            }
        });
    }

    public static synchronized void shutdown() {
        final ExecutorService localExecutor = executor;
        executor = null;

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
            Bukkit.getLogger().log(Level.WARNING, "Interrupted while shutting down async data-saving", ex);
        }
    }
}
