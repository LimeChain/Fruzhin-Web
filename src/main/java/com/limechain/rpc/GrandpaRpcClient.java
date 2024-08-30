package com.limechain.rpc;

import com.limechain.rpc.dto.GrandpaRoundStateResult;
import com.limechain.rpc.dto.RpcMethod;
import com.limechain.rpc.dto.RpcResponse;

import java.util.List;

/**
 * An implementation of {@link RpcClient}, which implements RPC calls from the "grandpa" category.
 */
public final class GrandpaRpcClient extends RpcClient {

    public static GrandpaRoundStateResult getGrandpaRoundState() {
        RpcResponse response = sendRpcRequest(RpcMethod.GRANDPA_ROUND_STATE, List.of());
        return getResult(response, GrandpaRoundStateResult.class);
    }
}
