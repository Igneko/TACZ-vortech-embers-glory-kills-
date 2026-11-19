package com.vortech.glorykills.client;

import com.vortech.glorykills.common.network.ExecutionStartPacket;

/** Punto de entrada de los paquetes servidor -> cliente. Solo se carga en el cliente. */
public final class ClientPacketHandler {

    public static void handleExecutionStart(ExecutionStartPacket packet) {
        ClientExecutionState.start(packet);
    }

    private ClientPacketHandler() {
    }
}
