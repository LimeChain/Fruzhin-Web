package com.limechain;

import com.limechain.client.HostNode;
import com.limechain.client.LightClient;
import com.limechain.config.AppBean;
import com.limechain.config.ChainService;
import com.limechain.config.CommonConfig;
import com.limechain.rpc.WsRpcClient;
import com.limechain.rpc.WsRpcClientImpl;
import lombok.extern.java.Log;
import org.teavm.jso.JSBody;
import org.teavm.jso.core.JSString;

@Log
public class Main {

    private static final String WS_RPC = "wsRpc";

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalStateException(
                    "Please provide a valid chain spec string or one of the supported chain names.");
        }

        log.info("Starting LimeChain node...");

        String chainString = args[0];
        initContext(chainString);

        HostNode client = new LightClient();
        client.start();

        log.info("\uD83D\uDE80Started light client!");
    }

    private static void initContext(String chainString) {
        ChainService chainService = AppBean.getBean(ChainService.class);
        chainService.init(chainString);

        CommonConfig.start();

        exportWsRpc(new WsRpcClientImpl(), JSString.valueOf(WS_RPC));
    }

    @JSBody(params = {"c", "apiName"}, script = "window[apiName] = c;")
    private static native void exportWsRpc(WsRpcClient c, JSString apiName);
}