package io.papermc.paper.command;

import java.text.DecimalFormat;
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
import gg.pufferfish.pufferfish.PufferfishConfig;
import org.ulyxspigot.ulyxspigot.UlyxConfig;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncWorldTicking;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

@DefaultQualifier(NonNull.class)
public final class UlyxCommand extends Command {
    private static final String BASE_PERMISSION = "bukkit.command.ulyx";
    private static final ThreadLocal<DecimalFormat> ONE_DECIMAL = ThreadLocal.withInitial(() -> new DecimalFormat("0.0"));

    public UlyxCommand(final String name) {
        super(name);
        this.description = "UlyxSpigot related commands";
        this.usageMessage = "/ulyx <reload|mspt [world]>";
        this.setAliases(List.of("ulyxspigot"));
        this.setPermission(BASE_PERMISSION);

        final PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        if (pluginManager.getPermission(BASE_PERMISSION) == null) {
            pluginManager.addPermission(new Permission(BASE_PERMISSION, PermissionDefault.OP));
        }
        if (pluginManager.getPermission(BASE_PERMISSION + ".reload") == null) {
            pluginManager.addPermission(new Permission(BASE_PERMISSION + ".reload", PermissionDefault.OP));
        }
        if (pluginManager.getPermission(BASE_PERMISSION + ".mspt") == null) {
            pluginManager.addPermission(new Permission(BASE_PERMISSION + ".mspt", PermissionDefault.OP));
        }
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args, final Location location) throws IllegalArgumentException {
        if (args.length == 1) {
            return CommandUtil.getListMatchingLast(sender, args, List.of("reload", "mspt"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("mspt")) {
            return CommandUtil.getListMatchingLast(sender, args, List.of("world"));
        }
        return List.of();
    }

    @Override
    public boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
        if (!testPermission(sender)) {
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            sender.sendMessage(text("Usage: " + this.usageMessage, RED));
            return false;
        }

        final String subcommand = args[0].toLowerCase(java.util.Locale.ROOT);

        if (subcommand.equals("reload")) {
            if (args.length != 1) {
                sender.sendMessage(text("Usage: " + this.usageMessage, RED));
                return false;
            }
            if (!sender.hasPermission(BASE_PERMISSION + ".reload") && !sender.hasPermission(BASE_PERMISSION)) {
                sender.sendMessage(Bukkit.permissionMessage());
                return true;
            }

            try {
                final List<String> blockedOptions = UlyxConfig.getReloadBlockedOptionsFromFile();
                if (!blockedOptions.isEmpty()) {
                    sender.sendMessage(text("[UlyxSpigot] Reload blocked. These options require restart while enabled: " + String.join(", ", blockedOptions), RED));
                    sender.sendMessage(text("[UlyxSpigot] Set them to false then run /ulyx reload, or restart the server.", RED));
                    return true;
                }

                UlyxConfig.reload();
                PufferfishConfig.reload();
                sender.sendMessage(text("[UlyxSpigot] Reloaded ulyxspigot/ulyxspigot.yml and pufferfish.yml", GREEN));
            } catch (Throwable throwable) {
                sender.sendMessage(text("[UlyxSpigot] Reload failed. Check console for details.", RED));
                Bukkit.getLogger().severe("[UlyxSpigot] Failed to reload config via /ulyx reload: " + throwable.getMessage());
                throwable.printStackTrace();
            }
            return true;
        }

        if (subcommand.equals("mspt")) {
            if (!sender.hasPermission(BASE_PERMISSION + ".mspt")
                && !sender.hasPermission("bukkit.command.mspt")
                && !sender.hasPermission(BASE_PERMISSION)) {
                sender.sendMessage(Bukkit.permissionMessage());
                return true;
            }

            if (args.length == 1) {
                MSPTCommand.sendMsptMessage(sender);
                return true;
            }

            if (args[1].equalsIgnoreCase("world")) {
                sendWorldMsptMessage(sender);
                return true;
            }

            sender.sendMessage(text("Usage: " + this.usageMessage, RED));
            return false;
        }

        sender.sendMessage(text("Usage: " + this.usageMessage, RED));
        return false;
    }

    private static void sendWorldMsptMessage(final CommandSender sender) {
        if (!UlyxConfig.isAsyncWorldTickingEnabled() || !UlyxAsyncWorldTicking.isEnabled()) {
            sender.sendMessage("[UlyxSpigot] asynchronous.world-ticking.enabled is false. Enable it and restart server first.");
            return;
        }

        final List<UlyxAsyncWorldTicking.WorldMsptSnapshot> snapshots = UlyxAsyncWorldTicking.getWorldMsptSnapshots();
        if (snapshots.isEmpty()) {
            sender.sendMessage("[UlyxSpigot] No per-world MSPT data yet. Wait a few seconds and run again.");
            return;
        }

        sender.sendMessage("[UlyxSpigot] Per-world MSPT (avg/min/max):");
        for (final UlyxAsyncWorldTicking.WorldMsptSnapshot snapshot : snapshots) {
            sender.sendMessage(" - " + snapshot.worldKey()
                + " | 5s " + formatWindow(snapshot.window5s())
                + " | 10s " + formatWindow(snapshot.window10s())
                + " | 1m " + formatWindow(snapshot.window1m()));
        }
    }

    private static String formatWindow(final UlyxAsyncWorldTicking.WindowMspt window) {
        if (window.samples() <= 0) {
            return "n/a";
        }
        return ONE_DECIMAL.get().format(window.avgMspt())
            + "/" + ONE_DECIMAL.get().format(window.minMspt())
            + "/" + ONE_DECIMAL.get().format(window.maxMspt())
            + " (n=" + window.samples() + ")";
    }
}
