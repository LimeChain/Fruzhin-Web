package com.limechain.sync.warpsync.action;

import com.limechain.config.AppBean;
import com.limechain.polkaj.Hash256;
import com.limechain.rpc.ChainRpcClient;
import com.limechain.rpc.GrandpaRpcClient;
import com.limechain.rpc.dto.ChainGetHeaderResult;
import com.limechain.rpc.dto.GrandpaRoundStateResult;
import com.limechain.storage.block.SyncState;
import com.limechain.sync.warpsync.WarpSyncMachine;
import com.limechain.utils.RpcUtils;
import lombok.extern.java.Log;

import java.math.BigInteger;
import java.util.logging.Level;

/**
 * A fallback state of the {@link WarpSyncMachine}. If the machine fails to start or fails during execution without
 * the possibility to retry it reaches this action. The {@link SyncState} of the node gets populated with the latest
 * finalized block data via the use of RPC calls to other active nodes.
 */
@Log
public class RpcFallbackAction implements WarpSyncAction {
    private final SyncState syncState;
    private Exception error;

    public RpcFallbackAction() {
        this.syncState = AppBean.getBean(SyncState.class);
    }

    @Override
    public void next(WarpSyncMachine sync) {
        if (this.error != null) {
            sync.setWarpSyncAction(new FinishedAction());
            return;
        }

        log.log(Level.INFO, "Populated sync state from RPC results. Block hash is now at #"
                + syncState.getLastFinalizedBlockNumber() + ": "
                + syncState.getLastFinalizedBlockHash().toString()
                + " with state root " + syncState.getStateRoot());

        sync.setWarpSyncAction(new FinishedAction());
    }

    @Override
    public void handle(WarpSyncMachine sync) {
        try {
            Hash256 latestFinalizedHashResult = ChainRpcClient.getLastFinalizedBlockHash();
            ChainGetHeaderResult headerResult = ChainRpcClient.getHeader(latestFinalizedHashResult.toString());
            GrandpaRoundStateResult roundStateResult = GrandpaRpcClient.getGrandpaRoundState();

            syncState.finalizeHeader(RpcUtils.toBlockHeader(headerResult));
            syncState.setSetId(BigInteger.valueOf(roundStateResult.getSetId()));
            syncState.resetRound();

            sync.setProtocolSync(false);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error while calling rpc endpoints: " + e.getMessage());
            this.error = e;
        }
    }
}
