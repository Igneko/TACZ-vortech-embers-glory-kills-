package com.vortech.glorykills.common.event;

import com.vortech.glorykills.GloryKillsMod;
import com.vortech.glorykills.common.config.GloryKillsConfig;
import com.vortech.glorykills.common.execution.ExecutionManager;
import com.vortech.glorykills.common.registry.ModEffects;
import com.vortech.glorykills.common.util.ServerTaskQueue;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = GloryKillsMod.MOD_ID)
public class GloryKillsEvents {

    /** Marca para que cada enemigo solo tenga UNA ventana de ejecucion en su vida. */
    private static final String WINDOW_USED_TAG = GloryKillsMod.MOD_ID + ".execution_window_used";

    // ================================================================ tick / ciclo de vida

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ExecutionManager.tick();
        ServerTaskQueue.runPending();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ExecutionManager.clear();
        ServerTaskQueue.clear();
    }

    // ================================================================ ventana de ejecucion

    /** Cuando un enemigo queda con poca vida (sin morir) entra en estado Executable + Glowing. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !canBeExecuted(entity)) {
            return;
        }
        float healthAfterHit = entity.getHealth() - event.getAmount();
        if (healthAfterHit <= 0.0F || healthAfterHit > GloryKillsConfig.EXECUTION_HEALTH_THRESHOLD.get()) {
            return;
        }

        entity.getPersistentData().putBoolean(WINDOW_USED_TAG, true);
        int window = GloryKillsConfig.EXECUTION_WINDOW_TICKS.get();
        // Aplazado: el dano puede venir de veneno/wither, que se aplica mientras
        // el juego recorre los efectos de la entidad.
        ServerTaskQueue.enqueue(() -> {
            if (entity.isAlive() && !entity.isRemoved()) {
                entity.addEffect(new MobEffectInstance(ModEffects.EXECUTABLE.get(), window, 0, false, false, true));
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, window, 0, false, false, false));
            }
        });
    }

    private static boolean canBeExecuted(LivingEntity entity) {
        return !(entity instanceof Player)
                && !(entity instanceof ArmorStand)
                && !entity.getType().is(Tags.EntityTypes.BOSSES)
                && !entity.hasEffect(ModEffects.EXECUTABLE.get())
                && !ExecutionManager.isBeingExecuted(entity)
                && !entity.getPersistentData().getBoolean(WINDOW_USED_TAG);
    }

    /** Nadie puede danar al verdugo ni a la victima mientras dura la ejecucion. */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        if (ExecutionManager.isProtected(entity)) {
            event.setCanceled(true);
        }
    }

    // ================================================================ expiracion de efectos

    @SubscribeEvent
    public static void onEffectExpire(MobEffectEvent.Expired event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || event.getEffectInstance() == null) {
            return;
        }
        MobEffect effect = event.getEffectInstance().getEffect();

        // IMPORTANTE: aqui el juego esta recorriendo la lista de efectos. Anadir o
        // quitar efectos directamente crashea (ConcurrentModificationException),
        // asi que todo se aplaza al final del tick.
        if (effect == ModEffects.STIMULATED.get()) {
            ServerTaskQueue.enqueue(() -> applyAdrenalineCrash(entity));
        } else if (effect == ModEffects.EXECUTABLE.get()) {
            ServerTaskQueue.enqueue(() -> rollBleedOut(entity));
        }
    }

    /** Quitar Stimulated antes de tiempo (leche, /effect clear) tambien provoca el bajon. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide() && event.getEffect() == ModEffects.STIMULATED.get()) {
            ServerTaskQueue.enqueue(() -> applyAdrenalineCrash(entity));
        }
    }

    private static void applyAdrenalineCrash(LivingEntity entity) {
        if (entity.isAlive() && !entity.isRemoved()) {
            entity.addEffect(new MobEffectInstance(ModEffects.OVERLOADED.get(),
                    GloryKillsConfig.OVERLOADED_DURATION_TICKS.get(), 0, false, false, true));
        }
    }

    /** El Juicio del Azar: si nadie lo ejecuto, el enemigo muere o sobrevive. */
    private static void rollBleedOut(LivingEntity entity) {
        if (!entity.isAlive() || entity.isRemoved() || ExecutionManager.isBeingExecuted(entity)) {
            return;
        }
        if (entity.getRandom().nextDouble() >= GloryKillsConfig.BLEED_OUT_DEATH_CHANCE.get()) {
            return; // Sobrevive arrastrandose.
        }
        // Si lo hirio un jugador, la muerte se le acredita (loot y experiencia).
        DamageSource source = entity.getLastHurtByMob() instanceof Player player
                ? entity.damageSources().playerAttack(player)
                : entity.damageSources().generic();
        entity.invulnerableTime = 0;
        entity.hurt(source, Float.MAX_VALUE);
    }

    // ================================================================ Overloaded

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ModEffects.OVERLOADED.get())
                && event.getEffectInstance().getEffect().getCategory() == MobEffectCategory.BENEFICIAL) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || event.getEffectInstance().getEffect() != ModEffects.OVERLOADED.get()) {
            return;
        }
        ServerTaskQueue.enqueue(() -> {
            List<MobEffect> effectsToRemove = new ArrayList<>();
            for (MobEffectInstance instance : entity.getActiveEffects()) {
                if (instance.getEffect().getCategory() == MobEffectCategory.BENEFICIAL) {
                    effectsToRemove.add(instance.getEffect());
                }
            }
            for (MobEffect effect : effectsToRemove) {
                entity.removeEffect(effect);
            }
        });
    }
}
