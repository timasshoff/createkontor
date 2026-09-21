package com.timder.kontor.game.network;

import com.timder.kontor.client.network.ClientPayloadHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class KontorNetwork {

    private static final String PROTOCOL_VERSION = "1";

    private KontorNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToClient(
                ChartPayload.TYPE,
                ChartPayload.STREAM_CODEC,
                ClientPayloadHandler::handleChart);
    }
}
