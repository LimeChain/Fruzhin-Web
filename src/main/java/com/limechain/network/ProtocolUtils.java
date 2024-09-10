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

    public static String getGrandpaProtocol() {
        return "/paritytech/grandpa/1";
    }

}