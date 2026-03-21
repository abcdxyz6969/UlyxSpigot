package org.ulyxspigot.ulyxspigot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncDataSaving;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncInventoryUpdates;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncPacketSending;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncPathfinding;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncTracker;

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

    private static boolean experimentalReducePlayerChunkSourceUpdates = true;
    private static boolean experimentalReduceChunkMidTickTaskExecution = true;
    private static boolean experimentalDisableChunkNewerVersionLoadCheck = false;

    private static boolean developerRecalculateChunksOutOfBounds = false;
    private static boolean developerAllowInvalidEnchantLevels = false;
    private static boolean developerDisableAsyncCatcher = false;
    private static boolean developerDisableSessionLockFile = false;

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

    private static List<String> loadChunksEntities = List.of("EXAMPLE");
    private static Set<String> loadChunksEntitiesSet = Set.of();
    private static List<String> waterSensitiveEntities = List.of("EXAMPLE");
    private static Set<String> waterSensitiveEntitiesSet = Set.of();
    private static List<String> entityUpdateInterval = List.of("PLAYER:2");
    private static Map<String, Integer> entityUpdateIntervalByType = Map.of();
    private static boolean disableGrassLightChecks = true;
    private static boolean disableSnowLightChecks = true;
    private static boolean disableSpawnerLightChecks = true;

    private static int spawnerMinSpawnDelay = 200;
    private static int spawnerMaxSpawnDelay = 800;
    private static int spawnerSpawnCount = 4;
    private static int spawnerMaxNearbyEntities = 6;
    private static int spawnerRequiredPlayerRange = 16;
    private static int spawnerSpawnRange = 4;
    private static boolean spawnerDisableMaxNearbyEntitiesCheck = true;
    private static boolean spawnerDisableNearbyPlayersCheck = true;
    private static boolean spawnerDisableObstructionCheck = true;
    private static boolean spawnerDisableWaterPreventSpawnCheck = true;

    private static boolean alternativeFarmsRaidsOldBehavior = false;
    private static boolean alternativeFarmsRaidsDropOminousBottles = false;

    private static double combatMaceLimitFallDistance = -1.0D;
    private static boolean combatMaceIgnoreFallDistance = false;
    private static boolean combatAlternativeHitRegistration = false;
    private static boolean combatDisableKnockbackScaling = false;
    private static boolean combatLegacyBlastProtection = false;
    private static boolean combatDisableShieldEffectiveness = false;
    private static boolean combatOldEnchantedGappleEffects = false;
    private static boolean combatImitateSwordBlocking = false;
    private static boolean combatRevertArmorProtection = false;
    private static boolean combatDisableNetheriteKnockbackResistance = false;
    private static boolean combatOldSharpnessDamageBuff = false;
    private static boolean combatPreventCriticalsIfSprinting = true;
    private static boolean combatDisableHitDelay = false;
    private static boolean combatEnableBowBoosting = true;
    private static boolean combatOldCollisionsProjectile = false;
    private static boolean combatDisableSweepingEdge = false;
    private static boolean combatFishingHooksDoKnockback = false;
    private static boolean combatFishingHooksPullEntities = true;
    private static boolean combatOldToolAttackDamage = false;
    private static double combatCriticalModifier = 1.5D;
    private static boolean combatKnockbackOldKnockback = false;
    private static double combatKnockbackVertical = 0.4D;
    private static double combatKnockbackHorizontal = 0.4D;
    private static double combatKnockbackFriction = 2.0D;
    private static double combatKnockbackVerticalLimit = 0.4000000059604645D;

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

        experimentalReducePlayerChunkSourceUpdates = getBoolean("experimental.reducePlayerChunkSourceUpdates", experimentalReducePlayerChunkSourceUpdates);
        experimentalReduceChunkMidTickTaskExecution = getBoolean("experimental.reduceChunkMidTickTaskExecution", experimentalReduceChunkMidTickTaskExecution);
        experimentalDisableChunkNewerVersionLoadCheck = getBoolean("experimental.disableChunkNewerVersionLoadCheck", experimentalDisableChunkNewerVersionLoadCheck);

        developerRecalculateChunksOutOfBounds = getBoolean("developer.recalculateChunksOutOfBounds", developerRecalculateChunksOutOfBounds);
        developerAllowInvalidEnchantLevels = getBoolean("developer.allowInvalidEnchantLevels", developerAllowInvalidEnchantLevels);
        developerDisableAsyncCatcher = getBoolean("developer.disableAsyncCatcher", developerDisableAsyncCatcher);
        developerDisableSessionLockFile = getBoolean("developer.disableSessionLockFile", developerDisableSessionLockFile);

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

        loadChunksEntities = getStringList("load-chunks.entities", loadChunksEntities);
        waterSensitiveEntities = getStringList("water-sensitive.entities", waterSensitiveEntities);
        entityUpdateInterval = getStringList("entity-update-interval", entityUpdateInterval);
        loadChunksEntitiesSet = parseEntityTypeSet(loadChunksEntities, "load-chunks.entities");
        waterSensitiveEntitiesSet = parseEntityTypeSet(waterSensitiveEntities, "water-sensitive.entities");
        entityUpdateIntervalByType = parseEntityUpdateIntervals(entityUpdateInterval);
        disableGrassLightChecks = getBoolean("disableGrassLightChecks", disableGrassLightChecks);
        disableSnowLightChecks = getBoolean("disableSnowLightChecks", disableSnowLightChecks);
        disableSpawnerLightChecks = getBoolean("disableSpawnerLightChecks", disableSpawnerLightChecks);

        spawnerMinSpawnDelay = Math.max(0, getInt("spawner.minSpawnDelay", spawnerMinSpawnDelay));
        spawnerMaxSpawnDelay = Math.max(spawnerMinSpawnDelay, getInt("spawner.maxSpawnDelay", spawnerMaxSpawnDelay));
        spawnerSpawnCount = Math.max(1, getInt("spawner.spawnCount", spawnerSpawnCount));
        spawnerMaxNearbyEntities = Math.max(0, getInt("spawner.maxNearbyEntities", spawnerMaxNearbyEntities));
        spawnerRequiredPlayerRange = Math.max(0, getInt("spawner.requiredPlayerRange", spawnerRequiredPlayerRange));
        spawnerSpawnRange = Math.max(0, getInt("spawner.spawnRange", spawnerSpawnRange));
        spawnerDisableMaxNearbyEntitiesCheck = getBoolean("spawner.disableMaxNearbyEntitiesCheck", spawnerDisableMaxNearbyEntitiesCheck);
        spawnerDisableNearbyPlayersCheck = getBoolean("spawner.disableNearbyPlayersCheck", spawnerDisableNearbyPlayersCheck);
        spawnerDisableObstructionCheck = getBoolean("spawner.disableObstructionCheck", spawnerDisableObstructionCheck);
        spawnerDisableWaterPreventSpawnCheck = getBoolean("spawner.disableWaterPreventSpawnCheck", spawnerDisableWaterPreventSpawnCheck);

        alternativeFarmsRaidsOldBehavior = getBoolean("alternative-farms.raids.old-behavior", alternativeFarmsRaidsOldBehavior);
        alternativeFarmsRaidsDropOminousBottles = getBoolean("alternative-farms.raids.drop-ominous-bottles", alternativeFarmsRaidsDropOminousBottles);

        combatMaceLimitFallDistance = getDouble("combat.mace.limit-fall-distance", combatMaceLimitFallDistance);
        combatMaceIgnoreFallDistance = getBoolean("combat.mace.ignore-fall-distance", combatMaceIgnoreFallDistance);
        combatAlternativeHitRegistration = getBoolean("combat.alternative-hit-registration", combatAlternativeHitRegistration);
        combatDisableKnockbackScaling = getBoolean("combat.disableKnockbackScaling", combatDisableKnockbackScaling);
        combatLegacyBlastProtection = getBoolean("combat.legacyBlastProtection", combatLegacyBlastProtection);
        combatDisableShieldEffectiveness = getBoolean("combat.disableShieldEffectiveness", combatDisableShieldEffectiveness);
        combatOldEnchantedGappleEffects = getBoolean("combat.oldEnchantedGappleEffects", combatOldEnchantedGappleEffects);
        combatImitateSwordBlocking = getBoolean("combat.imitateSwordBlocking", combatImitateSwordBlocking);
        combatRevertArmorProtection = getBoolean("combat.revertArmorProtection", combatRevertArmorProtection);
        combatDisableNetheriteKnockbackResistance = getBoolean("combat.disableNetheriteKnockbackResistance", combatDisableNetheriteKnockbackResistance);
        combatOldSharpnessDamageBuff = getBoolean("combat.oldSharpnessDamageBuff", combatOldSharpnessDamageBuff);
        combatPreventCriticalsIfSprinting = getBoolean("combat.preventCriticalsIfSprinting", combatPreventCriticalsIfSprinting);
        combatDisableHitDelay = getBoolean("combat.disableHitDelay", combatDisableHitDelay);
        combatEnableBowBoosting = getBoolean("combat.enableBowBoosting", combatEnableBowBoosting);
        combatOldCollisionsProjectile = getBoolean("combat.oldCollisionsProjectile", combatOldCollisionsProjectile);
        combatDisableSweepingEdge = getBoolean("combat.disableSweepingEdge", combatDisableSweepingEdge);
        combatFishingHooksDoKnockback = getBoolean("combat.fishingHooksDoKnockback", combatFishingHooksDoKnockback);
        combatFishingHooksPullEntities = getBoolean("combat.fishingHooksPullEntities", combatFishingHooksPullEntities);
        combatOldToolAttackDamage = getBoolean("combat.oldToolAttackDamage", combatOldToolAttackDamage);
        combatCriticalModifier = getDouble("combat.criticalModifier", combatCriticalModifier);
        combatKnockbackOldKnockback = getBoolean("combat.knockback.oldKnockback", combatKnockbackOldKnockback);
        combatKnockbackVertical = getDouble("combat.knockback.vertical", combatKnockbackVertical);
        combatKnockbackHorizontal = getDouble("combat.knockback.horizontal", combatKnockbackHorizontal);
        combatKnockbackFriction = getDouble("combat.knockback.friction", combatKnockbackFriction);
        combatKnockbackVerticalLimit = getDouble("combat.knockback.verticalLimit", combatKnockbackVerticalLimit);

        UlyxAsyncTracker.reconfigure(asyncTrackerEnabled);
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

    public static boolean isAsyncTrackerEnabled() {
        ensureLoaded();
        return asyncTrackerEnabled;
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

    public static boolean isExperimentalReducePlayerChunkSourceUpdates() {
        ensureLoaded();
        return experimentalReducePlayerChunkSourceUpdates;
    }

    public static boolean isExperimentalReduceChunkMidTickTaskExecution() {
        ensureLoaded();
        return experimentalReduceChunkMidTickTaskExecution;
    }

    public static boolean isExperimentalDisableChunkNewerVersionLoadCheck() {
        ensureLoaded();
        return experimentalDisableChunkNewerVersionLoadCheck;
    }

    public static boolean isDeveloperRecalculateChunksOutOfBounds() {
        ensureLoaded();
        return developerRecalculateChunksOutOfBounds;
    }

    public static boolean isDeveloperAllowInvalidEnchantLevels() {
        ensureLoaded();
        return developerAllowInvalidEnchantLevels;
    }

    public static boolean isDeveloperDisableAsyncCatcher() {
        ensureLoaded();
        return developerDisableAsyncCatcher;
    }

    public static boolean isDeveloperDisableSessionLockFile() {
        ensureLoaded();
        return developerDisableSessionLockFile;
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

    public static List<String> getLoadChunksEntities() {
        ensureLoaded();
        return loadChunksEntities;
    }

    public static List<String> getWaterSensitiveEntities() {
        ensureLoaded();
        return waterSensitiveEntities;
    }

    public static List<String> getEntityUpdateInterval() {
        ensureLoaded();
        return entityUpdateInterval;
    }
    public static boolean isLoadChunksEntity(String entityType) {
        ensureLoaded();
        if (entityType == null) {
            return false;
        }
        return loadChunksEntitiesSet.contains(entityType.toUpperCase(Locale.ROOT));
    }

    public static boolean isWaterSensitiveEntity(String entityType) {
        ensureLoaded();
        if (entityType == null) {
            return false;
        }
        return waterSensitiveEntitiesSet.contains(entityType.toUpperCase(Locale.ROOT));
    }



    public static int getEntityUpdateIntervalFor(String entityType, int defaultInterval) {
        ensureLoaded();
        if (entityType == null) {
            return defaultInterval;
        }
        return entityUpdateIntervalByType.getOrDefault(entityType.toUpperCase(Locale.ROOT), defaultInterval);
    }
    public static boolean isDisableGrassLightChecks() {
        ensureLoaded();
        return disableGrassLightChecks;
    }

    public static boolean isDisableSnowLightChecks() {
        ensureLoaded();
        return disableSnowLightChecks;
    }

    public static boolean isDisableSpawnerLightChecks() {
        ensureLoaded();
        return disableSpawnerLightChecks;
    }

    public static int getSpawnerMinSpawnDelay() {
        ensureLoaded();
        return spawnerMinSpawnDelay;
    }

    public static int getSpawnerMaxSpawnDelay() {
        ensureLoaded();
        return spawnerMaxSpawnDelay;
    }

    public static int getSpawnerSpawnCount() {
        ensureLoaded();
        return spawnerSpawnCount;
    }

    public static int getSpawnerMaxNearbyEntities() {
        ensureLoaded();
        return spawnerMaxNearbyEntities;
    }

    public static int getSpawnerRequiredPlayerRange() {
        ensureLoaded();
        return spawnerRequiredPlayerRange;
    }

    public static int getSpawnerSpawnRange() {
        ensureLoaded();
        return spawnerSpawnRange;
    }

    public static boolean isSpawnerDisableMaxNearbyEntitiesCheck() {
        ensureLoaded();
        return spawnerDisableMaxNearbyEntitiesCheck;
    }

    public static boolean isSpawnerDisableNearbyPlayersCheck() {
        ensureLoaded();
        return spawnerDisableNearbyPlayersCheck;
    }

    public static boolean isSpawnerDisableObstructionCheck() {
        ensureLoaded();
        return spawnerDisableObstructionCheck;
    }

    public static boolean isSpawnerDisableWaterPreventSpawnCheck() {
        ensureLoaded();
        return spawnerDisableWaterPreventSpawnCheck;
    }

    public static boolean isAlternativeFarmsRaidsOldBehavior() {
        ensureLoaded();
        return alternativeFarmsRaidsOldBehavior;
    }

    public static boolean isAlternativeFarmsRaidsDropOminousBottles() {
        ensureLoaded();
        return alternativeFarmsRaidsDropOminousBottles;
    }

    public static double getCombatMaceLimitFallDistance() {
        ensureLoaded();
        return combatMaceLimitFallDistance;
    }

    public static boolean isCombatMaceIgnoreFallDistance() {
        ensureLoaded();
        return combatMaceIgnoreFallDistance;
    }

    public static boolean isCombatAlternativeHitRegistration() {
        ensureLoaded();
        return combatAlternativeHitRegistration;
    }

    public static boolean isCombatDisableKnockbackScaling() {
        ensureLoaded();
        return combatDisableKnockbackScaling;
    }

    public static boolean isCombatLegacyBlastProtection() {
        ensureLoaded();
        return combatLegacyBlastProtection;
    }

    public static boolean isCombatDisableShieldEffectiveness() {
        ensureLoaded();
        return combatDisableShieldEffectiveness;
    }

    public static boolean isCombatOldEnchantedGappleEffects() {
        ensureLoaded();
        return combatOldEnchantedGappleEffects;
    }

    public static boolean isCombatImitateSwordBlocking() {
        ensureLoaded();
        return combatImitateSwordBlocking;
    }

    public static boolean isCombatRevertArmorProtection() {
        ensureLoaded();
        return combatRevertArmorProtection;
    }

    public static boolean isCombatDisableNetheriteKnockbackResistance() {
        ensureLoaded();
        return combatDisableNetheriteKnockbackResistance;
    }

    public static boolean isCombatOldSharpnessDamageBuff() {
        ensureLoaded();
        return combatOldSharpnessDamageBuff;
    }

    public static boolean isCombatPreventCriticalsIfSprinting() {
        ensureLoaded();
        return combatPreventCriticalsIfSprinting;
    }

    public static boolean isCombatDisableHitDelay() {
        ensureLoaded();
        return combatDisableHitDelay;
    }

    public static boolean isCombatEnableBowBoosting() {
        ensureLoaded();
        return combatEnableBowBoosting;
    }

    public static boolean isCombatOldCollisionsProjectile() {
        ensureLoaded();
        return combatOldCollisionsProjectile;
    }

    public static boolean isCombatDisableSweepingEdge() {
        ensureLoaded();
        return combatDisableSweepingEdge;
    }

    public static boolean isCombatFishingHooksDoKnockback() {
        ensureLoaded();
        return combatFishingHooksDoKnockback;
    }

    public static boolean isCombatFishingHooksPullEntities() {
        ensureLoaded();
        return combatFishingHooksPullEntities;
    }

    public static boolean isCombatOldToolAttackDamage() {
        ensureLoaded();
        return combatOldToolAttackDamage;
    }

    public static double getCombatCriticalModifier() {
        ensureLoaded();
        return combatCriticalModifier;
    }

    public static boolean isCombatKnockbackOldKnockback() {
        ensureLoaded();
        return combatKnockbackOldKnockback;
    }

    public static double getCombatKnockbackVertical() {
        ensureLoaded();
        return combatKnockbackVertical;
    }

    public static double getCombatKnockbackHorizontal() {
        ensureLoaded();
        return combatKnockbackHorizontal;
    }

    public static double getCombatKnockbackFriction() {
        ensureLoaded();
        return combatKnockbackFriction;
    }

    public static double getCombatKnockbackVerticalLimit() {
        ensureLoaded();
        return combatKnockbackVerticalLimit;
    }

    private static Set<String> parseEntityTypeSet(List<String> entries, String path) {
        if (entries == null || entries.isEmpty()) {
            return Set.of();
        }

        final Set<String> parsed = new HashSet<>();
        for (String entry : entries) {
            if (entry == null || entry.isBlank()) {
                continue;
            }

            final String normalized = entry.trim().toUpperCase(Locale.ROOT);
            if (normalized.isEmpty()) {
                continue;
            }

            if (!parsed.add(normalized)) {
                Bukkit.getLogger().warning("[UlyxSpigot] Duplicate entity type in " + path + ": " + entry);
            }
        }

        return parsed.isEmpty() ? Set.of() : Collections.unmodifiableSet(parsed);
    }

    private static Map<String, Integer> parseEntityUpdateIntervals(List<String> entries) {
        if (entries == null || entries.isEmpty()) {
            return Map.of();
        }

        final Map<String, Integer> parsed = new HashMap<>();
        for (String entry : entries) {
            if (entry == null || entry.isBlank()) {
                continue;
            }

            final String[] split = entry.split(":", 2);
            if (split.length != 2) {
                Bukkit.getLogger().warning("[UlyxSpigot] Invalid entity-update-interval entry (expected ENTITY:NUMBER): " + entry);
                continue;
            }

            final String key = split[0].trim().toUpperCase(Locale.ROOT);
            if (key.isEmpty()) {
                Bukkit.getLogger().warning("[UlyxSpigot] Invalid entity-update-interval entry (empty entity type): " + entry);
                continue;
            }

            final int interval;
            try {
                interval = Integer.parseInt(split[1].trim());
            } catch (NumberFormatException ex) {
                Bukkit.getLogger().warning("[UlyxSpigot] Invalid entity-update-interval number in entry: " + entry);
                continue;
            }

            if (interval < 1) {
                Bukkit.getLogger().warning("[UlyxSpigot] entity-update-interval must be >= 1, got " + interval + " in entry: " + entry);
                continue;
            }

            parsed.put(key, interval);
        }

        return parsed.isEmpty() ? Map.of() : Collections.unmodifiableMap(parsed);
    }

    private static void set(String path, Object value) {
        config.addDefault(path, value);
        config.set(path, value);
    }

    private static String getString(String path, String def) {
        config.addDefault(path, def);
        return config.getString(path, def);
    }

    private static List<String> getStringList(String path, List<String> def) {
        config.addDefault(path, def);
        return new ArrayList<>(config.getStringList(path));
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
