package com.vortech.glorykills.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ExecutableEffect extends MobEffect {
    public ExecutableEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFFFFF);
    }
}