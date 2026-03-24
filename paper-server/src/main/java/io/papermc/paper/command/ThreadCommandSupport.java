package io.papermc.paper.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.ulyxspigot.ulyxspigot.UlyxConfig;

final class ThreadCommandSupport {
    private ThreadCommandSupport() {
    }

    static boolean isEnabled() {
        return UlyxConfig.isDeveloperEnableThreadsCommand();
    }

    static void sendDisabledMessage(final CommandSender sender) {
        sender.sendMessage("[UlyxSpigot] developer.enable-threads-command is false. Enable it in ulyxspigot/ulyxspigot.yml first.");
    }

    static List<Thread> getThreadSnapshot() {
        final List<Thread> threads = new ArrayList<>(Thread.getAllStackTraces().keySet());
        threads.sort(
            Comparator.comparing(Thread::getName, String.CASE_INSENSITIVE_ORDER)
                .thenComparingLong(Thread::getId)
        );
        return threads;
    }

    static String toSuggestion(final Thread thread) {
        return thread.getId() + ":" + thread.getName().replace(' ', '_');
    }

    static Thread findThreadByQuery(final String query, final List<Thread> threads) {
        if (query == null) {
            return null;
        }

        final String trimmed = query.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        Thread match = findByIdToken(trimmed, threads);
        if (match != null) {
            return match;
        }

        final String normalized = trimmed.replace('_', ' ');

        match = findByName(normalized, threads, true);
        if (match != null) {
            return match;
        }

        if (!normalized.equals(trimmed)) {
            match = findByName(trimmed, threads, true);
            if (match != null) {
                return match;
            }
        }

        match = findByName(normalized, threads, false);
        if (match != null) {
            return match;
        }

        if (!normalized.equals(trimmed)) {
            match = findByName(trimmed, threads, false);
            if (match != null) {
                return match;
            }
        }

        return null;
    }

    private static Thread findByIdToken(final String token, final List<Thread> threads) {
        String idToken = token;
        final int colonIndex = idToken.indexOf(':');
        if (colonIndex > 0) {
            idToken = idToken.substring(0, colonIndex);
        }

        try {
            final long id = Long.parseLong(idToken);
            for (final Thread thread : threads) {
                if (thread.getId() == id) {
                    return thread;
                }
            }
        } catch (final NumberFormatException ignored) {
        }

        return null;
    }

    private static Thread findByName(final String name, final List<Thread> threads, final boolean exact) {
        final String target = name.toLowerCase(Locale.ROOT);
        for (final Thread thread : threads) {
            final String threadName = thread.getName().toLowerCase(Locale.ROOT);
            if (exact ? threadName.equals(target) : threadName.contains(target)) {
                return thread;
            }
        }
        return null;
    }
}
