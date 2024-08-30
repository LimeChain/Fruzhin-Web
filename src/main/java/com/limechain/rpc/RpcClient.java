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

/**
 * Base class for executing RPC requests.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public sealed class RpcClient permits ChainRpcClient, GrandpaRpcClient {

    private static final String POST = "POST";
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(1);
    private static final LoadBalancer LOAD_BALANCER = new LoadBalancer(AppBean.getBean(HostConfig.class));
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(false);

    /**
     * Send an RPC request. Currently used only by the exported RPC client.
     *
     * @param method {@link String} representation of the RPC method name. For example "system_name".
     * @param params An array of parameters for the sent RPC request.
     * @return The {@link String} representation of the received RPC json result.
     */
    public static String sendRpcRequest(String method, Object[] params) {
        return HttpRequest.createHttpRequest(POST, LOAD_BALANCER.getNextEndpoint(),
            createRpcRequestJson(method, List.of(params)));
    }

    /**
     * Send an RPC request. Used by the specific implementations of the RpcClient.
     *
     * @param method Enum representation of an RPC method name.
     * @param params An array of parameters for the sent RPC request.
     * @return The {@link RpcResponse} representation of the received RPC json result.
     */
    protected static RpcResponse sendRpcRequest(RpcMethod method, List<Object> params) {
        String jsonResult = HttpRequest.asyncHttpRequest(POST, LOAD_BALANCER.getNextEndpoint(),
            createRpcRequestJson(method.getMethod(), params));
        return OBJECT_MAPPER.mapToClass(jsonResult, RpcResponse.class);
    }

    private static String createRpcRequestJson(String method, List<Object> params) {
        RpcRequest request = new RpcRequest(ID_COUNTER.getAndAdd(1), method, params);
        return JsonUtil.stringify(request);
    }

    /**
     * Method used to map an {@link RpcResponse} result to a provided class type. This is needed because TeaVM does not
     * support use of {@link java.lang.reflect.ParameterizedType} and we cannot use an object mapper with generics
     * inside.
     *
     * @param response the {@link RpcResponse} whose result we have to map to an object.
     * @param klazz    the desired class for the mapping.
     * @return a mapped version of the response result in the form of the provided {@code klazz} type.
     */
    protected static <T> T getResult(RpcResponse response, Class<T> klazz) {
        if (response.getError() != null) {
            throw new IllegalStateException("RPC request resulted in an error with code:" + response.getError().getCode()
                + " and message:" + response.getError().getMessage());
        }

        return OBJECT_MAPPER.mapToClass(JsonUtil.stringify(response.getResult()), klazz);
    }
}
