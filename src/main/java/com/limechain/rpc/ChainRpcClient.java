package com.limechain.rpc;

import com.limechain.polkaj.Hash256;
import com.limechain.rpc.dto.ChainGetHeaderResult;
import com.limechain.rpc.dto.RpcMethod;
import com.limechain.rpc.dto.RpcResponse;

import java.util.List;

/**
 * An implementation of {@link RpcClient}, which implements RPC calls from the "chain" category.
 */
public final class ChainRpcClient extends RpcClient {

    public static Hash256 getLastFinalizedBlockHash() {
        RpcResponse response = sendRpcRequest(RpcMethod.CHAIN_GET_FINALIZED_HEAD, List.of());
        return Hash256.from(getResult(response, String.class));
    }

    public static ChainGetHeaderResult getHeader(String blockHash) {
        RpcResponse response = sendRpcRequest(RpcMethod.CHAIN_GET_HEADER, List.of(blockHash));
        return getResult(response, ChainGetHeaderResult.class);
    }
}
