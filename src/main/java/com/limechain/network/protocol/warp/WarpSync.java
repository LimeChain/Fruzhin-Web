package com.limechain.network.protocol.warp;

import com.limechain.network.StrictProtocolBinding;
import com.limechain.network.protocol.warp.dto.WarpSyncResponse;
import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
public class WarpSync extends StrictProtocolBinding {

    private final String protocolId;

    public WarpSync(String protocolId) {
        super(protocolId);
        this.protocolId = protocolId;
    }

    public WarpSyncResponse warpSyncRequest(String blockHash) {
        WarpSyncProtocol.Sender sender = new WarpSyncProtocol.Sender();
        WarpSyncResponse resp = sender.warpSyncRequest(blockHash, protocolId);
        log.log(Level.INFO, "Received warp sync response with " + resp.getFragments().length + " fragments");
        return resp;
    }
}
