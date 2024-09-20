package com.limechain.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// TODO Cleanup

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StorageConstants {
    /**
     * Key for storing the privateKey for nabu
     */
    public static final String PEER_ID = "nodePeerId";
    /**
     * Key under which the genesis chain spec is stored
     */
    public static final String GENESIS_KEY = "genesis";

    //<editor-fold desc="Sync keys">

    public static final String LAST_FINALIZED_BLOCK_NUMBER = "ss::lastFinalizedBlockNumber";
    public static final String LAST_FINALIZED_BLOCK_HASH = "ss::lastFinalizedBlockHash";
    public static final String AUTHORITY_SET = "ss::authoritySet";
    public static final String LATEST_ROUND = "ss::latestRound";
    public static final String SET_ID = "ss::setId";
    public static final String STATE_ROOT = "ss::stateRoot";
    public static final String IS_PROTOCOL_SYNC = "ss::isProtocolSync";
    public static final String LAST_SYNCED_CHAIN = "ss::lastSyncedChain";

    //</editor-fold>
}
