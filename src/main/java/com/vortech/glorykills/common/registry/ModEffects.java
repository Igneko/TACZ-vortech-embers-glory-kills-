package com.vortech.glorykills.common.registry;

import com.vortech.glorykills.common.effect.StimulatedEffect;
import com.vortech.glorykills.common.effect.OverloadedEffect;
import com.vortech.glorykills.common.effect.ExecutableEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = 
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "vortech_embers");

    public static final RegistryObject<MobEffect> STIMULATED = 
            MOB_EFFECTS.register("stimulated", StimulatedEffect::new);

    public static final RegistryObject<MobEffect> OVERLOADED = 
            MOB_EFFECTS.register("overloaded", OverloadedEffect::new);

    // El nuevo efecto debe ir aquí, junto a sus hermanos
    public static final RegistryObject<MobEffect> EXECUTABLE = 
            MOB_EFFECTS.register("executable", ExecutableEffect::new);

    // Un único método register al final
    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}