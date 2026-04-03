package io.papermc.paper.command;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class TpsBarCommand extends Command {
    private static final String BASE_PERMISSION = "bukkit.command.tpsbar";
    private static final String OTHERS_PERMISSION = BASE_PERMISSION + ".others";

    public TpsBarCommand(final String name) {
        super(name);
        this.description = "Shows TPS, MSPT and ping in a live boss bar";
        this.usageMessage = "/tpsbar [on|off|toggle] [player]";
        this.setPermission(BASE_PERMISSION);

        final PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        if (pluginManager.getPermission(BASE_PERMISSION) == null) {
            pluginManager.addPermission(new Permission(BASE_PERMISSION, PermissionDefault.OP));
        }
        if (pluginManager.getPermission(OTHERS_PERMISSION) == null) {
            pluginManager.addPermission(new Permission(OTHERS_PERMISSION, PermissionDefault.OP));
        }
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args, final Location location) throws IllegalArgumentException {
        final List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            addModeSuggestions(args[0], suggestions);
            if (sender.hasPermission(OTHERS_PERMISSION)) {
                addPlayerSuggestions(args[0], suggestions);
            }
            return suggestions;
        }

        if (args.length == 2 && isMode(args[0]) && sender.hasPermission(OTHERS_PERMISSION)) {
            addPlayerSuggestions(args[1], suggestions);
            return suggestions;
        }

        if (args.length == 2 && !isMode(args[0])) {
            addModeSuggestions(args[1], suggestions);
            return suggestions;
        }

        return List.of();
    }

    @Override
    public boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (args.length > 2) {
            sender.sendMessage("Usage: " + this.usageMessage);
            return false;
        }

        Mode mode = Mode.TOGGLE;
        Player target = null;

        if (args.length == 1) {
            if (isMode(args[0])) {
                mode = parseMode(args[0]);
            } else {
                target = findPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage("[UlyxSpigot] Player not found: " + args[0]);
                    return true;
                }
            }
        } else if (args.length == 2) {
            if (isMode(args[0])) {
                mode = parseMode(args[0]);
                target = findPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("[UlyxSpigot] Player not found: " + args[1]);
                    return true;
                }
            } else if (isMode(args[1])) {
                mode = parseMode(args[1]);
                target = findPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage("[UlyxSpigot] Player not found: " + args[0]);
                    return true;
                }
            } else {
                sender.sendMessage("Usage: " + this.usageMessage);
                return false;
            }
        }

        if (target == null) {
            if (sender instanceof Player player) {
                target = player;
            } else {
                sender.sendMessage("[UlyxSpigot] Console must specify a player.");
                sender.sendMessage("Usage: " + this.usageMessage);
                return true;
            }
        }

        if (!(sender instanceof Player senderPlayer && senderPlayer.getUniqueId().equals(target.getUniqueId())) && !sender.hasPermission(OTHERS_PERMISSION)) {
            sender.sendMessage(Bukkit.permissionMessage());
            return true;
        }

        final boolean currentlyEnabled = TpsBarService.isEnabled(target.getUniqueId());
        final boolean newState = switch (mode) {
            case ON -> true;
            case OFF -> false;
            case TOGGLE -> !currentlyEnabled;
        };

        TpsBarService.setEnabled(target, newState);

        if (sender.equals(target)) {
            sender.sendMessage("[UlyxSpigot] TPS bar " + (newState ? "enabled" : "disabled") + ".");
        } else {
            sender.sendMessage("[UlyxSpigot] TPS bar " + (newState ? "enabled" : "disabled") + " for " + target.getName() + ".");
            target.sendMessage("[UlyxSpigot] TPS bar " + (newState ? "enabled" : "disabled") + " by " + sender.getName() + ".");
        }

        return true;
    }

    private static Player findPlayer(final String input) {
        Player player = Bukkit.getPlayerExact(input);
        if (player == null) {
            player = Bukkit.getPlayer(input);
        }
        return player;
    }

    private static boolean isMode(final String value) {
        final String lower = value.toLowerCase(Locale.ROOT);
        return lower.equals("on") || lower.equals("off") || lower.equals("toggle");
    }

    private static Mode parseMode(final String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "on" -> Mode.ON;
            case "off" -> Mode.OFF;
            default -> Mode.TOGGLE;
        };
    }

    private static void addModeSuggestions(final String input, final List<String> suggestions) {
        if (CommandUtil.matches(input, "on")) {
            suggestions.add("on");
        }
        if (CommandUtil.matches(input, "off")) {
            suggestions.add("off");
        }
        if (CommandUtil.matches(input, "toggle")) {
            suggestions.add("toggle");
        }
    }

    private static void addPlayerSuggestions(final String input, final List<String> suggestions) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (CommandUtil.matches(input, player.getName())) {
                suggestions.add(player.getName());
            }
        }
    }

    private enum Mode {
        ON,
        OFF,
        TOGGLE
    }

    private static final class TpsBarService {
        private static final long UPDATE_INTERVAL_MS = 250L;
        private static final Set<UUID> ENABLED_PLAYERS = ConcurrentHashMap.newKeySet();
        private static final Map<UUID, BossBar> ACTIVE_BARS = new ConcurrentHashMap<>();
        private static final Object SCHEDULER_LOCK = new Object();
        private static final ThreadLocal<DecimalFormat> TPS_FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("0.00"));
        private static final ThreadLocal<DecimalFormat> MSPT_FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("0.0"));
        private static volatile ScheduledExecutorService updater;

        private TpsBarService() {
        }

        static boolean isEnabled(final UUID playerId) {
            return ENABLED_PLAYERS.contains(playerId);
        }

        static void setEnabled(final Player player, final boolean enabled) {
            final UUID playerId = player.getUniqueId();
            if (enabled) {
                ENABLED_PLAYERS.add(playerId);
                final BossBar bar = ACTIVE_BARS.computeIfAbsent(playerId, id -> Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID));
                if (!bar.getPlayers().contains(player)) {
                    bar.addPlayer(player);
                }
                bar.setVisible(true);
                startUpdater();
                queueUpdate();
                return;
            }

            ENABLED_PLAYERS.remove(playerId);
            removeBar(playerId);
            stopUpdaterIfIdle();
        }

        private static void startUpdater() {
            synchronized (SCHEDULER_LOCK) {
                if (updater != null && !updater.isShutdown()) {
                    return;
                }

                updater = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                    @Override
                    public Thread newThread(final Runnable runnable) {
                        final Thread thread = new Thread(runnable, "Ulyx-TPSBar-Updater");
                        thread.setDaemon(true);
                        return thread;
                    }
                });

                updater.scheduleAtFixedRate(() -> {
                    if (ENABLED_PLAYERS.isEmpty()) {
                        return;
                    }
                    final MinecraftServer server = MinecraftServer.getServer();
                    if (server == null) {
                        return;
                    }
                    try {
                        server.processQueue.add(TpsBarService::updateBarsOnMainThread);
                    } catch (final Throwable ignored) {
                        // Ignore here to keep the updater alive.
                    }
                }, UPDATE_INTERVAL_MS, UPDATE_INTERVAL_MS, TimeUnit.MILLISECONDS);
            }
        }

        private static void stopUpdaterIfIdle() {
            if (!ENABLED_PLAYERS.isEmpty()) {
                return;
            }
            synchronized (SCHEDULER_LOCK) {
                if (!ENABLED_PLAYERS.isEmpty()) {
                    return;
                }
                if (updater != null) {
                    updater.shutdownNow();
                    updater = null;
                }
            }
        }

        private static void queueUpdate() {
            final MinecraftServer server = MinecraftServer.getServer();
            if (server == null) {
                return;
            }
            try {
                server.processQueue.add(TpsBarService::updateBarsOnMainThread);
            } catch (final Throwable ignored) {
                // Ignore if the queue is unavailable during shutdown.
            }
        }

        private static void updateBarsOnMainThread() {
            if (ENABLED_PLAYERS.isEmpty()) {
                return;
            }

            final double[] tpsValues = Bukkit.getTPS();
            final double tps = clamp(tpsValues.length > 0 ? tpsValues[0] : 20.0D, 0.0D, 20.0D);
            final double mspt = Math.max(0.0D, Bukkit.getServer().getAverageTickTime());
            final double progress = clamp(tps / 20.0D, 0.0D, 1.0D);
            final BarColor barColor = pickBarColor(tps);

            for (final UUID playerId : new ArrayList<>(ENABLED_PLAYERS)) {
                final Player player = Bukkit.getPlayer(playerId);
                if (player == null || !player.isOnline()) {
                    ENABLED_PLAYERS.remove(playerId);
                    removeBar(playerId);
                    continue;
                }

                final BossBar bar = ACTIVE_BARS.computeIfAbsent(playerId, id -> Bukkit.createBossBar("", barColor, BarStyle.SOLID));
                if (!bar.getPlayers().contains(player)) {
                    bar.addPlayer(player);
                }

                bar.setColor(barColor);
                bar.setStyle(BarStyle.SOLID);
                bar.setProgress(progress);
                bar.setTitle(formatTitle(tps, mspt, Math.max(0, player.getPing())));
                bar.setVisible(true);
            }

            if (ENABLED_PLAYERS.isEmpty()) {
                stopUpdaterIfIdle();
            }
        }

        private static void removeBar(final UUID playerId) {
            final BossBar bar = ACTIVE_BARS.remove(playerId);
            if (bar != null) {
                bar.removeAll();
                bar.setVisible(false);
            }
        }

        private static BarColor pickBarColor(final double tps) {
            if (tps >= 18.0D) {
                return BarColor.GREEN;
            }
            if (tps >= 15.0D) {
                return BarColor.YELLOW;
            }
            return BarColor.RED;
        }

        private static String formatTitle(final double tps, final double mspt, final int ping) {
            return "§6TPS§7: "
                + colorForTps(tps)
                + TPS_FORMAT.get().format(tps)
                + " §8| §6MSPT§7: "
                + colorForMspt(mspt)
                + MSPT_FORMAT.get().format(mspt)
                + " §8| §6Ping§7: "
                + colorForPing(ping)
                + ping
                + "ms";
        }

        private static String colorForTps(final double tps) {
            if (tps >= 18.0D) {
                return "§a";
            }
            if (tps >= 15.0D) {
                return "§e";
            }
            return "§c";
        }

        private static String colorForMspt(final double mspt) {
            if (mspt <= 40.0D) {
                return "§a";
            }
            if (mspt <= 50.0D) {
                return "§e";
            }
            return "§c";
        }

        private static String colorForPing(final int ping) {
            if (ping <= 80) {
                return "§a";
            }
            if (ping <= 150) {
                return "§e";
            }
            return "§c";
        }

        private static double clamp(final double value, final double min, final double max) {
            return Math.max(min, Math.min(max, value));
        }
    }
}
