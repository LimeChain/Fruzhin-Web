package com.limechain.rpc;

import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSString;

/**
 * TeaVM overlay interface for a client used to communicate with a full node's RPC server.
 */
public interface WsRpcClient extends JSObject {

    void send(JSString rpcString);

    String nextResponse();
}
