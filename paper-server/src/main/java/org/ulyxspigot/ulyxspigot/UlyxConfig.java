package org.ulyxspigot.ulyxspigot;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
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
    private static boolean asyncDataSavingEnabled = true;
    private static boolean asyncInventoryUpdatesEnabled = false;
    private static boolean asyncPacketSendingEnabled = false;

    private static boolean behaviorAllowTeleportationWithPassengers = false;
    private static boolean behaviorDisableInitialWorldSpawn = false;
    private static boolean behaviorDisableWorldDataSaving = false;
    private static boolean behaviorDisableChatReporting = false;
    private static boolean behaviorDisablePortalHandling = false;
    private static boolean behaviorDisableActivationRange = false;
    private static boolean behaviorDisableEntityAI = false;
    private static boolean behaviorDisableTurtleHelmetTicking = false;
    private static boolean behaviorOldDeathItemDropBehavior = false;
    private static boolean behaviorRevertFoodRegeneration = false;
    private static double behaviorCookSpeedMultiplier = 1.0D;
    private static boolean behaviorOnlyPlayersPushEntities = false;
    private static boolean behaviorDisableEntitySuffocationCheck = false;
    private static boolean behaviorDisableEntityWorldBorderDamageCheck = false;
    private static boolean behaviorDisableLavaCatchesBlocksOnFire = false;
    private static boolean behaviorDisableLeafDecay = false;
    private static boolean behaviorDisableDragonFightTicking = false;
    private static boolean behaviorDisableWardenSpawnTracking = false;
    private static boolean behaviorDisableSleepAnnounceStatus = false;
    private static boolean behaviorDisableEntityBrain = false;
    private static boolean behaviorDisablePlayerStats = false;
    private static boolean behaviorDisableEntityCollisions = false;
    private static boolean behaviorDisableWeatherCycle = false;
    private static boolean behaviorDisableSkyBrightnessUpdates = false;
    private static boolean behaviorDisableDolphinTreasureGoal = false;

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

    public static void reload() {
        synchronized (LOAD_LOCK) {
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
        asyncDataSavingEnabled = getBoolean("asynchronous.data-saving.enabled", asyncDataSavingEnabled);
        asyncInventoryUpdatesEnabled = getBoolean("asynchronous.inventory-updates.enabled", asyncInventoryUpdatesEnabled);
        asyncPacketSendingEnabled = getBoolean("asynchronous.packet-sending.enabled", asyncPacketSendingEnabled);

        behaviorAllowTeleportationWithPassengers = getBoolean("behavior.allowTeleportationWithPassengers", behaviorAllowTeleportationWithPassengers);
        behaviorDisableInitialWorldSpawn = getBoolean("behavior.disableInitialWorldSpawn", behaviorDisableInitialWorldSpawn);
        behaviorDisableWorldDataSaving = getBoolean("behavior.disableWorldDataSaving", behaviorDisableWorldDataSaving);
        behaviorDisableChatReporting = getBoolean("behavior.disableChatReporting", behaviorDisableChatReporting);
        behaviorDisablePortalHandling = getBoolean("behavior.disablePortalHandling", behaviorDisablePortalHandling);
        behaviorDisableActivationRange = getBoolean("behavior.disableActivationRange", behaviorDisableActivationRange);
        behaviorDisableEntityAI = getBoolean("behavior.disableEntityAI", behaviorDisableEntityAI);
        behaviorDisableTurtleHelmetTicking = getBoolean("behavior.disableTurtleHelmetTicking", behaviorDisableTurtleHelmetTicking);
        behaviorOldDeathItemDropBehavior = getBoolean("behavior.oldDeathItemDropBehavior", behaviorOldDeathItemDropBehavior);
        behaviorRevertFoodRegeneration = getBoolean("behavior.revertFoodRegeneration", behaviorRevertFoodRegeneration);
        behaviorCookSpeedMultiplier = Math.max(0.01D, getDouble("behavior.cookSpeedMultiplier", behaviorCookSpeedMultiplier));
        behaviorOnlyPlayersPushEntities = getBoolean("behavior.onlyPlayersPushEntities", behaviorOnlyPlayersPushEntities);
        behaviorDisableEntitySuffocationCheck = getBoolean("behavior.disableEntitySuffocationCheck", behaviorDisableEntitySuffocationCheck);
        behaviorDisableEntityWorldBorderDamageCheck = getBoolean("behavior.disableEntityWorldBorderDamageCheck", behaviorDisableEntityWorldBorderDamageCheck);
        behaviorDisableLavaCatchesBlocksOnFire = getBoolean("behavior.disableLavaCatchesBlocksOnFire", behaviorDisableLavaCatchesBlocksOnFire);
        behaviorDisableLeafDecay = getBoolean("behavior.disableLeafDecay", behaviorDisableLeafDecay);
        behaviorDisableDragonFightTicking = getBoolean("behavior.disableDragonFightTicking", behaviorDisableDragonFightTicking);
        behaviorDisableWardenSpawnTracking = getBoolean("behavior.disableWardenSpawnTracking", behaviorDisableWardenSpawnTracking);
        behaviorDisableSleepAnnounceStatus = getBoolean("behavior.disableSleepAnnounceStatus", behaviorDisableSleepAnnounceStatus);
        behaviorDisableEntityBrain = getBoolean("behavior.disableEntityBrain", behaviorDisableEntityBrain);
        behaviorDisablePlayerStats = getBoolean("behavior.disablePlayerStats", behaviorDisablePlayerStats);
        behaviorDisableEntityCollisions = getBoolean("behavior.disableEntityCollisions", behaviorDisableEntityCollisions);
        behaviorDisableWeatherCycle = getBoolean("behavior.disableWeatherCycle", behaviorDisableWeatherCycle);
        behaviorDisableSkyBrightnessUpdates = getBoolean("behavior.disableSkyBrightnessUpdates", behaviorDisableSkyBrightnessUpdates);
        behaviorDisableDolphinTreasureGoal = getBoolean("behavior.disableDolphinTreasureGoal", behaviorDisableDolphinTreasureGoal);

        UlyxAsyncPathfinding.reconfigure(asyncPathfindingEnabled, asyncPathfindingThreads);
        UlyxAsyncPacketSending.reconfigure(asyncPacketSendingEnabled);
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

    public static boolean isBehaviorAllowTeleportationWithPassengers() {
        ensureLoaded();
        return behaviorAllowTeleportationWithPassengers;
    }

    public static boolean isBehaviorDisableInitialWorldSpawn() {
        ensureLoaded();
        return behaviorDisableInitialWorldSpawn;
    }

    public static boolean isBehaviorDisableWorldDataSaving() {
        ensureLoaded();
        return behaviorDisableWorldDataSaving;
    }

    public static boolean isBehaviorDisableChatReporting() {
        ensureLoaded();
        return behaviorDisableChatReporting;
    }

    public static boolean isBehaviorDisablePortalHandling() {
        ensureLoaded();
        return behaviorDisablePortalHandling;
    }

    public static boolean isBehaviorDisableActivationRange() {
        ensureLoaded();
        return behaviorDisableActivationRange;
    }

    public static boolean isBehaviorDisableEntityAI() {
        ensureLoaded();
        return behaviorDisableEntityAI;
    }

    public static boolean isBehaviorDisableTurtleHelmetTicking() {
        ensureLoaded();
        return behaviorDisableTurtleHelmetTicking;
    }

    public static boolean isBehaviorOldDeathItemDropBehavior() {
        ensureLoaded();
        return behaviorOldDeathItemDropBehavior;
    }

    public static boolean isBehaviorRevertFoodRegeneration() {
        ensureLoaded();
        return behaviorRevertFoodRegeneration;
    }

    public static double getBehaviorCookSpeedMultiplier() {
        ensureLoaded();
        return behaviorCookSpeedMultiplier;
    }

    public static boolean isBehaviorOnlyPlayersPushEntities() {
        ensureLoaded();
        return behaviorOnlyPlayersPushEntities;
    }

    public static boolean isBehaviorDisableEntitySuffocationCheck() {
        ensureLoaded();
        return behaviorDisableEntitySuffocationCheck;
    }

    public static boolean isBehaviorDisableEntityWorldBorderDamageCheck() {
        ensureLoaded();
        return behaviorDisableEntityWorldBorderDamageCheck;
    }

    public static boolean isBehaviorDisableLavaCatchesBlocksOnFire() {
        ensureLoaded();
        return behaviorDisableLavaCatchesBlocksOnFire;
    }

    public static boolean isBehaviorDisableLeafDecay() {
        ensureLoaded();
        return behaviorDisableLeafDecay;
    }

    public static boolean isBehaviorDisableDragonFightTicking() {
        ensureLoaded();
        return behaviorDisableDragonFightTicking;
    }

    public static boolean isBehaviorDisableWardenSpawnTracking() {
        ensureLoaded();
        return behaviorDisableWardenSpawnTracking;
    }

    public static boolean isBehaviorDisableSleepAnnounceStatus() {
        ensureLoaded();
        return behaviorDisableSleepAnnounceStatus;
    }

    public static boolean isBehaviorDisableEntityBrain() {
        ensureLoaded();
        return behaviorDisableEntityBrain;
    }

    public static boolean isBehaviorDisablePlayerStats() {
        ensureLoaded();
        return behaviorDisablePlayerStats;
    }

    public static boolean isBehaviorDisableEntityCollisions() {
        ensureLoaded();
        return behaviorDisableEntityCollisions;
    }

    public static boolean isBehaviorDisableWeatherCycle() {
        ensureLoaded();
        return behaviorDisableWeatherCycle;
    }

    public static boolean isBehaviorDisableSkyBrightnessUpdates() {
        ensureLoaded();
        return behaviorDisableSkyBrightnessUpdates;
    }

    public static boolean isBehaviorDisableDolphinTreasureGoal() {
        ensureLoaded();
        return behaviorDisableDolphinTreasureGoal;
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

    private static double getDouble(String path, double def) {
        config.addDefault(path, def);
        return config.getDouble(path, def);
    }
}
