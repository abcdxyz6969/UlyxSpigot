package org.ulyxspigot.ulyxspigot.async;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

public final class UlyxAsyncDataSaving {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();
    private static final ConcurrentMap<UUID, Object> SAVE_LOCKS = new ConcurrentHashMap<>();

    private static volatile ExecutorService executor;
    private static volatile boolean enabled;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(UlyxAsyncDataSaving::shutdown, "UlyxDataSavingShutdown"));
    }

    private UlyxAsyncDataSaving() {
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
                Thread thread = new Thread(runnable, "Ulyx DataSave Worker - " + THREAD_COUNTER.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        });

        Bukkit.getLogger().info("[UlyxSpigot] Async data-saving enabled with " + threads + " thread(s)");
    }

    public static void savePlayerData(PlayerDataStorage playerIo, ServerPlayer player) {
        if (org.spigotmc.SpigotConfig.disablePlayerDataSaving) {
            return;
        }

        if (!enabled || executor == null) {
            playerIo.save(player);
            return;
        }

        final CompoundTag compoundTag;
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(player.problemPath(), LOGGER)) {
            TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, player.registryAccess());
            player.saveWithoutId(tagValueOutput);
            compoundTag = tagValueOutput.buildResult();
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.WARNING, "[UlyxSpigot] Failed to encode player data for " + player.getScoreboardName(), ex);
            return;
        }

        final FastByteArrayOutputStream encoded = new FastByteArrayOutputStream(65536);
        try {
            NbtIo.writeCompressed(compoundTag, encoded);
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.WARNING, "[UlyxSpigot] Failed to compress player data for " + player.getScoreboardName(), ex);
            return;
        }

        final UUID uuid = player.getUUID();
        final String playerName = player.getScoreboardName();
        final String stringUuid = player.getStringUUID();
        final ExecutorService localExecutor = executor;
        if (localExecutor == null) {
            writePlayerData(playerIo, uuid, playerName, stringUuid, encoded);
            return;
        }

        localExecutor.execute(() -> writePlayerData(playerIo, uuid, playerName, stringUuid, encoded));
    }

    private static void writePlayerData(PlayerDataStorage playerIo, UUID uuid, String playerName, String stringUuid, FastByteArrayOutputStream encoded) {
        final Object lock = SAVE_LOCKS.computeIfAbsent(uuid, ignored -> new Object());

        synchronized (lock) {
            try {
                final Path path = playerIo.getPlayerDir().toPath();
                final Path tempPath = Files.createTempFile(path, stringUuid + "-", ".dat");
                FileUtils.writeByteArrayToFile(tempPath.toFile(), encoded.array, 0, encoded.length, false);

                final Path currentPath = path.resolve(stringUuid + ".dat");
                final Path backupPath = path.resolve(stringUuid + ".dat_old");
                Util.safeReplaceFile(currentPath, tempPath, backupPath);
            } catch (Exception ex) {
                Bukkit.getLogger().log(Level.WARNING, "[UlyxSpigot] Failed to save player data for " + playerName, ex);
            }
        }

        SAVE_LOCKS.remove(uuid, lock);
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
            Bukkit.getLogger().log(Level.WARNING, "Interrupted while shutting down async data-saving", ex);
        }
    }
}
