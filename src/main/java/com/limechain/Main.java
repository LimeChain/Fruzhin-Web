package com.limechain;

import com.limechain.client.HostNode;
import com.limechain.client.LightClient;
import com.limechain.rpc.server.RpcApp;
import com.limechain.utils.DivLogger;

import java.util.logging.Level;

public class Main {

    private static final String WS_RPC = "wsRpc";

    private static final DivLogger log = new DivLogger();

    public static void main(String[] args) {
        exportHttpRpc(RpcClient::sendRpcRequest, JSString.valueOf(HTTP_RPC));
        exportWsRpc(new WsRpcClientImpl(), JSString.valueOf(WS_RPC));

        log.log("Starting LimeChain node...");

        RpcApp rpcApp = new RpcApp();
        rpcApp.start();

        HostNode client = new LightClient();

        // Start the client
        // NOTE: This starts the beans the client would need - mutates the global context
        client.start();
        log.log(Level.INFO, "\uD83D\uDE80Started light client!");
    }

    @JSBody(params = {"c", "apiName"}, script = "window[apiName] = c;")
    private static native void exportWsRpc(WsRpcClient c, JSString apiName);
}