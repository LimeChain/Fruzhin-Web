package com.limechain.client;

import com.limechain.network.Network;
import com.limechain.rpc.server.AppBean;
import com.limechain.sync.warpsync.WarpSyncMachine;
import com.limechain.utils.DivLogger;
import lombok.SneakyThrows;

import java.util.logging.Level;

/**
 * Main light client class that starts and stops execution of
 * the client and hold references to dependencies
 */
public class LightClient implements HostNode {
    // TODO: Add service dependencies i.e rpc, sync, network, etc.
    // TODO: Do we need those as fields here...?
    private final Network network;

    private static final DivLogger log = new DivLogger();

    /**
     * @implNote the RpcApp is assumed to have been started before constructing the client,
     * as it relies on the application context
     */
    public LightClient() {
        this.network = AppBean.getBean(Network.class);
    }

    /**
     * Starts the light client by instantiating all dependencies and services
     */
    @SneakyThrows
    public void start() {
        this.network.start();
        WarpSyncMachine warpSyncMachine = AppBean.getBean(WarpSyncMachine.class);

        log.log(Level.INFO, "Syncing to latest finalized block state...");

        int retryCount = 0;
        while (retryCount < 3) {
            this.network.updateCurrentSelectedPeer();

            if (this.network.getKademliaService().getSuccessfulBootNodes() > 0) {
                warpSyncMachine.setProtocolSync(true);
                break;
            } else {
                retryCount++;
                System.out.println("Waiting to retry peer connection...");
                Thread.sleep(2000);
            }
        }

        warpSyncMachine.start();
    }
}
