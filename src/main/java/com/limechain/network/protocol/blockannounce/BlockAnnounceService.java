package com.limechain.network.protocol.blockannounce;

import com.limechain.network.protocol.blockannounce.teavm.BlockAnnounceHandler;
import lombok.extern.java.Log;

@Log
public class BlockAnnounceService {

    private final String protocolId;
    private boolean isRegistered = false;

    public BlockAnnounceService(String protocolId) {
        this.protocolId = protocolId;
    }

    public void sendHandshake() {
        try {
            if (!isRegistered) {
                BlockAnnounceEngine.registerHandler(new BlockAnnounceHandler(), protocolId);
                isRegistered = true;
            }
        } catch (IllegalStateException e) {
            log.warning("Error registering block announce handler");
        }
        try {
            BlockAnnounceEngine.sendHandshakeToAll(BlockAnnounceEngine.getHandshake(), protocolId);
        } catch (IllegalStateException e) {
            log.warning("Error sending block announce handshake request");
        }
    }
}
