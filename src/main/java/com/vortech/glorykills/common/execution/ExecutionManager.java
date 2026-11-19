package com.vortech.glorykills.common.execution;

import com.vortech.glorykills.common.config.GloryKillsConfig;
import com.vortech.glorykills.common.network.ExecutionStartPacket;
import com.vortech.glorykills.common.network.ModNetwork;
import com.vortech.glorykills.common.registry.ModEffects;
import com.vortech.glorykills.common.registry.ModSounds;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Controla las Glory Kills en el servidor. El servidor es la unica autoridad:
 * valida, congela a ambos, lleva el reloj de la ejecucion y aplica el golpe
 * letal y las recompensas. Los clientes solo reciben un aviso para la camara
 * y los efectos visuales.
 *
 * Linea de tiempo de una ejecucion (en ticks, configurable):
 *   0            -> inicio: ambos congelados, sonido de agarre, paquete a clientes
 *   impactTick   -> golpe letal: sangre, sonido, muerte, curacion, Stimulated
 *   durationTicks-> fin: el jugador recupera el control
 */
public final class ExecutionManager {

    /** Contador total de Glory Kills del jugador (sobrevive a la muerte). Base para los logros. */
    public static final String KILL_COUNT_KEY = "vortech_embers.glory_kills";

    /** Cono frontal (producto punto) en el que debe estar el objetivo: ~60 grados a cada lado. */
    private static final double MIN_FACING_DOT = 0.5D;

    private static final Map<UUID, ActiveExecution> BY_EXECUTOR = new HashMap<>();
    private static final Set<UUID> TARGETS = new HashSet<>();

    /** Objetivo que esta recibiendo el golpe letal ahora mismo (se le permite recibir dano). */
    private static UUID lethalBlowTarget;

    private ExecutionManager() {
    }

    // ------------------------------------------------------------------ consultas

    public static boolean isExecuting(LivingEntity entity) {
        return BY_EXECUTOR.containsKey(entity.getUUID());
    }

    public static boolean isBeingExecuted(LivingEntity entity) {
        return TARGETS.contains(entity.getUUID());
    }

    /** Durante la ejecucion ni el verdugo ni la victima reciben dano de terceros. */
    public static boolean isProtected(LivingEntity entity) {
        UUID id = entity.getUUID();
        if (BY_EXECUTOR.containsKey(id)) {
            return true;
        }
        return TARGETS.contains(id) && !id.equals(lethalBlowTarget);
    }

    // ------------------------------------------------------------------ inicio

