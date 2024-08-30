package com.limechain.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RpcConstants {
    public static final String POLKADOT_WS_RPC = "wss://rpc.polkadot.io";
    public static final String KUSAMA_WS_RPC = "wss://kusama-rpc.polkadot.io";
    public static final String WESTEND_WS_RPC = "wss://westend-rpc.polkadot.io";

    public static final List<String> POLKADOT_HTTPS_RPC = List.of(
        "https://rpc.ibp.network/polkadot",
        "https://polkadot-rpc.dwellir.com",
        "https://rpc.polkadot.io"
    );
    public static final List<String> KUSAMA_HTTPS_RPC = List.of(
        "https://rpc.ibp.network/kusama",
        "https://kusama-rpc.dwellir.com",
        "https://kusama-rpc.polkadot.io"
    );
    public static final List<String> WESTEND_HTTPS_RPC = List.of(
        "https://rpc.ibp.network/westend",
        "https://westend-rpc.dwellir.com",
        "https://westend-rpc.polkadot.io"
    );
}