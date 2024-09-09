package com.limechain.network;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProtocolUtils {
    public static final String PING_PROTOCOL = "/ipfs/ping/1.0.0";

    public static String getWarpSyncProtocol(String chainId) {
        return String.format("/%s/sync/warp", chainId);
    }

    public static String getBlockAnnounceProtocol(String chainId) {
        return String.format("/%s/block-announces/1", chainId);
    }

    public static String getKadProtocol(String chainId) {
        return String.format("/%s/kad", chainId);
    }

    public static String getGrandpaProtocol(String chainId) {
        return String.format("/%s/grandpa/1", grandpaProtocolChain(chainId));
    }

    //TODO: figure out a more elegant solution
    private static String grandpaProtocolChain(String chainId) {
        return chainId.equals("dot") ? "paritytech" : chainId;
    }
}