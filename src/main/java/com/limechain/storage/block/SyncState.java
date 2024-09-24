package com.limechain.storage.block;

import com.limechain.chain.lightsyncstate.Authority;
import com.limechain.chain.lightsyncstate.LightSyncState;
import com.limechain.config.ChainService;
import com.limechain.exception.storage.HeaderNotFoundException;
import com.limechain.network.protocol.grandpa.messages.commit.CommitMessage;
import com.limechain.network.protocol.warp.dto.BlockHeader;
import com.limechain.polkaj.Hash256;
import com.limechain.storage.LocalStorage;
import com.limechain.storage.StorageConstants;
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

    public SyncState(ChainService chainService) {
        this.genesisBlockHash = chainService.getGenesisBlockHash();

        clearStoredStateIfNeeded(chainService.getChain().getId());
        loadState();
        this.startingBlock = this.lastFinalizedBlockNumber;
    }

    private void loadState() {
        this.lastFinalizedBlockNumber = LocalStorage.find(
                StorageConstants.LAST_FINALIZED_BLOCK_NUMBER, BigInteger.class).orElse(BigInteger.ZERO);
        this.lastFinalizedBlockHash = new Hash256(LocalStorage.find(
                StorageConstants.LAST_FINALIZED_BLOCK_HASH, byte[].class).orElse(genesisBlockHash.getBytes()));
        byte[] stateRootBytes = LocalStorage.find(StorageConstants.STATE_ROOT, byte[].class).orElse(null);
        this.stateRoot = stateRootBytes != null ? new Hash256(stateRootBytes) : null;
        this.authoritySet = LocalStorage.find(StorageConstants.AUTHORITY_SET, Authority[].class).orElse(new Authority[0]);
        this.latestRound = LocalStorage.find(StorageConstants.LATEST_ROUND, BigInteger.class).orElse(BigInteger.ONE);
        this.setId = LocalStorage.find(StorageConstants.SET_ID, BigInteger.class).orElse(BigInteger.ZERO);
    }

    public void persistState(boolean isProtocolSync, String chainId) {
        LocalStorage.save(StorageConstants.LAST_FINALIZED_BLOCK_NUMBER, lastFinalizedBlockNumber);
        LocalStorage.save(StorageConstants.LAST_FINALIZED_BLOCK_HASH, lastFinalizedBlockHash.getBytes());
        LocalStorage.save(StorageConstants.AUTHORITY_SET, authoritySet);
        LocalStorage.save(StorageConstants.LATEST_ROUND, latestRound);
        LocalStorage.save(StorageConstants.SET_ID, setId);
        LocalStorage.save(StorageConstants.STATE_ROOT, stateRoot.getBytes());
        LocalStorage.save(StorageConstants.IS_PROTOCOL_SYNC, isProtocolSync);
        LocalStorage.save(StorageConstants.LAST_SYNCED_CHAIN, chainId);
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

    private void clearStoredStateIfNeeded(String currentChainId) {
        boolean isProtocolSync = LocalStorage.find(StorageConstants.IS_PROTOCOL_SYNC, boolean.class)
                .orElse(false);
        String lastChainId = LocalStorage.find(StorageConstants.LAST_SYNCED_CHAIN, String.class)
                .orElse(null);
        if (!isProtocolSync || !currentChainId.equals(lastChainId)) {
            LocalStorage.clear();
        }
    }
}
