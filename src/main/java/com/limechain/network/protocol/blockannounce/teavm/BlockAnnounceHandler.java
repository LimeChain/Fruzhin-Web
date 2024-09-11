package com.limechain.network.protocol.blockannounce.teavm;

import com.limechain.network.protocol.blockannounce.BlockAnnounceEngine;
import com.limechain.utils.Stopwatch;
import com.limechain.utils.StringUtils;


public class BlockAnnounceHandler implements BlockAnnounceExport {

    private final Stopwatch stopwatch;

    public BlockAnnounceHandler(Stopwatch stopwatch) {
        this.stopwatch = stopwatch;
    }

    public void blockAnnounce(String announce, String peerId) {
        BlockAnnounceEngine.handleBlockAnnounce(StringUtils.fromHex(announce), peerId);
        stopwatch.reset();
    }

    public String getHandshake() {
        return BlockAnnounceEngine.getHandshake();
    }
}