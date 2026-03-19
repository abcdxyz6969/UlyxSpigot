package org.ulyxspigot.ulyxspigot.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;

public final class UlyxAsyncPacketSending {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();

    private static volatile ExecutorService executor;
    private static volatile boolean enabled;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(UlyxAsyncPacketSending::shutdown, "UlyxPacketSendShutdown"));
    }

    private UlyxAsyncPacketSending() {
    }

    public static synchronized void reconfigure(boolean shouldEnable) {
        shutdown();
        enabled = shouldEnable;

        if (!shouldEnable) {
            return;
        }

        final int threads = Math.max(1, Runtime.getRuntime().availableProcessors() / 4);
        executor = Executors.newFixedThreadPool(threads, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "Ulyx PacketSend Worker - " + THREAD_COUNTER.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        });

        Bukkit.getLogger().info("[UlyxSpigot] Async packet-sending enabled with " + threads + " thread(s)");
    }

    public static boolean isEnabled() {
        return enabled && executor != null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void send(ServerGamePacketListenerImpl packetListener, Packet<?> packet) {
        final ExecutorService localExecutor = executor;
        if (!enabled || localExecutor == null) {
            packetListener.send((Packet) packet);
            return;
        }

        localExecutor.execute(() -> packetListener.send((Packet) packet));
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
            Bukkit.getLogger().log(Level.WARNING, "Interrupted while shutting down async packet-sending", ex);
        }
    }
}
