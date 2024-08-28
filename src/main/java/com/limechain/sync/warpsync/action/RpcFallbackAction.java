package com.limechain.sync.warpsync.action;

import com.limechain.network.protocol.warp.dto.BlockHeader;
import com.limechain.network.protocol.warp.dto.HeaderDigest;
import com.limechain.network.protocol.warp.scale.reader.HeaderDigestReader;
import com.limechain.polkaj.Hash256;
import com.limechain.polkaj.reader.ScaleCodecReader;
import com.limechain.rpc.BlockRpcClient;
import com.limechain.rpc.dto.ChainGetHeaderResult;
import com.limechain.rpc.dto.GrandpaRoundStateResult;
import com.limechain.rpc.server.AppBean;
import com.limechain.storage.block.SyncState;
import com.limechain.sync.warpsync.WarpSyncMachine;
import com.limechain.utils.StringUtils;
import lombok.extern.java.Log;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;

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
            sync.setWarpSyncAction(new RequestFragmentsAction(syncState.getLastFinalizedBlockHash()));
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
            Hash256 latestFinalizedHashResult = BlockRpcClient.getLastFinalizedBlockHash();
            ChainGetHeaderResult headerResult = BlockRpcClient.getHeader(latestFinalizedHashResult.toString());
            GrandpaRoundStateResult roundStateResult = BlockRpcClient.getGrandpaRoundState();

            BlockHeader latestFinalizedHeader = new BlockHeader();
            latestFinalizedHeader.setBlockNumber(new BigInteger(
                StringUtils.remove0xPrefix(headerResult.getNumber()), 16));
            latestFinalizedHeader.setParentHash(Hash256.from(headerResult.getParentHash()));
            latestFinalizedHeader.setStateRoot(Hash256.from(headerResult.getStateRoot()));
            latestFinalizedHeader.setExtrinsicsRoot(Hash256.from(headerResult.getExtrinsicsRoot()));

            List<String> digestHexes = headerResult.getDigest().getLogs();
            HeaderDigest[] digests = new HeaderDigest[digestHexes.size()];
            for (int i = 0; i < digestHexes.size(); i++) {
                digests[i] = new HeaderDigestReader().read(
                    new ScaleCodecReader(StringUtils.hexToBytes(digestHexes.get(i))));
            }
            latestFinalizedHeader.setDigest(digests);

            syncState.finalizeHeader(latestFinalizedHeader);
            syncState.setSetId(BigInteger.valueOf(roundStateResult.getSetId()));
            syncState.resetRound();

        } catch (Exception e) {
            log.log(Level.WARNING, "Error while calling rpc endpoints: " + e.getMessage());
            this.error = e;
        }
    }
}
