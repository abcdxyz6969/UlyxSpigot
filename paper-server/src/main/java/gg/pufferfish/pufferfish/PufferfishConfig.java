package gg.pufferfish.pufferfish;

import gg.pufferfish.pufferfish.simd.SIMDDetection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class PufferfishConfig {
    private static final String HEADER = "Pufferfish compatibility configuration for UlyxSpigot.\n"
        + "Only a subset is currently active. More options can be mapped gradually.\n";
    private static final int CURRENT_CONFIG_VERSION = 1;
    private static final File CONFIG_FILE = new File("pufferfish.yml");
    private static final Object LOAD_LOCK = new Object();

    private static volatile boolean loaded;
    private static YamlConfiguration config;

    private static boolean tpsCatchup = true;
    private static boolean enableBooks = true;
    private static boolean enableSuffocationOptimization = true;
    private static boolean inactiveGoalSelectorThrottle = true;
    private static boolean allowEndCrystalRespawn = true;
    private static int maxProjectileLoadsPerTick = 10;
    private static int maxProjectileLoadsPerProjectile = 10;

    private PufferfishConfig() {
    }

    public static void ensureLoaded() {
        if (loaded) {
            return;
        }
        synchronized (LOAD_LOCK) {
            if (!loaded) {
                loadConfig();
                loaded = true;
            }
        }
    }

    public static void reload() {
        synchronized (LOAD_LOCK) {
            loadConfig();
            loaded = true;
        }
    }

    public static boolean isTpsCatchup() {
        ensureLoaded();
        return tpsCatchup;
    }

    public static boolean isEnableBooks() {
        ensureLoaded();
        return enableBooks;
    }

    public static boolean isEnableSuffocationOptimization() {
        ensureLoaded();
        return enableSuffocationOptimization;
    }

    public static boolean isInactiveGoalSelectorThrottle() {
        ensureLoaded();
        return inactiveGoalSelectorThrottle;
    }

    public static boolean isAllowEndCrystalRespawn() {
        ensureLoaded();
        return allowEndCrystalRespawn;
    }

    public static int getMaxProjectileLoadsPerTick() {
        ensureLoaded();
        return maxProjectileLoadsPerTick;
    }

    public static int getMaxProjectileLoadsPerProjectile() {
        ensureLoaded();
        return maxProjectileLoadsPerProjectile;
    }

    private static void loadConfig() {
        copyDefaultConfigIfMissing();

        config = new YamlConfiguration();
        try {
            config.load(CONFIG_FILE);
        } catch (IOException ignore) {
        } catch (InvalidConfigurationException ex) {
            logger().log(Level.SEVERE, "Could not load pufferfish.yml, please correct your syntax errors", ex);
            throw new RuntimeException(ex);
        }

        config.options().header(HEADER);
        config.options().parseComments(true);
        config.options().copyDefaults(true);

        set("config-version", CURRENT_CONFIG_VERSION);
        set("info.version", "1.0");

        tpsCatchup = getBoolean("tps-catchup", tpsCatchup);
        enableBooks = getBoolean("enable-books", enableBooks);
        enableSuffocationOptimization = getBoolean("enable-suffocation-optimization", enableSuffocationOptimization);
        inactiveGoalSelectorThrottle = getBoolean("inactive-goal-selector-throttle", inactiveGoalSelectorThrottle);
        allowEndCrystalRespawn = getBoolean("allow-end-crystal-respawn", allowEndCrystalRespawn);
        maxProjectileLoadsPerTick = getInt("projectile.max-loads-per-tick", maxProjectileLoadsPerTick);
        maxProjectileLoadsPerProjectile = getInt("projectile.max-loads-per-projectile", maxProjectileLoadsPerProjectile);

        saveConfig();
        detectSimd();
    }

    private static void detectSimd() {
        try {
            SIMDDetection.isEnabled = SIMDDetection.canEnable(logger());
            SIMDDetection.versionLimited = SIMDDetection.getJavaVersion() < 17 || SIMDDetection.getJavaVersion() > 25;
        } catch (Throwable throwable) {
            SIMDDetection.isEnabled = false;
            logger().log(Level.FINE, "[Pufferfish] Failed to detect SIMD support", throwable);
        }

        if (SIMDDetection.isEnabled) {
            logger().info("[Pufferfish] SIMD operations detected as functional. Map rendering SIMD path is enabled.");
        } else if (SIMDDetection.versionLimited) {
            logger().warning("[Pufferfish] SIMD disabled. These optimizations are supported on Java 17-25.");
        } else {
            logger().warning("[Pufferfish] SIMD optimizations are available but not enabled.");
            logger().warning("[Pufferfish] Add \"--add-modules=jdk.incubator.vector\" to startup flags before \"-jar\".");
            logger().warning("[Pufferfish] If already added, SIMD may not be supported by your current JVM/CPU.");
        }
    }

    private static java.util.logging.Logger logger() {
        final org.bukkit.Server server = Bukkit.getServer();
        return server != null ? server.getLogger() : java.util.logging.Logger.getLogger("Pufferfish");
    }

    private static void copyDefaultConfigIfMissing() {
        if (CONFIG_FILE.exists()) {
            return;
        }

        final File parent = CONFIG_FILE.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            logger().warning("[Pufferfish] Could not create config directory " + parent.getAbsolutePath());
        }

        try (InputStream input = PufferfishConfig.class.getClassLoader().getResourceAsStream("configurations/pufferfish.yml")) {
            if (input == null) {
                logger().warning("[Pufferfish] Could not find default pufferfish.yml template resource");
                return;
            }
            Files.copy(input, CONFIG_FILE.toPath());
        } catch (IOException ex) {
            logger().log(Level.WARNING, "[Pufferfish] Failed to create default pufferfish.yml", ex);
        }
    }

    private static void saveConfig() {
        try {
            config.save(CONFIG_FILE);
        } catch (IOException ex) {
            logger().log(Level.WARNING, "Could not save pufferfish.yml", ex);
        }
    }

    private static void set(final String path, final Object value) {
        if (!config.isSet(path)) {
            config.set(path, value);
        }
    }

    private static boolean getBoolean(final String path, final boolean fallback) {
        set(path, fallback);
        return config.getBoolean(path, fallback);
    }

    private static int getInt(final String path, final int fallback) {
        set(path, fallback);
        return config.getInt(path, fallback);
    }
}
