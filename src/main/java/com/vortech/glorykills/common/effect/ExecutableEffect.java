package com.vortech.glorykills.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Enemigo aturdido y listo para ser ejecutado. Se mueve a la mitad de velocidad
 * mientras dura la ventana de ejecucion.
 */
public class ExecutableEffect extends MobEffect {
    public ExecutableEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFFFFF);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "3C1B7E5A-0D5E-4F0B-9A61-6C0E2F6B8D11", -0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
