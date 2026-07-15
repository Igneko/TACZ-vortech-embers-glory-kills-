package com.vortech.glorykills;

import com.vortech.glorykills.common.registry.ModEffects;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("vortech_embers")
public class GloryKillsMod {
    
    public GloryKillsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // ¡Encendemos el registro de efectos!
        ModEffects.register(modEventBus);
    }
}