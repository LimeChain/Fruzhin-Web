package com.limechain.sync.warpsync.action;

import com.limechain.config.AppBean;
import com.limechain.exception.global.MissingObjectException;
import com.limechain.network.protocol.warp.dto.WarpSyncResponse;
import com.limechain.polkaj.Hash256;
import com.limechain.sync.warpsync.WarpSyncMachine;
import com.limechain.sync.warpsync.WarpSyncState;
import lombok.extern.java.Log;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.logging.Level;

@Log
public class RequestFragmentsAction implements WarpSyncAction {

    private final WarpSyncState warpSyncState;
    private final Hash256 blockHash;
    private WarpSyncResponse result;
    private Exception error;

    public RequestFragmentsAction(Hash256 blockHash) {
        this.blockHash = blockHash;
        this.warpSyncState = AppBean.getBean(WarpSyncState.class);
    }

    @Override
    public void next(WarpSyncMachine sync) {
        if (this.error != null) {
            //Retry with a different source
            try {
                // Wait a bit before retrying. The peer might've just connected and still not in address book
                Thread.sleep(1000);
                sync.setWarpSyncAction(new RequestFragmentsAction(blockHash));
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.log(Level.SEVERE, "Retry warp sync request fragment exception: "
                        + e.getMessage(), e.getStackTrace());
                sync.setWarpSyncAction(new RpcFallbackAction());
            }
        }
        if (this.result != null) {
            sync.setWarpSyncAction(new VerifyJustificationAction());
            return;
        }
        log.log(Level.WARNING, "RequestFragmentsState.next() called without result or error set.");
    }

    @Override
    public void handle(WarpSyncMachine sync) {
        try {
            WarpSyncResponse resp = sync.getNetworkService().makeWarpSyncRequest(blockHash.toString());
            if (resp == null) {
                throw new MissingObjectException("No response received.");
            }

            log.log(Level.INFO, "Successfully received response from peer");
            if (resp.getFragments().length == 0) {
                log.log(Level.WARNING, "The response contained no fragments - the peer is up to date.");
                return;
            }
            warpSyncState.setWarpSyncFragmentsFinished(resp.isFinished());
            sync.setFragmentsQueue(new ArrayDeque<>(
                    Arrays.stream(resp.getFragments()).toList())
            );

            this.result = resp;
        } catch (Exception e) {
            // TODO: Set error state, next() will use to transition to correct next state.
            // This error state could be either recoverable or irrecoverable.
            log.log(Level.WARNING, "Error while requesting fragments: " + e.getMessage());
            this.error = e;
        }
    }
}
