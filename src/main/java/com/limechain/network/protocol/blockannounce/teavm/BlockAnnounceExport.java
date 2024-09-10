package com.limechain.network.protocol.blockannounce.teavm;

import org.teavm.jso.JSObject;

public interface BlockAnnounceExport extends JSObject {
    String getHandshake();

    void blockAnnounce(String announce, String peerId);

}