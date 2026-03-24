package io.papermc.paper.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class ThreadsListCommand extends Command {
    private static final String BASE_PERMISSION = "bukkit.command.threadslist";
    private static final int PAGE_SIZE = 20;

    public ThreadsListCommand(final String name) {
        super(name);
        this.description = "Lists running JVM threads";
        this.usageMessage = "/threadslist [page]";
        this.setAliases(List.of("threads"));
        this.setPermission(BASE_PERMISSION);

        final PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        if (pluginManager.getPermission(BASE_PERMISSION) == null) {
            pluginManager.addPermission(new Permission(BASE_PERMISSION, PermissionDefault.OP));
        }
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args, final Location location) throws IllegalArgumentException {
        if (!ThreadCommandSupport.isEnabled()) {
            return List.of();
        }
        if (args.length != 1) {
            return List.of();
        }

        final List<Thread> threads = ThreadCommandSupport.getThreadSnapshot();
        final int totalPages = Math.max(1, (threads.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        final int maxPageSuggestions = Math.min(totalPages, 20);
        final List<String> suggestions = new ArrayList<>(maxPageSuggestions);
        for (int page = 1; page <= maxPageSuggestions; page++) {
            final String pageString = Integer.toString(page);
            if (CommandUtil.matches(args[0], pageString)) {
                suggestions.add(pageString);
            }
        }
        return suggestions;
    }

    @Override
    public boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (!ThreadCommandSupport.isEnabled()) {
            ThreadCommandSupport.sendDisabledMessage(sender);
            return true;
        }

        if (args.length > 1) {
            sender.sendMessage("Usage: " + this.usageMessage);
            return false;
        }

        int page = 1;
        if (args.length == 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (final NumberFormatException ex) {
                sender.sendMessage("[UlyxSpigot] Invalid page number: " + args[0]);
                return true;
            }
        }

        final List<Thread> threads = ThreadCommandSupport.getThreadSnapshot();
        if (threads.isEmpty()) {
            sender.sendMessage("[UlyxSpigot] No active threads found.");
            return true;
        }

        final int totalThreads = threads.size();
        final int totalPages = Math.max(1, (totalThreads + PAGE_SIZE - 1) / PAGE_SIZE);
        if (page < 1 || page > totalPages) {
            sender.sendMessage("[UlyxSpigot] Page out of range. Valid range: 1-" + totalPages);
            return true;
        }

        int virtualCount = 0;
        for (final Thread thread : threads) {
            if (thread.isVirtual()) {
                virtualCount++;
            }
        }

        final int startIndex = (page - 1) * PAGE_SIZE;
        final int endExclusive = Math.min(startIndex + PAGE_SIZE, totalThreads);

        sender.sendMessage(
            "[UlyxSpigot] Threads "
                + (startIndex + 1)
                + "-"
                + endExclusive
                + "/"
                + totalThreads
                + " (page "
                + page
                + "/"
                + totalPages
                + ", virtual="
                + virtualCount
                + ")"
        );

        for (int i = startIndex; i < endExclusive; i++) {
            final Thread thread = threads.get(i);
            sender.sendMessage(
                "["
                    + thread.getId()
                    + "] "
                    + thread.getName()
                    + " | state="
                    + thread.getState()
                    + " | daemon="
                    + thread.isDaemon()
                    + " | virtual="
                    + thread.isVirtual()
            );
        }

        return true;
    }
}
