package com.limechain.network.protocol.grandpa;

import com.limechain.network.protocol.grandpa.teavm.GrandpaHandler;
import lombok.extern.java.Log;

/**
 * Service for sending messages on Grandpa protocol.
 */
@Log
public class GrandpaService {
    private final String protocolId;

    public GrandpaService(String protocolId) {
        this.protocolId = protocolId;
    }

    private boolean isRegistered = false;

    public void sendHandshake() {
        try {
            if (!isRegistered) {
                GrandpaEngine.registerHandler(new GrandpaHandler(), protocolId);
                isRegistered = true;
            }
        } catch (IllegalStateException e) {
            log.warning("Error registering grandpa handler");
        }
        try {
            GrandpaEngine.sendHandshakeToAll(GrandpaEngine.getHandshake(), protocolId);
        } catch (IllegalStateException e) {
            log.warning("Error sending grandpa handshake request");
        }
    }
}
