package org.ulyxspigot.ulyxspigot;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncChunkSending;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncDataSaving;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncInventoryUpdates;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncPacketSending;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncPathfinding;

public final class UlyxConfig {
    private static final String HEADER = "This is the main configuration file for UlyxSpigot.\n"
            + "These options are experimental and should be tested before production use.\n";
    private static final int CURRENT_CONFIG_VERSION = 1;
    private static final File CONFIG_FILE = new File("ulyxspigot", "ulyxspigot.yml");
    private static final Object LOAD_LOCK = new Object();

    private static volatile boolean loaded;
    private static YamlConfiguration config;

    private static String serverBrandNameDisplay = "UlyxSpigot";

    private static boolean asyncTrackerEnabled = true;
    private static boolean asyncPathfindingEnabled = true;
    private static int asyncPathfindingThreads = 2;
    private static boolean asyncChunksSendingEnabled = true;
    private static boolean asyncDataSavingEnabled = true;
    private static boolean asyncInventoryUpdatesEnabled = false;
    private static boolean asyncPacketSendingEnabled = false;

    private UlyxConfig() {
    }

    public static void ensureLoaded() {
        if (loaded) {
            return;
        }
        synchronized (LOAD_LOCK) {
            if (loaded) {
                return;
            }
            loadConfig();
            loaded = true;
        }
    }

    private static void loadConfig() {
        final File parent = CONFIG_FILE.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            Bukkit.getLogger().log(Level.WARNING, "Could not create config directory " + parent.getAbsolutePath());
        }

        config = new YamlConfiguration();
        try {
            config.load(CONFIG_FILE);
        } catch (IOException ignore) {
        } catch (InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not load ulyxspigot.yml, please correct your syntax errors", ex);
            throw new RuntimeException(ex);
        }

        config.options().header(HEADER);
        config.options().copyDefaults(true);

        set("config-version", CURRENT_CONFIG_VERSION);

        serverBrandNameDisplay = getString("server-brand-name-display", serverBrandNameDisplay);

        asyncTrackerEnabled = getBoolean("asynchronous.tracker.enabled", asyncTrackerEnabled);
        asyncPathfindingEnabled = getBoolean("asynchronous.pathfinding.enabled", asyncPathfindingEnabled);
        asyncPathfindingThreads = Math.max(0, getInt("asynchronous.pathfinding.threads", asyncPathfindingThreads));
        asyncChunksSendingEnabled = getBoolean("asynchronous.chunks-sending.enabled", asyncChunksSendingEnabled);
        asyncDataSavingEnabled = getBoolean("asynchronous.data-saving.enabled", asyncDataSavingEnabled);
        asyncInventoryUpdatesEnabled = getBoolean("asynchronous.inventory-updates.enabled", asyncInventoryUpdatesEnabled);
        asyncPacketSendingEnabled = getBoolean("asynchronous.packet-sending.enabled", asyncPacketSendingEnabled);

        UlyxAsyncPathfinding.reconfigure(asyncPathfindingEnabled, asyncPathfindingThreads);
        UlyxAsyncPacketSending.reconfigure(asyncPacketSendingEnabled);
        UlyxAsyncChunkSending.reconfigure(asyncChunksSendingEnabled);
        UlyxAsyncDataSaving.reconfigure(asyncDataSavingEnabled);
        UlyxAsyncInventoryUpdates.reconfigure(asyncInventoryUpdatesEnabled);

        try {
            config.save(CONFIG_FILE);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not save " + CONFIG_FILE, ex);
        }
    }

    public static String getServerBrandNameDisplay() {
        ensureLoaded();
        return serverBrandNameDisplay;
    }

    public static boolean isAsyncPathfindingEnabled() {
        ensureLoaded();
        return asyncPathfindingEnabled;
    }

    public static int getAsyncPathfindingThreads() {
        ensureLoaded();
        return asyncPathfindingThreads;
    }

    public static boolean isAsyncChunksSendingEnabled() {
        ensureLoaded();
        return asyncChunksSendingEnabled;
    }

    public static boolean isAsyncDataSavingEnabled() {
        ensureLoaded();
        return asyncDataSavingEnabled;
    }

    public static boolean isAsyncInventoryUpdatesEnabled() {
        ensureLoaded();
        return asyncInventoryUpdatesEnabled;
    }

    public static boolean isAsyncPacketSendingEnabled() {
        ensureLoaded();
        return asyncPacketSendingEnabled;
    }

    private static void set(String path, Object value) {
        config.addDefault(path, value);
        config.set(path, value);
    }

    private static String getString(String path, String def) {
        config.addDefault(path, def);
        return config.getString(path, def);
    }

    private static boolean getBoolean(String path, boolean def) {
        config.addDefault(path, def);
        return config.getBoolean(path, def);
    }

    private static int getInt(String path, int def) {
        config.addDefault(path, def);
        return config.getInt(path, def);
    }
}
