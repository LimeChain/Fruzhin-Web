package com.limechain.network.protocol.warp;

import com.limechain.network.protocol.warp.dto.WarpSyncRequest;
import com.limechain.network.protocol.warp.dto.WarpSyncResponse;

public interface WarpSyncController {
    WarpSyncResponse send(WarpSyncRequest req, String protocolId);

    default WarpSyncResponse warpSyncRequest(String blockHash, String protocolId) {
        var request = new WarpSyncRequest(blockHash);

        return send(request, protocolId);
    }
}
