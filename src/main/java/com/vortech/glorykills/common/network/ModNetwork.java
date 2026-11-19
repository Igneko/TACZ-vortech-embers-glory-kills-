package com.vortech.glorykills.common.network;

import com.vortech.glorykills.GloryKillsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {

    // Subir este numero cada vez que cambie el formato de algun paquete.
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(GloryKillsMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        int id = 0;

        CHANNEL.messageBuilder(RequestExecutionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RequestExecutionPacket::encode)
                .decoder(RequestExecutionPacket::decode)
                .consumerMainThread(RequestExecutionPacket::handle)
                .add();

        CHANNEL.messageBuilder(ExecutionStartPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ExecutionStartPacket::encode)
                .decoder(ExecutionStartPacket::decode)
                .consumerMainThread(ExecutionStartPacket::handle)
                .add();
    }

    private ModNetwork() {
    }
}
