package org.ulyxspigot.ulyxspigot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncDataSaving;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncInventoryUpdates;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncPacketSending;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncWorldTicking;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncPathfinding;
import org.ulyxspigot.ulyxspigot.async.UlyxAsyncTracker;
import org.ulyxspigot.ulyxspigot.virtual.UlyxVirtualThreadDispatcher;

public final class UlyxConfig {
    private static final String HEADER = "This is the main configuration file for UlyxSpigot.\n"
            + "These options are experimental and should be tested before production use.\n";
    private static final int CURRENT_CONFIG_VERSION = 1;
    private static final File CONFIG_FILE = new File("ulyxspigot", "ulyxspigot.yml");
    private static final Object LOAD_LOCK = new Object();

    private static volatile boolean loaded;
    private static volatile boolean asyncSystemsConfigured;
    private static YamlConfiguration config;

    private static String serverBrandNameDisplay = "UlyxSpigot";

    private static boolean useSparkTpsAtTpsCommand = false;
    private static boolean asyncTrackerEnabled = true;
    private static boolean asyncPathfindingEnabled = true;
    private static int asyncPathfindingThreads = 2;
    private static boolean asyncDataSavingEnabled = true;
    private static boolean asyncInventoryUpdatesEnabled = false;
    private static boolean asyncPacketSendingEnabled = false;
    private static boolean asyncWorldTickingEnabled = false;
    private static int asyncWorldTickingThreads = 0;

    private static boolean networkTcpFastOpen = false;

    private static boolean experimentalReducePlayerChunkSourceUpdates = true;
    private static boolean experimentalReduceChunkMidTickTaskExecution = true;
    private static boolean experimentalDisableChunkNewerVersionLoadCheck = false;
    private static boolean experimentalPrestartVirtualThreads = false;

    private static boolean developerRecalculateChunksOutOfBounds = false;
    private static boolean developerAllowInvalidEnchantLevels = false;
    private static boolean developerDisableAsyncCatcher = false;
    private static boolean developerDisableSessionLockFile = false;
    private static boolean developerEnableThreadsCommand = false;

    private static boolean miscLogCleanerEnabled = true;
    private static int miscLogCleanerOlderThan = 30;
    private static int miscLogCleanerMaxCount = -1;
    private static boolean miscDisableJoinMessage = false;
    private static boolean miscDisableQuitMessage = false;
    private static volatile boolean startupLogCleanerExecuted;

    private static boolean fixesDisableUnacknowledgedChatKick = true;
    private static boolean fixesFixPluginPlaceholderExploits = false;
    private static boolean fixesAllowTripwireDisarmingExploit = false;
    private static boolean fixesDisableInvalidItemWarn = false;
    private static boolean fixesUseSecureSeedLogic = false;
    private static boolean fixesAlternativeProfileLookup = false;
    private static boolean fixesDisableSavingSnowballs = true;
    private static boolean fixesDisableSavingFireworks = true;
    private static boolean fixesLockOpSystem = false;

    private static boolean limitersRedstoneEnabled = false;
    private static int limitersRedstoneMaxRedstonePerTick = 2000;
    private static int limitersRedstoneMaxPistonPerTick = 2000;
    private static int limitersRedstoneMaxHopperPerTick = 2000;
    private static int limitersRedstoneMaxDispenserPerTick = 2000;
    private static int limitersRedstoneMaxDropperPerTick = 2000;
    private static int limitersRedstoneMaxObserverPerTick = 2000;
    private static int limitersRedstoneMaxPistonPush = 12;
    private static Map<String, Integer> limitersRedstoneBlockThreshold = Map.of("OBSERVER", 2000);
    private static boolean limitersRemoveExcessMinecarts = true;
    private static boolean limitersRemoveExcessBoats = true;
    private static int limitersExcessMinecartsLimit = 5;
    private static int limitersExcessBoatsLimit = 5;
    private static List<String> limitersNonTickableEntities = List.of("EXAMPLE");
    private static Set<String> limitersNonTickableEntitiesSet = Set.of();

    private static boolean particlesDisableSprintParticles = false;
    private static boolean particlesDisableFallParticles = false;
    private static boolean particlesDisableDeathParticles = false;
    private static boolean particlesDisableBlockBreakParticles = false;
    private static boolean particlesDisableEffectParticles = false;
    private static boolean particlesDisableWaterSplashParticles = false;
    private static boolean particlesDisableBubbleColumnParticles = false;
    private static boolean particlesDisableSpawnerParticles = false;
    private static boolean particlesDisableNewCombatParticles = false;

    private static boolean soundsDisableShoulderEntityAmbientSound = false;
    private static boolean soundsDisableFootStepSounds = false;
    private static boolean soundsDisableNewCombatSounds = false;

