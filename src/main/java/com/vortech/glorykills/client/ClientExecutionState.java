package com.vortech.glorykills.client;

import com.vortech.glorykills.common.network.ExecutionStartPacket;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

/**
 * Estado de la ejecucion del jugador local: camara en tercera persona,
 * sacudida en el golpe y bloqueo de movimiento.
 *
 * Aqui es donde, mas adelante, se dispararan las animaciones de PlayerAnimator.
 */
public final class ClientExecutionState {

    /** Cuantos ticks dura la sacudida de camara tras el golpe. */
    private static final float SHAKE_TICKS = 8.0F;

    private static boolean active;
    private static int age;
    private static int durationTicks;
    private static int impactTick;
    private static CameraType previousCamera;

    public static void start(ExecutionStartPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || packet.executorId() != minecraft.player.getId()) {
            // Ejecucion de otro jugador: aqui ira su animacion vista desde fuera.
            return;
        }
        if (!active) {
            previousCamera = minecraft.options.getCameraType();
        }
        minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        active = true;
        age = 0;
        durationTicks = packet.durationTicks();
        impactTick = packet.impactTick();
    }

    public static void tick() {
        if (!active) {
            return;
        }
        age++;
        if (age >= durationTicks) {
            stop();
        }
    }

    public static void stop() {
        if (!active) {
            return;
        }
        active = false;
        if (previousCamera != null) {
            Minecraft.getInstance().options.setCameraType(previousCamera);
            previousCamera = null;
        }
    }

    public static boolean isActive() {
        return active;
    }

    /** 1.0 justo en el golpe, baja a 0 en SHAKE_TICKS. */
    public static float shakeStrength(float partialTick) {
        if (!active) {
            return 0.0F;
        }
        float sinceImpact = age + partialTick - impactTick;
        if (sinceImpact < 0.0F || sinceImpact > SHAKE_TICKS) {
            return 0.0F;
        }
        return 1.0F - sinceImpact / SHAKE_TICKS;
    }

    private ClientExecutionState() {
    }
}
