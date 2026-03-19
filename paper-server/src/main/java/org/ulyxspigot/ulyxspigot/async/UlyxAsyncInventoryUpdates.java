package org.ulyxspigot.ulyxspigot.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;

public final class UlyxAsyncInventoryUpdates {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();
    private static final AtomicLong TASK_COUNTER = new AtomicLong();

    private static volatile ExecutorService executor;
    private static volatile boolean enabled;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(UlyxAsyncInventoryUpdates::shutdown, "UlyxInventoryUpdateShutdown"));
    }

    private UlyxAsyncInventoryUpdates() {
    }

    public static synchronized void reconfigure(boolean shouldEnable) {
        shutdown();
        enabled = shouldEnable;

        if (!shouldEnable) {
            return;
        }

        // Keep inventory update packet ordering deterministic.
        executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "Ulyx InventoryUpdate Worker - " + THREAD_COUNTER.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        });

        Bukkit.getLogger().info("[UlyxSpigot] Async inventory-updates enabled");
    }

    public static boolean isEnabled() {
        return enabled && executor != null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void send(ServerGamePacketListenerImpl packetListener, Packet<?> packet) {
        final ExecutorService localExecutor = executor;
        if (!enabled || localExecutor == null) {
            UlyxAsyncPacketSending.send(packetListener, packet);
            return;
        }

        final long queued = TASK_COUNTER.incrementAndGet();
        if (queued > 100000L) {
            TASK_COUNTER.decrementAndGet();
            UlyxAsyncPacketSending.send(packetListener, packet);
            return;
        }

        localExecutor.execute(() -> {
            try {
                packetListener.send((Packet) packet);
            } finally {
                TASK_COUNTER.decrementAndGet();
            }
        });
    }

    public static synchronized void shutdown() {
        final ExecutorService localExecutor = executor;
        executor = null;
        enabled = false;
        TASK_COUNTER.set(0L);

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
            Bukkit.getLogger().log(Level.WARNING, "Interrupted while shutting down async inventory-updates", ex);
        }
    }
}
