package org.ulyxspigot.ulyxspigot.network;

import org.bukkit.Bukkit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;

public final class UlyxPacketFilters {

    private UlyxPacketFilters() {
    }

    public static boolean shouldBlockFootstepSound(final Packet<?> packet) {
        if (!(packet instanceof ClientboundSoundPacket) && !"ClientboundSoundEntityPacket".equals(packet.getClass().getSimpleName())) {
            return false;
        }

        final Identifier key = resolveSoundKey(packet);
        return key != null && key.getPath().contains("step");
    }

    public static boolean shouldBlockSpawnerParticles(final ServerLevel level, final Packet<?> packet) {
        if (!(packet instanceof ClientboundLevelParticlesPacket particlesPacket)) {
            return false;
        }

        final Identifier particleKey = resolveParticleKey(particlesPacket);
        if (particleKey == null) {
            return false;
        }

        final String particleName = particleKey.getPath();
        if (!("smoke".equals(particleName) || "flame".equals(particleName))) {
            return false;
        }

        final Double x = readDouble(particlesPacket, "x", "getX");
        final Double y = readDouble(particlesPacket, "y", "getY");
        final Double z = readDouble(particlesPacket, "z", "getZ");
        if (x == null || y == null || z == null) {
            return false;
        }

        final BlockPos pos = BlockPos.containing(x, y, z);
        return isSpawnerAt(level, pos)
            || isSpawnerAt(level, pos.below())
            || isSpawnerAt(level, pos.above())
            || isSpawnerAt(level, pos.north())
            || isSpawnerAt(level, pos.south())
            || isSpawnerAt(level, pos.east())
            || isSpawnerAt(level, pos.west());
    }

    public static boolean shouldBlockFirePackets(final ServerLevel level, final Packet<?> packet, final boolean ignoreInvisible) {
        if (!"ClientboundSetEntityDataPacket".equals(packet.getClass().getSimpleName())) {
            return false;
        }

        if (Bukkit.isPrimaryThread()) {
            return shouldBlockFirePacketsSync(level, packet, ignoreInvisible);
        }

        final java.util.concurrent.CompletableFuture<Boolean> result = new java.util.concurrent.CompletableFuture<>();
        level.getServer().scheduleOnMain(() -> {
            try {
                result.complete(shouldBlockFirePacketsSync(level, packet, ignoreInvisible));
            } catch (Throwable throwable) {
                result.complete(false);
            }
        });

        try {
            return result.get(25L, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (java.lang.InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException exception) {
            return false;
        }
    }

    private static boolean shouldBlockFirePacketsSync(final ServerLevel level, final Packet<?> packet, final boolean ignoreInvisible) {
        final Integer entityId = readInt(packet, "id", "getId", "entityId", "getEntityId");
        if (entityId == null) {
            return false;
        }

        final Entity entity = level.getEntity(entityId);
        if (entity == null) {
            return false;
        }

        if (ignoreInvisible && entity.isInvisible()) {
            return false;
        }

        if (!(entity instanceof LivingEntity living)
            || !living.hasEffect(MobEffects.FIRE_RESISTANCE)
            || entity.getRemainingFireTicks() <= 0) {
            return false;
        }

        final Object packedItems = invokeNoArg(packet, "packedItems", "trackedValues", "getUnpackedData");
        if (!(packedItems instanceof Iterable<?> iterable)) {
            return false;
        }

        boolean hasAny = false;
        for (Object item : iterable) {
            final Integer dataId = readInt(item, "id", "getId", "accessorId", "getAccessorId");
            if (dataId == null || dataId != 0) {
                return false;
            }
            hasAny = true;
        }

        return hasAny;
    }

    private static boolean isSpawnerAt(final ServerLevel level, final BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.SPAWNER);
    }

    private static Identifier resolveSoundKey(final Object packet) {
        final Object holderObj = invokeNoArg(packet, "sound", "getSound");
        if (!(holderObj instanceof Holder<?> holder)) {
            return null;
        }

        final Object value = holder.value();
        if (!(value instanceof SoundEvent soundEvent)) {
            return null;
        }

        return BuiltInRegistries.SOUND_EVENT.getKey(soundEvent);
    }

    private static Identifier resolveParticleKey(final Object packet) {
        final Object particleObj = invokeNoArg(packet, "particle", "getParticle");
        if (!(particleObj instanceof ParticleOptions particleOptions)) {
            return null;
        }

        return BuiltInRegistries.PARTICLE_TYPE.getKey(particleOptions.getType());
    }

    private static Integer readInt(final Object target, final String... methodNames) {
        final Object value = invokeNoArg(target, methodNames);
        if (value instanceof Number number) {
            return number.intValue();
        }

        return null;
    }

    private static Double readDouble(final Object target, final String... methodNames) {
        final Object value = invokeNoArg(target, methodNames);
        if (value instanceof Number number) {
            return number.doubleValue();
        }

        return null;
    }

    private static Object invokeNoArg(final Object target, final String... methodNames) {
        for (String methodName : methodNames) {
            try {
                return target.getClass().getMethod(methodName).invoke(target);
            } catch (ReflectiveOperationException ignored) {
            }
        }

        return null;
    }
}
