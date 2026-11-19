package com.vortech.glorykills;

import com.vortech.glorykills.common.config.GloryKillsConfig;
import com.vortech.glorykills.common.network.ModNetwork;
import com.vortech.glorykills.common.registry.ModEffects;
import com.vortech.glorykills.common.registry.ModSounds;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GloryKillsMod.MOD_ID)
public class GloryKillsMod {

    public static final String MOD_ID = "vortech_embers";

    public GloryKillsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEffects.register(modEventBus);
        ModSounds.register(modEventBus);

        // Config editable en config/vortech_embers-common.toml
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GloryKillsConfig.SPEC);

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }
}
