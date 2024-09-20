package com.limechain.chain;

import lombok.Getter;

// TODO: Cleanup

/**
 * Stores data for the supported chains.
 */
@Getter
public enum Chain {
    POLKADOT("polkadot",
            "polkadot",
            "91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3",
            "rpc.polkadot.io"),
    KUSAMA("kusama",
            "ksmcc3",
            "b0a8d493285c2df73290dfb7e61f870f17b41801197a149ca93654499ea3dafe",
            "kusama-rpc.polkadot.io"),
    WESTEND("westend",
            "westend2",
            "e143f23803ac50e8f6f8e62695d1ce9e4e1d68aa36c1cd2cfd15340213f3423e",
            "westend-rpc.polkadot.io"),
    LOCAL("local", "local", null, null);

    /**
     * Holds the name of the chain
     */
    private final String value;
    private final String id;
    private final String genesisBlockHash;
    private final String rpcEndpoint;

    Chain(String value, String id, String genesisBlockHash, String rpcEndpoint) {
        this.value = value;
        this.id = id;
        this.genesisBlockHash = genesisBlockHash;
        this.rpcEndpoint = rpcEndpoint;
    }

    /**
     * Tries to map string parameter to an enum value
     *
     * @param chain name of the enum value to map
     * @return {@link Chain} or null if mapping is unsuccessful
     */
    public static Chain fromString(String chain) {
        for (Chain type : values()) {
            if (type.value.equals(chain)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Tries to map chain id string parameter to an enum based on its id field.
     *
     * @param id name of the enum id to map
     * @return {@link Chain} or null if mapping is unsuccessful
     */
    public static Chain fromChainId(String id) {
        for (Chain type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }
}
