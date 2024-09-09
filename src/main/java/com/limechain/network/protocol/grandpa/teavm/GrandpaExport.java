package com.limechain.network.protocol.grandpa.teavm;

import org.teavm.jso.JSObject;

public interface GrandpaExport extends JSObject {
    String getHandshake();

    String getNeighbourMessage();

    void handleMessage(String announce, String peerId);

}