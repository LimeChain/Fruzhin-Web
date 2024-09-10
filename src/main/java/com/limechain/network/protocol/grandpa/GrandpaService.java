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

//    /**
//     * Sends a neighbour message to a peer. If there is no initiator stream opened with the peer,
//     * sends a handshake instead.
//     *
//     * @param us our host object
//     * @param peerId message receiver
//     */
//    public void sendNeighbourMessage(Host us, PeerId peerId) {
//        Optional.ofNullable(connectionManager.getPeerInfo(peerId))
//                .map(p -> p.getGrandpaStreams().getInitiator())
//                .ifPresentOrElse(
//                        this::sendNeighbourMessage,
//                        () -> sendHandshake(us, peerId)
//                );
//    }
//
//    private void sendNeighbourMessage(Stream stream) {
//        GrandpaController controller = new GrandpaController(stream);
//        controller.sendNeighbourMessage();
//    }

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
