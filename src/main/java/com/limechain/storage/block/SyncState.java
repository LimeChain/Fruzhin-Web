package com.limechain.storage.block;

import com.limechain.chain.lightsyncstate.Authority;
import com.limechain.chain.lightsyncstate.LightSyncState;
import com.limechain.constants.GenesisBlockHash;
import com.limechain.exception.storage.HeaderNotFoundException;
import com.limechain.network.protocol.grandpa.messages.commit.CommitMessage;
import com.limechain.network.protocol.warp.dto.BlockHeader;
import com.limechain.polkaj.Hash256;
import com.limechain.storage.DBConstants;
import com.limechain.storage.LocalStorage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.math.BigInteger;

@Getter
@Log
public class SyncState {

    private BigInteger lastFinalizedBlockNumber;
    private final BigInteger startingBlock;
    private final Hash256 genesisBlockHash;
    private Hash256 lastFinalizedBlockHash;
    private Hash256 stateRoot;
    @Setter
    private Authority[] authoritySet;
    private BigInteger latestRound;
    @Setter
    private BigInteger setId;

    public SyncState() {
        this.genesisBlockHash = GenesisBlockHash.POLKADOT;

        clearStoredStateIfNeeded();
        loadState();
        this.startingBlock = this.lastFinalizedBlockNumber;
    }

    private void loadState() {
        this.lastFinalizedBlockNumber = LocalStorage.find(
            DBConstants.LAST_FINALIZED_BLOCK_NUMBER, BigInteger.class).orElse(BigInteger.ZERO);
        this.lastFinalizedBlockHash = new Hash256(LocalStorage.find(
            DBConstants.LAST_FINALIZED_BLOCK_HASH, byte[].class).orElse(genesisBlockHash.getBytes()));
        byte[] stateRootBytes = LocalStorage.find(DBConstants.STATE_ROOT, byte[].class).orElse(null);
        this.stateRoot = stateRootBytes != null ? new Hash256(stateRootBytes) : null;
        this.authoritySet = LocalStorage.find(DBConstants.AUTHORITY_SET, Authority[].class).orElse(new Authority[0]);
        this.latestRound = LocalStorage.find(DBConstants.LATEST_ROUND, BigInteger.class).orElse(BigInteger.ONE);
        this.setId = LocalStorage.find(DBConstants.SET_ID, BigInteger.class).orElse(BigInteger.ZERO);
    }

    public void persistState() {
        LocalStorage.save(DBConstants.LAST_FINALIZED_BLOCK_NUMBER, lastFinalizedBlockNumber);
        LocalStorage.save(DBConstants.LAST_FINALIZED_BLOCK_HASH, lastFinalizedBlockHash.getBytes());
        LocalStorage.save(DBConstants.AUTHORITY_SET, authoritySet);
        LocalStorage.save(DBConstants.LATEST_ROUND, latestRound);
        LocalStorage.save(DBConstants.SET_ID, setId);
        LocalStorage.save(DBConstants.STATE_ROOT, stateRoot.getBytes());
    }

    public void finalizeHeader(BlockHeader header) {
        this.lastFinalizedBlockNumber = header.getBlockNumber();
        this.lastFinalizedBlockHash = header.getHash();
        this.stateRoot = header.getStateRoot();
    }

    public void finalizedCommitMessage(CommitMessage commitMessage) {
        try {
            this.lastFinalizedBlockHash = commitMessage.getVote().getBlockHash();
            this.lastFinalizedBlockNumber = commitMessage.getVote().getBlockNumber();
            this.setId = commitMessage.getSetId();
            this.latestRound = commitMessage.getRoundNumber();
        } catch (HeaderNotFoundException ignored) {
            log.fine("Received commit message for a block that is not in the block store");
        }
    }

    public BigInteger incrementSetId() {
        this.setId = this.setId.add(BigInteger.ONE);
        return setId;
    }

    public void resetRound() {
        this.latestRound = BigInteger.ONE;
    }

    public void setLightSyncState(LightSyncState initState) {
        this.setId = initState.getGrandpaAuthoritySet().getSetId();
        setAuthoritySet(initState.getGrandpaAuthoritySet().getCurrentAuthorities());
        finalizeHeader(initState.getFinalizedBlockHeader());
    }

    public void saveIsProtocolSync(boolean isProtocolSync) {
        LocalStorage.save(DBConstants.IS_PROTOCOL_SYNC, isProtocolSync);
    }

    private void clearStoredStateIfNeeded() {
        boolean isProtocolSync = LocalStorage.find(DBConstants.IS_PROTOCOL_SYNC, boolean.class).orElse(false);
        if (!isProtocolSync) {
            LocalStorage.clear();
        }
    }
}
