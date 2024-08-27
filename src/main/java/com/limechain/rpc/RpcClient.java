package com.limechain.rpc;

import com.limechain.config.HostConfig;
import com.limechain.rpc.dto.RpcMethod;
import com.limechain.rpc.dto.RpcRequest;
import com.limechain.rpc.dto.RpcResponse;
import com.limechain.rpc.server.AppBean;
import com.limechain.teavm.HttpRequest;
import com.limechain.utils.json.JsonUtil;
import com.limechain.utils.json.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public sealed class RpcClient permits BlockRpcClient {

    private static final String POST = "POST";

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(1);
    private static final LoadBalancer LOAD_BALANCER = new LoadBalancer(AppBean.getBean(HostConfig.class));
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(false);

    private static String createRpcRequestJson(String method, List<Object> params) {
        RpcRequest request = new RpcRequest(ID_COUNTER.getAndAdd(1), method, params);
        return JsonUtil.stringify(request);
    }

    protected static RpcResponse sendRpcRequest(RpcMethod method, List<Object> params) {
        String jsonResult = HttpRequest.asyncHttpRequest(POST, LOAD_BALANCER.getNextEndpoint(),
            createRpcRequestJson(method.getMethod(), params));
        return OBJECT_MAPPER.mapToClass(jsonResult, RpcResponse.class);
    }

    protected static <T> T getResult(RpcResponse response, Class<T> type) {
        if (response.getError() != null) {
            throw new IllegalStateException("RPC request resulted in an error with code:" + response.getError().getCode()
                + " and message:" + response.getError().getMessage());
        }

        return OBJECT_MAPPER.mapToClass(JsonUtil.stringify(response.getResult()), type);
    }
}
