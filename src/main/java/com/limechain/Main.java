package com.limechain;

import com.limechain.client.HostNode;
import com.limechain.client.LightClient;
import com.limechain.rpc.Function;
import com.limechain.rpc.RpcClient;
import com.limechain.rpc.server.RpcApp;
import com.limechain.utils.DivLogger;
import org.teavm.jso.JSBody;
import org.teavm.jso.core.JSString;

import java.util.logging.Level;

public class Main {

    private static final DivLogger log = new DivLogger();

    public static void main(String[] args) {
        log.log("Starting LimeChain node...");
        exportAPI(RpcClient::sendRpcRequest, JSString.valueOf("rpc"));

        RpcApp rpcApp = new RpcApp();
        rpcApp.start();

        HostNode client = new LightClient();

        // Start the client
        // NOTE: This starts the beans the client would need - mutates the global context
        client.start();
        log.log(Level.INFO, "\uD83D\uDE80Started light client!");
    }

    @JSBody(params = {"f", "apiName"}, script = "window[apiName] = f;" +
        "isRpcExported = true;")
    private static native void exportAPI(Function f, JSString apiName);
}