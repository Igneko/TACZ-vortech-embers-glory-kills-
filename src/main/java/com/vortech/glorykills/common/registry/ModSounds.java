package com.vortech.glorykills.common.registry;

import com.vortech.glorykills.GloryKillsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, GloryKillsMod.MOD_ID);

    /** Inicio de la ejecucion (el jugador agarra al enemigo). */
    public static final RegistryObject<SoundEvent> GLORY_KILL_START = register("glory_kill.start");

    /** Golpe letal. */
    public static final RegistryObject<SoundEvent> GLORY_KILL_IMPACT = register("glory_kill.impact");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(GloryKillsMod.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    private ModSounds() {
    }
}
