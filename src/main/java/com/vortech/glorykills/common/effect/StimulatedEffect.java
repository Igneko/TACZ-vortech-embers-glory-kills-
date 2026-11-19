package com.vortech.glorykills.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Adrenaline Rush: Velocidad I + Fuerza I + 5 puntos de absorcion mientras dura.
 */
public class StimulatedEffect extends MobEffect {

    /** 5 puntos = 2.5 corazones por nivel del efecto. */
    private static final float ABSORPTION_PER_LEVEL = 5.0F;

    public StimulatedEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "91AEAA56-376B-4498-935B-2F7F68070635", 0.20D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 3.0D, AttributeModifier.Operation.ADDITION);
    }

    // Igual que el efecto vanilla de Absorcion: el escudo aparece al aplicar el
    // efecto y se retira (lo que quede de el) cuando termina.
    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() + ABSORPTION_PER_LEVEL * (amplifier + 1));
        super.addAttributeModifiers(entity, attributes, amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() - ABSORPTION_PER_LEVEL * (amplifier + 1));
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }
}
