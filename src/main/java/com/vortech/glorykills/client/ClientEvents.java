package com.vortech.glorykills.client;

import com.vortech.glorykills.GloryKillsMod;
import com.vortech.glorykills.common.network.ModNetwork;
import com.vortech.glorykills.common.network.RequestExecutionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GloryKillsMod.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {

    /** Grados maximos de sacudida en el golpe. */
    private static final float SHAKE_DEGREES = 3.0F;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        while (ModKeyMappings.EXECUTE.consumeClick()) {
            if (minecraft.player != null && !ClientExecutionState.isActive()) {
                ModNetwork.CHANNEL.sendToServer(new RequestExecutionPacket());
            }
        }
        ClientExecutionState.tick();
    }

    /** Sacudida de camara en el momento del impacto. */
    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float partialTick = (float) event.getPartialTick();
        float strength = ClientExecutionState.shakeStrength(partialTick);
        Minecraft minecraft = Minecraft.getInstance();
        if (strength <= 0.0F || minecraft.player == null) {
            return;
        }
        float time = minecraft.player.tickCount + partialTick;
        float amount = SHAKE_DEGREES * strength;
        event.setPitch(event.getPitch() + Mth.sin(time * 2.7F) * amount);
        event.setYaw(event.getYaw() + Mth.sin(time * 3.1F + 1.3F) * amount * 0.6F);
        event.setRoll(event.getRoll() + Mth.sin(time * 3.7F + 2.1F) * amount);
    }

    /** El jugador no se puede mover ni saltar durante la ejecucion. */
    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!ClientExecutionState.isActive()) {
            return;
        }
        Input input = event.getInput();
        input.forwardImpulse = 0.0F;
        input.leftImpulse = 0.0F;
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientExecutionState.stop();
    }

    private ClientEvents() {
    }
}
