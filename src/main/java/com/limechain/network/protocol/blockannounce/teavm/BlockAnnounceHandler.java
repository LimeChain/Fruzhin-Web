package com.limechain.network.protocol.blockannounce.teavm;

import com.limechain.network.protocol.blockannounce.BlockAnnounceEngine;
import com.limechain.utils.StringUtils;


public class BlockAnnounceHandler implements BlockAnnounceExport {
    public void blockAnnounce(String announce, String peerId) {
        BlockAnnounceEngine.handleBlockAnnounce(StringUtils.fromHex(announce), peerId);
    }

    public String getHandshake() {
        return BlockAnnounceEngine.getHandshake();
    }

}
