package com.limechain.rpc.dto;

import lombok.Getter;

@Getter
public enum RpcMethod {

    CHAIN_GET_FINALIZED_HEAD("chain_getFinalizedHead"),
    CHAIN_GET_HEADER("chain_getHeader"),
    GRANDPA_ROUND_STATE("grandpa_roundState");

    RpcMethod(String method) {
        this.method = method;
    }

    private final String method;
}
