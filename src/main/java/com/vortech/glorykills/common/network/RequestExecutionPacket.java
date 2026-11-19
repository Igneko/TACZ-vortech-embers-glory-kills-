package com.vortech.glorykills.common.network;

import com.vortech.glorykills.common.execution.ExecutionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Cliente -> servidor: "el jugador pulso la tecla de ejecucion".
 * No lleva el objetivo: el servidor lo elige y valida por su cuenta,
 * asi un cliente modificado no puede ejecutar enemigos a distancia.
 */
public record RequestExecutionPacket() {

    public static void encode(RequestExecutionPacket packet, FriendlyByteBuf buf) {
    }

    public static RequestExecutionPacket decode(FriendlyByteBuf buf) {
        return new RequestExecutionPacket();
    }

    public static void handle(RequestExecutionPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            ExecutionManager.tryStartExecution(player);
        }
    }
}