    public static void tryStartExecution(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || isExecuting(player)) {
            return;
        }
        LivingEntity target = findTarget(player);
        if (target != null) {
            start(player, target);
        }
    }

    private static LivingEntity findTarget(ServerPlayer player) {
        double range = GloryKillsConfig.EXECUTION_RANGE.get();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        AABB searchBox = player.getBoundingBox().inflate(range);

        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e != player && e.isAlive() && e.hasEffect(ModEffects.EXECUTABLE.get()) && !isBeingExecuted(e))) {
            double distance = player.distanceTo(candidate);
            if (distance > range) {
                continue;
            }
            Vec3 toTarget = candidate.getBoundingBox().getCenter().subtract(eye).normalize();
            double facing = look.dot(toTarget);
            if (facing < MIN_FACING_DOT || !player.hasLineOfSight(candidate)) {
                continue;
            }
            // Preferimos al mas cercano y al que este mas centrado en la mira.
            double score = distance * (2.0D - facing);
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    private static void start(ServerPlayer player, LivingEntity target) {
        int duration = GloryKillsConfig.EXECUTION_DURATION_TICKS.get();
        int impact = Math.min(GloryKillsConfig.EXECUTION_IMPACT_TICK.get(), duration);

        // Quitar el estado de ventana (removeEffect NO dispara la ruleta 50/50).
        target.removeEffect(ModEffects.EXECUTABLE.get());
        target.removeEffect(MobEffects.GLOWING);

        boolean hadNoAi = false;
        if (target instanceof Mob mob) {
            hadNoAi = mob.isNoAi();
            mob.setNoAi(true);
        }
        target.setDeltaMovement(Vec3.ZERO);
        faceEachOther(player, target);

        ActiveExecution execution = new ActiveExecution(player, target,
                player.getHealth() >= player.getMaxHealth(), hadNoAi, duration, impact);
        BY_EXECUTOR.put(player.getUUID(), execution);
        TARGETS.add(target.getUUID());

        player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                ModSounds.GLORY_KILL_START.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new ExecutionStartPacket(player.getId(), target.getId(), duration, impact));
    }

    /** Gira a la victima para que mire al jugador (la camara del jugador no se toca). */
    private static void faceEachOther(Player player, LivingEntity target) {
        double dx = player.getX() - target.getX();
        double dz = player.getZ() - target.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        target.setYRot(yaw);
        target.setYHeadRot(yaw);
        target.setYBodyRot(yaw);
    }

    // ------------------------------------------------------------------ tick

    /** Se llama una vez por tick del servidor (fase END). */
    public static void tick() {
        Iterator<ActiveExecution> iterator = BY_EXECUTOR.values().iterator();
        while (iterator.hasNext()) {
            ActiveExecution execution = iterator.next();

            if (!execution.isStillValid()) {
                finish(execution);
                iterator.remove();
                continue;
            }

            execution.age++;
            if (execution.target.isAlive()) {
                execution.target.setDeltaMovement(Vec3.ZERO);
            }

            if (!execution.impactDone && execution.age >= execution.impactTick) {
                execution.impactDone = true;
                impact(execution);
            }

            if (execution.age >= execution.durationTicks) {
                finish(execution);
                iterator.remove();
            }
        }
    }

    private static void impact(ActiveExecution execution) {
        ServerPlayer player = execution.executor;
        LivingEntity target = execution.target;
        ServerLevel level = player.serverLevel();

        double x = target.getX();
        double y = target.getY() + target.getBbHeight() * 0.6D;
        double z = target.getZ();
        double spread = target.getBbWidth() * 0.3D;

        // VFX provisional: "sangre" con particulas de bloque de redstone + indicadores de dano.
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()),
                x, y, z, 40, spread, target.getBbHeight() * 0.25D, spread, 0.15D);
        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, x, y, z, 6, spread, 0.2D, spread, 0.1D);
        level.playSound(null, x, y, z, ModSounds.GLORY_KILL_IMPACT.get(), SoundSource.PLAYERS, 1.0F,
                0.9F + level.random.nextFloat() * 0.2F);

        // Golpe letal acreditado al jugador (cuenta para loot y experiencia).
        lethalBlowTarget = target.getUUID();
        try {
            target.invulnerableTime = 0;
            target.hurt(level.damageSources().playerAttack(player), Float.MAX_VALUE);
        } finally {
            lethalBlowTarget = null;
        }

        if (target.isDeadOrDying()) {
            reward(execution);
        }
    }

    private static void reward(ActiveExecution execution) {
        ServerPlayer player = execution.executor;

        player.heal(GloryKillsConfig.HEAL_AMOUNT.get().floatValue());

        // Si ya estaba a tope de vida: Adrenaline Rush. Si esta Overloaded,
        // el propio evento Applicable lo bloquea.
        if (execution.executorWasAtFullHealth) {
            player.addEffect(new MobEffectInstance(ModEffects.STIMULATED.get(),
                    GloryKillsConfig.STIMULATED_DURATION_TICKS.get(), 0, false, true, true));
        }

        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putInt(KILL_COUNT_KEY, persisted.getInt(KILL_COUNT_KEY) + 1);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    private static void finish(ActiveExecution execution) {
        LivingEntity target = execution.target;
        TARGETS.remove(target.getUUID());
        // Si la ejecucion se cancelo (p. ej. el jugador se desconecto) la victima vuelve a moverse.
        if (target.isAlive() && target instanceof Mob mob) {
            mob.setNoAi(execution.targetHadNoAi);
        }
    }

    public static void clear() {
        BY_EXECUTOR.clear();
        TARGETS.clear();
        lethalBlowTarget = null;
    }

    // ------------------------------------------------------------------ estado

    private static final class ActiveExecution {
        final ServerPlayer executor;
        final LivingEntity target;
        final boolean executorWasAtFullHealth;
        final boolean targetHadNoAi;
        final int durationTicks;
        final int impactTick;
        int age;
        boolean impactDone;

        ActiveExecution(ServerPlayer executor, LivingEntity target, boolean executorWasAtFullHealth,
                        boolean targetHadNoAi, int durationTicks, int impactTick) {
            this.executor = executor;
            this.target = target;
            this.executorWasAtFullHealth = executorWasAtFullHealth;
            this.targetHadNoAi = targetHadNoAi;
            this.durationTicks = durationTicks;
            this.impactTick = impactTick;
        }

        boolean isStillValid() {
            if (!executor.isAlive() || executor.hasDisconnected() || executor.level() != target.level()) {
                return false;
            }
            // Antes del golpe la victima tiene que seguir ahi; despues ya puede estar muriendo.
            return impactDone || (target.isAlive() && !target.isRemoved());
        }
    }
}
