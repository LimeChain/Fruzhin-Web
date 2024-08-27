package com.limechain.rpc;

import com.limechain.polkaj.Hash256;
import com.limechain.rpc.dto.ChainGetHeaderResult;
import com.limechain.rpc.dto.GrandpaRoundStateResult;
import com.limechain.rpc.dto.RpcMethod;
import com.limechain.rpc.dto.RpcResponse;

import java.util.List;

public final class BlockRpcClient extends RpcClient {

    public static Hash256 getLastFinalizedBlockHash() {
        RpcResponse response = sendRpcRequest(RpcMethod.CHAIN_GET_FINALIZED_HEAD, List.of());
        return Hash256.from(getResult(response, String.class));
    }

    public static ChainGetHeaderResult getHeader(String blockHash) {
        RpcResponse response = sendRpcRequest(RpcMethod.CHAIN_GET_HEADER, List.of(blockHash));
        return getResult(response, ChainGetHeaderResult.class);
    }

    public static GrandpaRoundStateResult getGrandpaRoundState() {
        RpcResponse response = sendRpcRequest(RpcMethod.GRANDPA_ROUND_STATE, List.of());
        return getResult(response, GrandpaRoundStateResult.class);
    }
}
