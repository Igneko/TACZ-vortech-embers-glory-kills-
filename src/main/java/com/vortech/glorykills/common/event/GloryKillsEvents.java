package com.vortech.glorykills.common.event;

import com.vortech.glorykills.common.registry.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "vortech_embers")
public class GloryKillsEvents {

    @SubscribeEvent
    public static void onEffectExpire(MobEffectEvent.Expired event) {
        if (event.getEffectInstance().getEffect() == ModEffects.STIMULATED.get()) {
            LivingEntity entity = event.getEntity();
            if (!entity.level().isClientSide()) {
                entity.addEffect(new MobEffectInstance(ModEffects.OVERLOADED.get(), 1200, 0, false, false, true));
            }
        }
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ModEffects.OVERLOADED.get())) {
            if (event.getEffectInstance().getEffect().getCategory() == MobEffectCategory.BENEFICIAL) {
                event.setResult(Event.Result.DENY); 
            }
        }
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (event.getEffectInstance().getEffect() == ModEffects.OVERLOADED.get()) {
            LivingEntity entity = event.getEntity();
            if (!entity.level().isClientSide()) {
                List<MobEffect> effectsToRemove = new ArrayList<>();
                
                for (MobEffectInstance instance : entity.getActiveEffects()) {
                    if (instance.getEffect().getCategory() == MobEffectCategory.BENEFICIAL) {
                        effectsToRemove.add(instance.getEffect());
                    }
                }
                
                for (MobEffect effect : effectsToRemove) {
                    entity.removeEffect(effect);
                }
            }
        }
    }
}