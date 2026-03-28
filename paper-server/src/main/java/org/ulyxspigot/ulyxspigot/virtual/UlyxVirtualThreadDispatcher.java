package org.ulyxspigot.ulyxspigot.virtual;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UlyxVirtualThreadDispatcher {
    private static final Logger LOGGER = Logger.getLogger("UlyxSpigot");
    private static final int WORKERS_PER_CHANNEL = 1;

    private static volatile WorkerGroup commandSendingGroup;
    private static volatile WorkerGroup commandLoggingGroup;
    private static volatile WorkerGroup chatTextFilteringGroup;
    private static volatile WorkerGroup authenticatorGroup;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(UlyxVirtualThreadDispatcher::shutdown, "UlyxVirtualThreadsShutdown"));
    }

    private UlyxVirtualThreadDispatcher() {
    }

    public static synchronized void reconfigure(
        final boolean prestartEnabled,
        final boolean commandSendingEnabled,
        final boolean commandLoggingEnabled,
        final boolean chatTextFilteringEnabled,
        final boolean authenticatorEnabled
    ) {
        shutdown();

        if (!prestartEnabled) {
            return;
        }

        if (commandSendingEnabled) {
            commandSendingGroup = new WorkerGroup("Ulyx-CommandSend-Worker-", WORKERS_PER_CHANNEL);
        }
        if (commandLoggingEnabled) {
            commandLoggingGroup = new WorkerGroup("Ulyx-CommandLog-Worker-", WORKERS_PER_CHANNEL);
        }
        if (chatTextFilteringEnabled) {
            chatTextFilteringGroup = new WorkerGroup("Ulyx-ChatFilter-Worker-", WORKERS_PER_CHANNEL);
        }
        if (authenticatorEnabled) {
            authenticatorGroup = new WorkerGroup("Ulyx-Authenticator-Worker-", WORKERS_PER_CHANNEL);
        }
    }

    public static void executeCommandSending(final Runnable task) {
        execute(task, commandSendingGroup, "Ulyx-CommandSend-");
    }

    public static void executeCommandLogging(final Runnable task) {
        execute(task, commandLoggingGroup, "Ulyx-CommandLog-");
    }

    public static void executeAuthenticator(final Runnable task) {
        execute(task, authenticatorGroup, "Ulyx-Authenticator-");
    }

    public static Executor chatTextFilteringExecutor() {
        final WorkerGroup localGroup = chatTextFilteringGroup;
        if (localGroup != null) {
            return localGroup;
        }
        return runnable -> Thread.ofVirtual().name("Ulyx-ChatFilter-", 0).start(runnable);
    }

    private static void execute(final Runnable task, final WorkerGroup group, final String fallbackThreadPrefix) {
        Objects.requireNonNull(task, "task");

        if (group != null) {
            group.execute(task);
            return;
        }

        Thread.ofVirtual().name(fallbackThreadPrefix, 0).start(task);
    }

    public static synchronized void shutdown() {
        final WorkerGroup oldCommandSending = commandSendingGroup;
        final WorkerGroup oldCommandLogging = commandLoggingGroup;
        final WorkerGroup oldChatTextFiltering = chatTextFilteringGroup;
        final WorkerGroup oldAuthenticator = authenticatorGroup;

        commandSendingGroup = null;
        commandLoggingGroup = null;
        chatTextFilteringGroup = null;
        authenticatorGroup = null;

        if (oldCommandSending != null) {
            oldCommandSending.shutdown();
        }
        if (oldCommandLogging != null) {
            oldCommandLogging.shutdown();
        }
        if (oldChatTextFiltering != null) {
            oldChatTextFiltering.shutdown();
        }
        if (oldAuthenticator != null) {
            oldAuthenticator.shutdown();
        }
    }

    private static final class WorkerGroup implements Executor {
        private final String namePrefix;
        private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        private final AtomicBoolean running = new AtomicBoolean(true);
        private final List<Thread> workers;

        private WorkerGroup(final String namePrefix, final int workerCount) {
            this.namePrefix = namePrefix;
            final int totalWorkers = Math.max(1, workerCount);
            final List<Thread> createdWorkers = new ArrayList<>(totalWorkers);
            for (int i = 1; i <= totalWorkers; i++) {
                final Thread worker = Thread.ofVirtual().name(namePrefix + i).start(this::runLoop);
                createdWorkers.add(worker);
            }
            this.workers = List.copyOf(createdWorkers);
        }

        @Override
        public void execute(final Runnable command) {
            if (!this.running.get()) {
                Thread.ofVirtual().name(this.namePrefix + "fallback-", 0).start(command);
                return;
            }

            this.queue.offer(command);
        }

        private void runLoop() {
            while (this.running.get()) {
                try {
                    final Runnable task = this.queue.take();
                    task.run();
                } catch (final InterruptedException ex) {
                    if (!this.running.get()) {
                        return;
                    }
                } catch (final Throwable throwable) {
                    LOGGER.log(Level.WARNING, "[UlyxSpigot] Virtual thread worker task failed", throwable);
                }
            }
        }

        private void shutdown() {
            if (!this.running.compareAndSet(true, false)) {
                return;
            }

            for (final Thread worker : this.workers) {
                worker.interrupt();
            }
            this.queue.clear();
        }
    }
}
