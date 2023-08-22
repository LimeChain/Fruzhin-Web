package com.limechain.sync.warpsync.state;

import com.limechain.network.protocol.lightclient.pb.LightClientMessage;
import com.limechain.sync.warpsync.SyncedState;
import com.limechain.sync.warpsync.WarpSyncMachine;
import com.limechain.trie.Trie;
import com.limechain.trie.TrieVerifier;
import com.limechain.trie.decoder.TrieDecoderException;
import com.limechain.utils.LittleEndianUtils;
import com.limechain.utils.StringUtils;
import io.emeraldpay.polkaj.scale.ScaleCodecReader;
import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
public class RuntimeDownloadState implements WarpSyncState {
    private final SyncedState syncedState = SyncedState.getInstance();
    private Exception error;
    private static byte[] codeKey =
            LittleEndianUtils.convertBytes(StringUtils.hexToBytes(StringUtils.toHex(":code")));

    @Override
    public void next(WarpSyncMachine sync) {
        if (this.error != null) {
            sync.setWarpSyncState(new RequestFragmentsState(syncedState.getLastFinalizedBlockHash()));
            return;
        }
        // After runtime is downloaded, we have to build the runtime and then build chain information
        sync.setWarpSyncState(new RuntimeBuildState());
    }

    @Override
    public void handle(WarpSyncMachine sync) {
        log.log(Level.INFO, "Downloading runtime...");
        LightClientMessage.Response response = sync.getNetworkService().makeRemoteReadRequest(
                syncedState.getLastFinalizedBlockHash().toString(),
                new String[]{StringUtils.toHex(":code")});

        byte[] proof = response.getRemoteReadResponse().getProof().toByteArray();

        byte[][] decodedProofs = decodeProof(proof);

        setCodeAndHeapPages(sync, decodedProofs);
    }

    private byte[][] decodeProof(byte[] proof) {
        ScaleCodecReader reader = new ScaleCodecReader(proof);
        int size = reader.readCompactInt();
        byte[][] decodedProofs = new byte[size][];

        for (int i = 0; i < size; ++i) {
            decodedProofs[i] = reader.readByteArray();
        }
        return decodedProofs;
    }

    private void setCodeAndHeapPages(WarpSyncMachine sync, byte[][] decodedProofs) {
        Trie trie;
        try {
            trie = TrieVerifier.buildTrie(decodedProofs, syncedState.getStateRoot().getBytes());
            var code = trie.get(codeKey);
            if (code == null) {
                this.error = new RuntimeException("Couldn't retrieve runtime code from trie");
            }
            //TODO Heap pages should be fetched from out storage
            if (code == null) return;
            syncedState.setRuntimeCode(code);
            log.log(Level.INFO, "Runtime and heap pages downloaded");

        } catch (TrieDecoderException e) {
            this.error = new RuntimeException("Couldn't build trie from proofs list: " + e.getMessage());
        }
    }
}