package com.vortech.glorykills.common.network;

import com.vortech.glorykills.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Servidor -> clientes cercanos: "empieza la ejecucion entre estas dos entidades".
 * Los clientes la usan para camara, sacudida y (mas adelante) animaciones.
 */
public record ExecutionStartPacket(int executorId, int targetId, int durationTicks, int impactTick) {

    public static void encode(ExecutionStartPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.executorId);
        buf.writeVarInt(packet.targetId);
        buf.writeVarInt(packet.durationTicks);
        buf.writeVarInt(packet.impactTick);
    }

    public static ExecutionStartPacket decode(FriendlyByteBuf buf) {
        return new ExecutionStartPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(ExecutionStartPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleExecutionStart(packet));
    }
}
