package io.papermc.paper.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
public final class ThreadCommand extends Command {
    private static final String BASE_PERMISSION = "bukkit.command.thread";
    private static final int MAX_STACK_LINES = 12;

    public ThreadCommand(final String name) {
        super(name);
        this.description = "Shows details about a specific JVM thread";
        this.usageMessage = "/thread <id|name...>";
        this.setAliases(List.of("threadinfo"));
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

        final String prefix = args[0].toLowerCase(Locale.ROOT);
        final List<String> suggestions = new ArrayList<>();
        for (final Thread thread : ThreadCommandSupport.getThreadSnapshot()) {
            final String id = Long.toString(thread.getId());
            final String suggestion = ThreadCommandSupport.toSuggestion(thread);
            if (id.startsWith(prefix) || suggestion.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                suggestions.add(suggestion);
                if (suggestions.size() >= 100) {
                    break;
                }
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

        if (args.length < 1) {
            sender.sendMessage("Usage: " + this.usageMessage);
            return false;
        }

        final String query = String.join(" ", args).trim();
        final List<Thread> threads = ThreadCommandSupport.getThreadSnapshot();
        final Thread thread = ThreadCommandSupport.findThreadByQuery(query, threads);

        if (thread == null) {
            sender.sendMessage("[UlyxSpigot] Thread not found: " + query);
            return true;
        }

        final ThreadGroup threadGroup = thread.getThreadGroup();
        sender.sendMessage("[UlyxSpigot] Thread info:");
        sender.sendMessage(" id: " + thread.getId());
        sender.sendMessage(" name: " + thread.getName());
        sender.sendMessage(" state: " + thread.getState());
        sender.sendMessage(" daemon: " + thread.isDaemon() + ", virtual: " + thread.isVirtual());
        sender.sendMessage(" priority: " + thread.getPriority() + ", alive: " + thread.isAlive() + ", interrupted: " + thread.isInterrupted());
        sender.sendMessage(" group: " + (threadGroup == null ? "<none>" : threadGroup.getName()));

        final StackTraceElement[] stackTrace = thread.getStackTrace();
        if (stackTrace.length == 0) {
            sender.sendMessage(" stack: <empty>");
            return true;
        }

        final int lines = Math.min(MAX_STACK_LINES, stackTrace.length);
        sender.sendMessage(" stack (top " + lines + "/" + stackTrace.length + "):");
        for (int i = 0; i < lines; i++) {
            sender.sendMessage("  at " + stackTrace[i]);
        }

        return true;
    }
}
