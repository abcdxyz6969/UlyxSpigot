package org.ulyxspigot.ulyxspigot.async;

import ca.spottedleaf.moonrise.common.list.ReferenceList;
import ca.spottedleaf.moonrise.patches.chunk_system.entity.ChunkSystemEntity;
import ca.spottedleaf.moonrise.patches.chunk_system.level.ChunkSystemServerLevel;
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.server.ServerEntityLookup;
import ca.spottedleaf.moonrise.patches.entity_tracker.EntityTrackerEntity;
import ca.spottedleaf.moonrise.patches.entity_tracker.EntityTrackerTrackedEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Bukkit;

public final class UlyxAsyncTracker {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();
    private static final int MIN_SLICE_SIZE = 64;
    private static final int QUEUE_CAPACITY = 1024;

    private static final ConcurrentLinkedQueue<Runnable> MAIN_THREAD_TASKS = new ConcurrentLinkedQueue<>();

    private static volatile ThreadPoolExecutor executor;
    private static volatile boolean enabled;
    private static volatile long lastWarnMillis;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(UlyxAsyncTracker::shutdown, "UlyxTrackerShutdown"));
    }

    private UlyxAsyncTracker() {
    }

    public static synchronized void reconfigure(boolean shouldEnable) {
        shutdown();
        enabled = shouldEnable;

        if (!shouldEnable) {
            return;
        }

        final int threads = Math.max(1, Runtime.getRuntime().availableProcessors() / 4);
        executor = new ThreadPoolExecutor(
                threads,
                threads,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable runnable) {
                        final Thread thread = new Thread(runnable, "Ulyx Tracker Worker - " + THREAD_COUNTER.incrementAndGet());
                        thread.setDaemon(true);
                        return thread;
                    }
                },
                (runnable, threadPoolExecutor) -> {
                    runnable.run();
                    warnBusy();
                }
        );

        Bukkit.getLogger().info("[UlyxSpigot] Async tracker enabled with up to " + threads + " thread(s)");
    }

    public static boolean isEnabled() {
        return enabled && executor != null;
    }

    public static void executeOnMainThread(Runnable task) {
        if (task == null) {
            return;
        }

        if (Bukkit.isPrimaryThread()) {
            runMainThreadTask(task);
            return;
        }

        MAIN_THREAD_TASKS.offer(task);
    }

    public static void tick(ServerLevel level) {
        drainMainThreadTasks();

        final ThreadPoolExecutor localExecutor = executor;
        if (!enabled || localExecutor == null) {
            tickSync(level);
            drainMainThreadTasks();
            return;
        }

        final ServerEntityLookup entityLookup = (ServerEntityLookup) ((ChunkSystemServerLevel) level).moonrise$getEntityLookup();
        final ReferenceList<Entity> trackerEntities = entityLookup.trackerEntities;
        final int trackerSize = trackerEntities.size();
        if (trackerSize <= 0) {
            drainMainThreadTasks();
            return;
        }

        final Entity[] trackerEntitiesRaw = trackerEntities.getRawDataUnchecked();
        final int maxWorkers = Math.max(1, localExecutor.getMaximumPoolSize());
        final int taskCount = Math.min(maxWorkers, Math.max(1, (trackerSize + MIN_SLICE_SIZE - 1) / MIN_SLICE_SIZE));

        if (taskCount <= 1) {
            tickSlice(trackerEntitiesRaw, 0, trackerSize);
            tickLivingSync(trackerEntitiesRaw, trackerSize);
            drainMainThreadTasks();
            return;
        }

        final int sliceSize = (trackerSize + taskCount - 1) / taskCount;
        final List<Future<?>> futures = new ArrayList<>(taskCount);

        for (int start = 0; start < trackerSize; start += sliceSize) {
            final int sliceStart = start;
            final int sliceEnd = Math.min(trackerSize, start + sliceSize);
            futures.add(localExecutor.submit(() -> tickSlice(trackerEntitiesRaw, sliceStart, sliceEnd)));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                Bukkit.getLogger().log(Level.WARNING, "Interrupted while waiting for async tracker tasks", ex);
                drainMainThreadTasks();
                tickSync(level);
                drainMainThreadTasks();
                return;
            } catch (ExecutionException ex) {
                final Throwable cause = ex.getCause();
                if (isSyncOnlyViolation(cause)) {
                    Bukkit.getLogger().log(Level.WARNING,
                            "[UlyxSpigot] Async tracker hit sync-only game/plugin code path, disabling async tracker",
                            cause);
                    drainMainThreadTasks();
                    reconfigure(false);
                    tickSync(level);
                    drainMainThreadTasks();
                    return;
                }
                Bukkit.getLogger().log(Level.WARNING, "Async tracker task failed", cause);
            }
        }

        tickLivingSync(trackerEntitiesRaw, trackerSize);
        drainMainThreadTasks();
    }

    private static void tickSync(ServerLevel level) {
        final ServerEntityLookup entityLookup = (ServerEntityLookup) ((ChunkSystemServerLevel) level).moonrise$getEntityLookup();
        final ReferenceList<Entity> trackerEntities = entityLookup.trackerEntities;
        final Entity[] trackerEntitiesRaw = trackerEntities.getRawDataUnchecked();

        for (int i = 0, len = trackerEntities.size(); i < len; ++i) {
            tickTrackedEntity(trackerEntitiesRaw[i], false);
        }
    }

    private static void tickSlice(Entity[] trackerEntitiesRaw, int start, int end) {
        for (int i = start; i < end; ++i) {
            final Entity entity = trackerEntitiesRaw[i];
            if (entity == null || entity instanceof LivingEntity) {
                continue;
            }

            tickTrackedEntity(entity, true);
        }
    }

    private static void tickLivingSync(Entity[] trackerEntitiesRaw, int size) {
        for (int i = 0; i < size; ++i) {
            final Entity entity = trackerEntitiesRaw[i];
            if (!(entity instanceof LivingEntity)) {
                continue;
            }
            tickTrackedEntity(entity, false);
        }
    }

    private static void tickTrackedEntity(Entity entity, boolean lockTracker) {
        if (entity == null) {
            return;
        }

        final ChunkMap.TrackedEntity tracker = ((EntityTrackerEntity) entity).moonrise$getTrackedEntity();
        if (tracker == null) {
            return;
        }

        if (lockTracker) {
            synchronized (tracker) {
                tickTrackedEntity0(entity, tracker);
            }
            return;
        }

        tickTrackedEntity0(entity, tracker);
    }

    private static void tickTrackedEntity0(Entity entity, ChunkMap.TrackedEntity tracker) {
        final ChunkSystemEntity chunkSystemEntity = (ChunkSystemEntity) entity;
        final ca.spottedleaf.moonrise.patches.chunk_system.level.chunk.ChunkData chunkData = chunkSystemEntity.moonrise$getChunkData();
        ((EntityTrackerTrackedEntity) tracker).moonrise$tick(chunkData == null ? null : chunkData.nearbyPlayers);

        final FullChunkStatus chunkStatus = chunkSystemEntity.moonrise$getChunkStatus();
        if (((EntityTrackerTrackedEntity) tracker).moonrise$hasPlayers()
                || (chunkStatus != null && chunkStatus.isOrAfter(FullChunkStatus.ENTITY_TICKING))) {
            tracker.serverEntity.sendChanges();
        }
    }

    private static boolean isSyncOnlyViolation(Throwable throwable) {
        if (!(throwable instanceof IllegalStateException)) {
            return false;
        }

        final String message = throwable.getMessage();
        if (message == null) {
            return false;
        }

        return message.contains("may only be triggered synchronously")
                || message.startsWith("Asynchronous ");
    }

    private static void drainMainThreadTasks() {
        Runnable task;
        while ((task = MAIN_THREAD_TASKS.poll()) != null) {
            runMainThreadTask(task);
        }
    }

    private static void runMainThreadTask(Runnable task) {
        try {
            task.run();
        } catch (Throwable throwable) {
            Bukkit.getLogger().log(Level.WARNING, "[UlyxSpigot] Async tracker main-thread task failed", throwable);
        }
    }

    private static void warnBusy() {
        final long now = System.currentTimeMillis();
        if (now - lastWarnMillis < 30000L) {
            return;
        }

        lastWarnMillis = now;
        Bukkit.getLogger().warning("[UlyxSpigot] Async tracker queue is full, running tracker task on server thread");
    }

    public static synchronized void shutdown() {
        final ThreadPoolExecutor localExecutor = executor;
        executor = null;
        enabled = false;
        MAIN_THREAD_TASKS.clear();

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
            Bukkit.getLogger().log(Level.WARNING, "Interrupted while shutting down async tracker", ex);
        }
    }
}