    private static boolean performancePacketReducerEnabled = true;
    private static boolean performancePacketReducerReduceHandSwingUpdates = true;
    private static boolean performancePacketReducerFirePacketsEnabled = true;
    private static boolean performancePacketReducerFirePacketsIgnoreInvisible = true;
    private static boolean performanceOptimiseBlockEntities = true;
    private static boolean performanceVirtualThreadsEnabled = true;
    private static boolean performanceVirtualThreadsCommandLogging = false;
    private static boolean performanceVirtualThreadsTabCompleting = true;
    private static boolean performanceVirtualThreadsCommandSending = true;
    private static boolean performanceVirtualThreadsChatTextFiltering = true;
    private static boolean performanceVirtualThreadsAuthenticatorScheduler = true;
    private static boolean performanceAlwaysMoistFarmland = false;
    private static boolean performanceAlwaysMoistSugarcane = false;
    private static boolean performanceCheckIfCactusCanSurviveBeforeGrowth = false;
    private static boolean performanceOnlyPlayersUnpackLootTable = true;
    private static boolean performanceOptimisePlayerMovement = true;
    private static boolean performanceOptimisePlayerPickup = true;
    private static boolean performanceOptimiseRails = false;
    private static boolean performanceDisableCriterionTrigger = true;
    private static boolean performanceDisableBlockEntityTicking = true;
    private static boolean performanceDisableTileSnapshotCreation = false;
    private static boolean performanceDisableBlockSnapshotCreation = false;
    private static boolean performanceDisableSpawnerChunkTickIteration = true;
    private static int performanceTickingWorldTicksBetweenRaidTicking = 1;
    private static int performanceTickingWorldTicksBetweenStatisticUpdate = 20;
    private static boolean performanceDisableBukkitVanishAPI = false;

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
            maybeConfigureAsyncSystems();
            return;
        }
        synchronized (LOAD_LOCK) {
            if (!loaded) {
                loadConfig();
                runStartupLogCleanerIfNeeded();
                loaded = true;
            }
            maybeConfigureAsyncSystems();
        }
    }

    public static void reload() {
        synchronized (LOAD_LOCK) {
            loadConfig();
            loaded = true;
        }
    }

    public static List<String> getReloadBlockedOptionsFromFile() {
        final YamlConfiguration preview = new YamlConfiguration();
        try {
            preview.load(CONFIG_FILE);
        } catch (IOException ignore) {
        } catch (InvalidConfigurationException ex) {
            throw new RuntimeException(ex);
        }

        final List<String> blockedOptions = new ArrayList<>(3);
        if (preview.getBoolean("experimental.disableChunkNewerVersionLoadCheck", false)) {
            blockedOptions.add("experimental.disableChunkNewerVersionLoadCheck");
        }
        if (preview.getBoolean("developer.disableSessionLockFile", false)) {
            blockedOptions.add("developer.disableSessionLockFile");
        }
        if (preview.getBoolean("asynchronous.world-ticking.enabled", false)) {
            blockedOptions.add("asynchronous.world-ticking.enabled");
        }
        return blockedOptions;
    }

    private static void maybeConfigureAsyncSystems() {
        if (asyncSystemsConfigured || Bukkit.getServer() == null) {
            return;
        }

        synchronized (LOAD_LOCK) {
            if (asyncSystemsConfigured || Bukkit.getServer() == null) {
                return;
            }

            UlyxAsyncTracker.reconfigure(asyncTrackerEnabled);
            UlyxAsyncPathfinding.reconfigure(asyncPathfindingEnabled, asyncPathfindingThreads);
            UlyxAsyncPacketSending.reconfigure(asyncPacketSendingEnabled);
            UlyxAsyncWorldTicking.reconfigure(asyncWorldTickingEnabled, asyncWorldTickingThreads);
            UlyxAsyncDataSaving.reconfigure(asyncDataSavingEnabled);
            UlyxAsyncInventoryUpdates.reconfigure(asyncInventoryUpdatesEnabled);
            asyncSystemsConfigured = true;
        }
    }

    private static java.util.logging.Logger logger() {
        final org.bukkit.Server server = Bukkit.getServer();
        return server != null ? server.getLogger() : java.util.logging.Logger.getLogger("UlyxSpigot");
    }

    private static void loadConfig() {
        final File parent = CONFIG_FILE.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            logger().log(Level.WARNING, "Could not create config directory " + parent.getAbsolutePath());
        }

        copyDefaultConfigIfMissing();

        config = new YamlConfiguration();
        try {
            config.load(CONFIG_FILE);
        } catch (IOException ignore) {
        } catch (InvalidConfigurationException ex) {
            logger().log(Level.SEVERE, "Could not load ulyxspigot.yml, please correct your syntax errors", ex);
            throw new RuntimeException(ex);
        }

        config.options().header(HEADER);
        config.options().parseComments(true);
        config.options().copyDefaults(true);

        set("config-version", CURRENT_CONFIG_VERSION);

        serverBrandNameDisplay = getString("server-brand-name-display", serverBrandNameDisplay);
        useSparkTpsAtTpsCommand = getBoolean("use-spark-tps-at-tps-command", useSparkTpsAtTpsCommand);

        asyncTrackerEnabled = getBoolean("asynchronous.tracker.enabled", asyncTrackerEnabled);
        asyncPathfindingEnabled = getBoolean("asynchronous.pathfinding.enabled", asyncPathfindingEnabled);
        asyncPathfindingThreads = Math.max(0, getInt("asynchronous.pathfinding.threads", asyncPathfindingThreads));
        asyncDataSavingEnabled = getBoolean("asynchronous.data-saving.enabled", asyncDataSavingEnabled);
        asyncInventoryUpdatesEnabled = getBoolean("asynchronous.inventory-updates.enabled", asyncInventoryUpdatesEnabled);
        asyncPacketSendingEnabled = getBoolean("asynchronous.packet-sending.enabled", asyncPacketSendingEnabled);
        asyncWorldTickingEnabled = getBoolean("asynchronous.world-ticking.enabled", asyncWorldTickingEnabled);
        asyncWorldTickingThreads = Math.max(0, getInt("asynchronous.world-ticking.threads", asyncWorldTickingThreads));

        networkTcpFastOpen = getBoolean("network.tcp-fast-open", networkTcpFastOpen);

        experimentalReducePlayerChunkSourceUpdates = getBoolean("experimental.reducePlayerChunkSourceUpdates", experimentalReducePlayerChunkSourceUpdates);
        experimentalReduceChunkMidTickTaskExecution = getBoolean("experimental.reduceChunkMidTickTaskExecution", experimentalReduceChunkMidTickTaskExecution);
        experimentalDisableChunkNewerVersionLoadCheck = getBoolean("experimental.disableChunkNewerVersionLoadCheck", experimentalDisableChunkNewerVersionLoadCheck);
        experimentalPrestartVirtualThreads = getBoolean("experimental.prestartVirtualThreads", experimentalPrestartVirtualThreads);

        developerRecalculateChunksOutOfBounds = getBoolean("developer.recalculateChunksOutOfBounds", developerRecalculateChunksOutOfBounds);
        developerAllowInvalidEnchantLevels = getBoolean("developer.allowInvalidEnchantLevels", developerAllowInvalidEnchantLevels);
        developerDisableAsyncCatcher = getBoolean("developer.disableAsyncCatcher", developerDisableAsyncCatcher);
        developerDisableSessionLockFile = getBoolean("developer.disableSessionLockFile", developerDisableSessionLockFile);
        developerEnableThreadsCommand = getBoolean("developer.enable-threads-command", developerEnableThreadsCommand);

        miscLogCleanerEnabled = getBoolean("misc.log-cleaner.enabled", miscLogCleanerEnabled);
        miscLogCleanerOlderThan = getInt("misc.log-cleaner.older-than", miscLogCleanerOlderThan);
        miscLogCleanerMaxCount = getInt("misc.log-cleaner.max-count", miscLogCleanerMaxCount);
        miscDisableJoinMessage = getBoolean("misc.disableJoinMessage", miscDisableJoinMessage);
        miscDisableQuitMessage = getBoolean("misc.disableQuitMessage", miscDisableQuitMessage);

        fixesDisableUnacknowledgedChatKick = getBoolean("fixes.disableUnacknowledgedChatKick", fixesDisableUnacknowledgedChatKick);
        fixesFixPluginPlaceholderExploits = getBoolean("fixes.fixPluginPlaceholderExploits", fixesFixPluginPlaceholderExploits);
        fixesAllowTripwireDisarmingExploit = getBoolean("fixes.allowTripwireDisarmingExploit", fixesAllowTripwireDisarmingExploit);
        fixesDisableInvalidItemWarn = getBoolean("fixes.disableInvalidItemWarn", fixesDisableInvalidItemWarn);
        fixesUseSecureSeedLogic = getBoolean("fixes.useSecureSeedLogic", fixesUseSecureSeedLogic);
        fixesAlternativeProfileLookup = getBoolean("fixes.alternativeProfileLookup", fixesAlternativeProfileLookup);
        fixesDisableSavingSnowballs = getBoolean("fixes.disableSavingSnowballs", fixesDisableSavingSnowballs);
        fixesDisableSavingFireworks = getBoolean("fixes.disableSavingFireworks", fixesDisableSavingFireworks);
        fixesLockOpSystem = getBoolean("fixes.lockOpSystem", fixesLockOpSystem);

        limitersRedstoneEnabled = getBoolean("limiters.redstone.enabled", limitersRedstoneEnabled);
        limitersRedstoneMaxRedstonePerTick = Math.max(0, getInt("limiters.redstone.maxRedstonePerTick", limitersRedstoneMaxRedstonePerTick));
        limitersRedstoneMaxPistonPerTick = Math.max(0, getInt("limiters.redstone.maxPistonPerTick", limitersRedstoneMaxPistonPerTick));
        limitersRedstoneMaxHopperPerTick = Math.max(0, getInt("limiters.redstone.maxHopperPerTick", limitersRedstoneMaxHopperPerTick));
        limitersRedstoneMaxDispenserPerTick = Math.max(0, getInt("limiters.redstone.maxDispenserPerTick", limitersRedstoneMaxDispenserPerTick));
        limitersRedstoneMaxDropperPerTick = Math.max(0, getInt("limiters.redstone.maxDropperPerTick", limitersRedstoneMaxDropperPerTick));
        limitersRedstoneMaxObserverPerTick = Math.max(0, getInt("limiters.redstone.maxObserverPerTick", limitersRedstoneMaxObserverPerTick));
        limitersRedstoneMaxPistonPush = Math.max(0, getInt("limiters.redstone.maxPistonPush", limitersRedstoneMaxPistonPush));
        limitersRedstoneBlockThreshold = parseKeyIntSection("limiters.redstone.block-threshold", limitersRedstoneBlockThreshold);
        limitersRemoveExcessMinecarts = getBoolean("limiters.remove-excess.removeExcessMinecarts", limitersRemoveExcessMinecarts);
        limitersRemoveExcessBoats = getBoolean("limiters.remove-excess.removeExcessBoats", limitersRemoveExcessBoats);
        limitersExcessMinecartsLimit = Math.max(0, getInt("limiters.remove-excess.excessMinecartsLimit", limitersExcessMinecartsLimit));
        limitersExcessBoatsLimit = Math.max(0, getInt("limiters.remove-excess.excessBoatsLimit", limitersExcessBoatsLimit));
        limitersNonTickableEntities = getStringList("limiters.non-tickable-entities", limitersNonTickableEntities);
        limitersNonTickableEntitiesSet = parseEntityTypeSet(limitersNonTickableEntities, "limiters.non-tickable-entities");

        particlesDisableSprintParticles = getBoolean("particles.disableSprintParticles", particlesDisableSprintParticles);
        particlesDisableFallParticles = getBoolean("particles.disableFallParticles", particlesDisableFallParticles);
        particlesDisableDeathParticles = getBoolean("particles.disableDeathParticles", particlesDisableDeathParticles);
        particlesDisableBlockBreakParticles = getBoolean("particles.disableBlockBreakParticles", particlesDisableBlockBreakParticles);
        particlesDisableEffectParticles = getBoolean("particles.disableEffectParticles", particlesDisableEffectParticles);
        particlesDisableWaterSplashParticles = getBoolean("particles.disableWaterSplashParticles", particlesDisableWaterSplashParticles);
        particlesDisableBubbleColumnParticles = getBoolean("particles.disableBubbleColumnParticles", particlesDisableBubbleColumnParticles);
        particlesDisableSpawnerParticles = getBoolean("particles.disableSpawnerParticles", particlesDisableSpawnerParticles);
        particlesDisableNewCombatParticles = getBoolean("particles.disableNewCombatParticles", particlesDisableNewCombatParticles);

        soundsDisableShoulderEntityAmbientSound = getBoolean("sounds.disableShoulderEntityAmbientSound", soundsDisableShoulderEntityAmbientSound);
        soundsDisableFootStepSounds = getBoolean("sounds.disableFootStepSounds", soundsDisableFootStepSounds);
        soundsDisableNewCombatSounds = getBoolean("sounds.disableNewCombatSounds", soundsDisableNewCombatSounds);

        performancePacketReducerEnabled = getBoolean("performance.packet-reducer.enabled", performancePacketReducerEnabled);
        performancePacketReducerReduceHandSwingUpdates = getBoolean("performance.packet-reducer.reduceHandSwingUpdates", performancePacketReducerReduceHandSwingUpdates);
        performancePacketReducerFirePacketsEnabled = getBoolean("performance.packet-reducer.fire-packets.enabled", performancePacketReducerFirePacketsEnabled);
        performancePacketReducerFirePacketsIgnoreInvisible = getBoolean("performance.packet-reducer.fire-packets.ignore-invisible", performancePacketReducerFirePacketsIgnoreInvisible);
        performanceOptimiseBlockEntities = getBoolean("performance.optimiseBlockEntities", performanceOptimiseBlockEntities);
        performanceVirtualThreadsEnabled = getBoolean("performance.virtual-threads.enabled", performanceVirtualThreadsEnabled);
        performanceVirtualThreadsCommandLogging = getBoolean("performance.virtual-threads.command-logging", performanceVirtualThreadsCommandLogging);
        performanceVirtualThreadsTabCompleting = getBoolean("performance.virtual-threads.tab-completing", performanceVirtualThreadsTabCompleting);
        performanceVirtualThreadsCommandSending = getBoolean("performance.virtual-threads.command-sending", performanceVirtualThreadsCommandSending);
        performanceVirtualThreadsChatTextFiltering = getBoolean("performance.virtual-threads.chat-text-filtering", performanceVirtualThreadsChatTextFiltering);
        performanceVirtualThreadsAuthenticatorScheduler = getBoolean("performance.virtual-threads.authenticator-scheduler", performanceVirtualThreadsAuthenticatorScheduler);
        UlyxVirtualThreadDispatcher.reconfigure(
            experimentalPrestartVirtualThreads && performanceVirtualThreadsEnabled,
            performanceVirtualThreadsCommandSending,
            performanceVirtualThreadsCommandLogging,
            performanceVirtualThreadsChatTextFiltering,
            performanceVirtualThreadsTabCompleting,
            performanceVirtualThreadsAuthenticatorScheduler
        );
        performanceAlwaysMoistFarmland = getBoolean("performance.alwaysMoistFarmland", performanceAlwaysMoistFarmland);
        performanceAlwaysMoistSugarcane = getBoolean("performance.alwaysMoistSugarcane", performanceAlwaysMoistSugarcane);
        performanceCheckIfCactusCanSurviveBeforeGrowth = getBoolean("performance.checkIfCactusCanSurviveBeforeGrowth", performanceCheckIfCactusCanSurviveBeforeGrowth);
        performanceOnlyPlayersUnpackLootTable = getBoolean("performance.onlyPlayersUnpackLootTable", performanceOnlyPlayersUnpackLootTable);
        performanceOptimisePlayerMovement = getBoolean("performance.optimisePlayerMovement", performanceOptimisePlayerMovement);
        performanceOptimiseRails = getBoolean("performance.optimiseRails", performanceOptimiseRails);
        performanceOptimisePlayerPickup = getBoolean("performance.optimisePlayerPickup", performanceOptimisePlayerPickup);
        performanceDisableCriterionTrigger = getBoolean("performance.disableCriterionTrigger", performanceDisableCriterionTrigger);
        performanceDisableBlockEntityTicking = getBoolean("performance.disableBlockEntityTicking", performanceDisableBlockEntityTicking);
        performanceDisableTileSnapshotCreation = getBoolean("performance.disableTileSnapshotCreation", performanceDisableTileSnapshotCreation);
        performanceDisableBlockSnapshotCreation = getBoolean("performance.disableBlockSnapshotCreation", performanceDisableBlockSnapshotCreation);
        performanceDisableSpawnerChunkTickIteration = getBoolean("performance.disableSpawnerChunkTickIteration", performanceDisableSpawnerChunkTickIteration);
        performanceTickingWorldTicksBetweenRaidTicking = Math.max(1, getInt("performance.ticking.world.ticksBetweenRaidTicking", performanceTickingWorldTicksBetweenRaidTicking));
        performanceTickingWorldTicksBetweenStatisticUpdate = Math.max(1, getInt("performance.ticking.world.ticksBetweenStatisticUpdate", performanceTickingWorldTicksBetweenStatisticUpdate));
        performanceDisableBukkitVanishAPI = getBoolean("performance.disableBukkitVanishAPI", performanceDisableBukkitVanishAPI);

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
        asyncSystemsConfigured = false;
        maybeConfigureAsyncSystems();

        try {
            config.save(CONFIG_FILE);
        } catch (IOException ex) {
            logger().log(Level.SEVERE, "Could not save " + CONFIG_FILE, ex);
        }
    }

    public static String getServerBrandNameDisplay() {
        ensureLoaded();
        return serverBrandNameDisplay;
    }

    public static boolean isUseSparkTpsAtTpsCommand() {
        ensureLoaded();
        return useSparkTpsAtTpsCommand;
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

    public static boolean isAsyncWorldTickingEnabled() {
        ensureLoaded();
        return asyncWorldTickingEnabled;
    }

    public static int getAsyncWorldTickingThreads() {
        ensureLoaded();
        return asyncWorldTickingThreads;
    }

    public static boolean isNetworkTcpFastOpen() {
        ensureLoaded();
        return networkTcpFastOpen;
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

    public static boolean isExperimentalPrestartVirtualThreads() {
        ensureLoaded();
        return experimentalPrestartVirtualThreads;
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

    public static boolean isDeveloperEnableThreadsCommand() {
        ensureLoaded();
        return developerEnableThreadsCommand;
    }

    public static boolean isMiscLogCleanerEnabled() {
        ensureLoaded();
        return miscLogCleanerEnabled;
    }

    public static int getMiscLogCleanerOlderThan() {
        ensureLoaded();
        return miscLogCleanerOlderThan;
    }

    public static int getMiscLogCleanerMaxCount() {
        ensureLoaded();
        return miscLogCleanerMaxCount;
    }

    public static boolean isMiscDisableJoinMessage() {
        ensureLoaded();
        return miscDisableJoinMessage;
    }

    public static boolean isMiscDisableQuitMessage() {
        ensureLoaded();
        return miscDisableQuitMessage;
    }

    public static boolean isFixesDisableUnacknowledgedChatKick() {
        ensureLoaded();
        return fixesDisableUnacknowledgedChatKick;
    }

    public static boolean isFixesFixPluginPlaceholderExploits() {
        ensureLoaded();
        return fixesFixPluginPlaceholderExploits;
    }

    public static boolean isFixesAllowTripwireDisarmingExploit() {
        ensureLoaded();
        return fixesAllowTripwireDisarmingExploit;
    }

    public static boolean isFixesDisableInvalidItemWarn() {
        ensureLoaded();
        return fixesDisableInvalidItemWarn;
    }

    public static boolean isFixesUseSecureSeedLogic() {
        ensureLoaded();
        return fixesUseSecureSeedLogic;
    }

    public static boolean isFixesAlternativeProfileLookup() {
        ensureLoaded();
        return fixesAlternativeProfileLookup;
    }

    public static boolean isFixesDisableSavingSnowballs() {
        ensureLoaded();
        return fixesDisableSavingSnowballs;
    }

    public static boolean isFixesDisableSavingFireworks() {
        ensureLoaded();
        return fixesDisableSavingFireworks;
    }

    public static boolean isFixesLockOpSystem() {
        ensureLoaded();
        return fixesLockOpSystem;
    }

    public static boolean isLimitersRedstoneEnabled() {
        ensureLoaded();
        return limitersRedstoneEnabled;
    }

    public static int getLimitersRedstoneMaxRedstonePerTick() {
        ensureLoaded();
        return limitersRedstoneMaxRedstonePerTick;
    }

    public static int getLimitersRedstoneMaxPistonPerTick() {
        ensureLoaded();
        return limitersRedstoneMaxPistonPerTick;
    }

    public static int getLimitersRedstoneMaxHopperPerTick() {
        ensureLoaded();
        return limitersRedstoneMaxHopperPerTick;
    }

    public static int getLimitersRedstoneMaxDispenserPerTick() {
        ensureLoaded();
        return limitersRedstoneMaxDispenserPerTick;
    }

    public static int getLimitersRedstoneMaxDropperPerTick() {
        ensureLoaded();
        return limitersRedstoneMaxDropperPerTick;
    }

    public static int getLimitersRedstoneMaxObserverPerTick() {
        ensureLoaded();
        return limitersRedstoneMaxObserverPerTick;
    }

    public static int getLimitersRedstoneMaxPistonPush() {
        ensureLoaded();
        return limitersRedstoneMaxPistonPush;
    }

    public static int getLimitersRedstoneBlockThreshold(String key) {
        ensureLoaded();
        if (key == null) {
            return -1;
        }
        return limitersRedstoneBlockThreshold.getOrDefault(key.trim().toUpperCase(Locale.ROOT), -1);
    }

    public static boolean isLimitersRemoveExcessMinecarts() {
        ensureLoaded();
        return limitersRemoveExcessMinecarts;
    }

    public static boolean isLimitersRemoveExcessBoats() {
        ensureLoaded();
        return limitersRemoveExcessBoats;
    }

    public static int getLimitersExcessMinecartsLimit() {
        ensureLoaded();
        return limitersExcessMinecartsLimit;
    }

    public static int getLimitersExcessBoatsLimit() {
        ensureLoaded();
        return limitersExcessBoatsLimit;
    }

    public static List<String> getLimitersNonTickableEntities() {
        ensureLoaded();
        return limitersNonTickableEntities;
    }

    public static boolean isLimitersNonTickableEntity(String entityTypeName) {
        ensureLoaded();
        if (entityTypeName == null) {
            return false;
        }
        return limitersNonTickableEntitiesSet.contains(entityTypeName.trim().toUpperCase(Locale.ROOT));
    }

    public static boolean isParticlesDisableSprintParticles() {
        ensureLoaded();
        return particlesDisableSprintParticles;
    }

    public static boolean isParticlesDisableFallParticles() {
        ensureLoaded();
        return particlesDisableFallParticles;
    }

    public static boolean isParticlesDisableDeathParticles() {
        ensureLoaded();
        return particlesDisableDeathParticles;
    }

    public static boolean isParticlesDisableBlockBreakParticles() {
        ensureLoaded();
        return particlesDisableBlockBreakParticles;
    }

    public static boolean isParticlesDisableEffectParticles() {
        ensureLoaded();
        return particlesDisableEffectParticles;
    }

    public static boolean isParticlesDisableWaterSplashParticles() {
        ensureLoaded();
        return particlesDisableWaterSplashParticles;
    }

    public static boolean isParticlesDisableBubbleColumnParticles() {
        ensureLoaded();
        return particlesDisableBubbleColumnParticles;
    }

    public static boolean isParticlesDisableSpawnerParticles() {
        ensureLoaded();
        return particlesDisableSpawnerParticles;
    }

    public static boolean isParticlesDisableNewCombatParticles() {
        ensureLoaded();
        return particlesDisableNewCombatParticles;
    }

    public static boolean isSoundsDisableShoulderEntityAmbientSound() {
        ensureLoaded();
        return soundsDisableShoulderEntityAmbientSound;
    }

    public static boolean isSoundsDisableFootStepSounds() {
        ensureLoaded();
        return soundsDisableFootStepSounds;
    }

    public static boolean isSoundsDisableNewCombatSounds() {
        ensureLoaded();
        return soundsDisableNewCombatSounds;
    }

    public static boolean isPerformancePacketReducerEnabled() {
        ensureLoaded();
        return performancePacketReducerEnabled;
    }

    public static boolean isPerformancePacketReducerReduceHandSwingUpdates() {
        ensureLoaded();
        return performancePacketReducerReduceHandSwingUpdates;
    }

    public static boolean isPerformancePacketReducerFirePacketsEnabled() {
        ensureLoaded();
        return performancePacketReducerFirePacketsEnabled;
    }

    public static boolean isPerformancePacketReducerFirePacketsIgnoreInvisible() {
        ensureLoaded();
        return performancePacketReducerFirePacketsIgnoreInvisible;
    }

    public static boolean isPerformanceOptimiseBlockEntities() {
        ensureLoaded();
        return performanceOptimiseBlockEntities;
    }

    public static boolean isPerformanceVirtualThreadsEnabled() {
        ensureLoaded();
        return performanceVirtualThreadsEnabled;
    }

    public static boolean isPerformanceVirtualThreadsCommandLogging() {
        ensureLoaded();
        return performanceVirtualThreadsCommandLogging;
    }

    public static boolean isPerformanceVirtualThreadsTabCompleting() {
        ensureLoaded();
        return performanceVirtualThreadsTabCompleting;
    }

    public static boolean isPerformanceVirtualThreadsCommandSending() {
        ensureLoaded();
        return performanceVirtualThreadsCommandSending;
    }

    public static boolean isPerformanceVirtualThreadsChatTextFiltering() {
        ensureLoaded();
        return performanceVirtualThreadsChatTextFiltering;
    }

    public static boolean isPerformanceVirtualThreadsAuthenticatorScheduler() {
        ensureLoaded();
        return performanceVirtualThreadsAuthenticatorScheduler;
    }

    public static boolean isPerformanceAlwaysMoistFarmland() {
        ensureLoaded();
        return performanceAlwaysMoistFarmland;
    }

    public static boolean isPerformanceAlwaysMoistSugarcane() {
        ensureLoaded();
        return performanceAlwaysMoistSugarcane;
    }

    public static boolean isPerformanceCheckIfCactusCanSurviveBeforeGrowth() {
        ensureLoaded();
        return performanceCheckIfCactusCanSurviveBeforeGrowth;
    }

    public static boolean isPerformanceOnlyPlayersUnpackLootTable() {
        ensureLoaded();
        return performanceOnlyPlayersUnpackLootTable;
    }

    public static boolean isPerformanceOptimisePlayerMovement() {
        ensureLoaded();
        return performanceOptimisePlayerMovement;
    }

    public static boolean isPerformanceOptimisePlayerPickup() {
        ensureLoaded();
        return performanceOptimisePlayerPickup;
    }

    public static boolean isPerformanceOptimiseRails() {
        ensureLoaded();
        return performanceOptimiseRails;
    }

    public static boolean isPerformanceDisableCriterionTrigger() {
        ensureLoaded();
        return performanceDisableCriterionTrigger;
    }

    public static boolean isPerformanceDisableBlockEntityTicking() {
        ensureLoaded();
        return performanceDisableBlockEntityTicking;
    }

    public static boolean isPerformanceDisableTileSnapshotCreation() {
        ensureLoaded();
        return performanceDisableTileSnapshotCreation;
    }

    public static boolean isPerformanceDisableBlockSnapshotCreation() {
        ensureLoaded();
        return performanceDisableBlockSnapshotCreation;
    }

    public static boolean isPerformanceDisableSpawnerChunkTickIteration() {
        ensureLoaded();
        return performanceDisableSpawnerChunkTickIteration;
    }

    public static int getPerformanceTickingWorldTicksBetweenRaidTicking() {
        ensureLoaded();
        return performanceTickingWorldTicksBetweenRaidTicking;
    }

    public static int getPerformanceTickingWorldTicksBetweenStatisticUpdate() {
        ensureLoaded();
        return performanceTickingWorldTicksBetweenStatisticUpdate;
    }

    public static boolean isPerformanceDisableBukkitVanishAPI() {
        ensureLoaded();
        return performanceDisableBukkitVanishAPI;
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


    private static void copyDefaultConfigIfMissing() {
        if (CONFIG_FILE.exists()) {
            return;
        }

        try (InputStream input = UlyxConfig.class.getClassLoader().getResourceAsStream("configurations/ulyxspigot.yml")) {
            if (input == null) {
                logger().warning("[UlyxSpigot] Could not find default ulyxspigot.yml template resource");
                return;
            }
            Files.copy(input, CONFIG_FILE.toPath());
        } catch (IOException ex) {
            logger().log(Level.WARNING, "[UlyxSpigot] Failed to create default ulyxspigot.yml", ex);
        }
    }

    private static void runStartupLogCleanerIfNeeded() {
        if (startupLogCleanerExecuted) {
            return;
        }
        startupLogCleanerExecuted = true;

        if (!miscLogCleanerEnabled) {
            return;
        }

        final Path logsDirectory = Path.of("logs");
        if (!Files.isDirectory(logsDirectory)) {
            return;
        }

        final List<Path> files = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.list(logsDirectory)) {
            stream.filter(Files::isRegularFile).filter(UlyxConfig::isLogFile).forEach(files::add);
        } catch (IOException ex) {
            logger().log(Level.WARNING, "[UlyxSpigot] Failed to list log files", ex);
            return;
        }

        if (files.isEmpty()) {
            return;
        }

        int deleted = 0;
        if (miscLogCleanerOlderThan >= 0) {
            final long cutoff = System.currentTimeMillis() - (miscLogCleanerOlderThan * 24L * 60L * 60L * 1000L);
            for (Path file : new ArrayList<>(files)) {
                try {
                    if (Files.getLastModifiedTime(file).toMillis() < cutoff && Files.deleteIfExists(file)) {
                        files.remove(file);
                        deleted++;
                    }
                } catch (IOException ex) {
                    logger().log(Level.WARNING, "[UlyxSpigot] Failed to delete old log file " + file.getFileName(), ex);
                }
            }
        }

        if (miscLogCleanerMaxCount >= 0 && files.size() > miscLogCleanerMaxCount) {
            files.sort((a, b) -> {
                try {
                    return Long.compare(Files.getLastModifiedTime(b).toMillis(), Files.getLastModifiedTime(a).toMillis());
                } catch (IOException ex) {
                    return 0;
                }
            });

            for (int i = miscLogCleanerMaxCount; i < files.size(); i++) {
                final Path file = files.get(i);
                try {
                    if (Files.deleteIfExists(file)) {
                        deleted++;
                    }
                } catch (IOException ex) {
                    logger().log(Level.WARNING, "[UlyxSpigot] Failed to delete extra log file " + file.getFileName(), ex);
                }
            }
        }

        if (deleted > 0) {
            logger().info("[UlyxSpigot] Log cleaner removed " + deleted + " file(s) from logs/");
        }
    }

    private static boolean isLogFile(Path path) {
        final String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".log") || name.endsWith(".log.gz");
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
                logger().warning("[UlyxSpigot] Duplicate entity type in " + path + ": " + entry);
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
                logger().warning("[UlyxSpigot] Invalid entity-update-interval entry (expected ENTITY:NUMBER): " + entry);
                continue;
            }

            final String key = split[0].trim().toUpperCase(Locale.ROOT);
            if (key.isEmpty()) {
                logger().warning("[UlyxSpigot] Invalid entity-update-interval entry (empty entity type): " + entry);
                continue;
            }

            final int interval;
            try {
                interval = Integer.parseInt(split[1].trim());
            } catch (NumberFormatException ex) {
                logger().warning("[UlyxSpigot] Invalid entity-update-interval number in entry: " + entry);
                continue;
            }

            if (interval < 1) {
                logger().warning("[UlyxSpigot] entity-update-interval must be >= 1, got " + interval + " in entry: " + entry);
                continue;
            }

            parsed.put(key, interval);
        }

        return parsed.isEmpty() ? Map.of() : Collections.unmodifiableMap(parsed);
    }

    private static Map<String, Integer> parseKeyIntSection(String path, Map<String, Integer> defaults) {
        final Map<String, Integer> fallback = defaults == null || defaults.isEmpty()
            ? Map.of()
            : Collections.unmodifiableMap(new HashMap<>(defaults));

        final ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            config.addDefault(path, fallback);
            return fallback;
        }

        final Map<String, Integer> parsed = new HashMap<>();
        for (String key : section.getKeys(false)) {
            if (key == null || key.isBlank()) {
                continue;
            }

            final String normalized = key.trim().toUpperCase(Locale.ROOT);
            final int value = section.getInt(key, fallback.getOrDefault(normalized, 0));
            if (value < 0) {
                logger().warning("[UlyxSpigot] " + path + "." + key + " must be >= 0, got " + value);
                continue;
            }

            parsed.put(normalized, value);
        }

        if (parsed.isEmpty()) {
            return fallback;
        }
        return Collections.unmodifiableMap(parsed);
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
