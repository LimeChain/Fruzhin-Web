package com.limechain.network.protocol.warp;

import com.limechain.exception.global.ExecutionFailedException;
import com.limechain.exception.global.ThreadInterruptedException;
import com.limechain.network.StrictProtocolBinding;
import com.limechain.network.kad.dto.Host;
import com.limechain.network.kad.dto.PeerId;
import com.limechain.network.protocol.warp.dto.WarpSyncRequest;
import com.limechain.network.protocol.warp.dto.WarpSyncResponse;
import com.limechain.network.wrapper.Stream;
import lombok.extern.java.Log;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

@Log
public class WarpSync extends StrictProtocolBinding {

    private String protocolId;

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
