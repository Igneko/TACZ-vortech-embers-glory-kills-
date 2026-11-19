package com.vortech.glorykills.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.vortech.glorykills.GloryKillsMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = GloryKillsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModKeyMappings {

    public static final String CATEGORY = "key.categories." + GloryKillsMod.MOD_ID;

    /** Tecla de Glory Kill. Por defecto B; se puede cambiar en Opciones > Controles. */
    public static final KeyMapping EXECUTE = new KeyMapping(
            "key." + GloryKillsMod.MOD_ID + ".execute",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY);

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(EXECUTE);
    }

    private ModKeyMappings() {
    }
}
