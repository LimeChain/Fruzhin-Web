package com.limechain.network.protocol.grandpa.teavm;

import com.limechain.network.protocol.grandpa.GrandpaEngine;
import com.limechain.utils.StringUtils;

public class GrandpaHandler implements GrandpaExport {
    public void handleMessage(String announce, String peerId) {
        GrandpaEngine.handleResponderStreamMessage(StringUtils.fromHex(announce), peerId);
    }

    public String getHandshake() {
        return GrandpaEngine.getHandshake();
    }

    @Override
    public String getNeighbourMessage() {
        return GrandpaEngine.getNeighbourMessage();
    }

}
