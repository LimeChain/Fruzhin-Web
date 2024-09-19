package com.limechain.network.protocol.blockannounce;

import com.limechain.network.protocol.blockannounce.teavm.BlockAnnounceHandler;
import com.limechain.network.protocol.warp.dto.BlockHeader;
import com.limechain.rpc.ChainRpcClient;
import com.limechain.rpc.dto.ChainGetHeaderResult;
import com.limechain.rpc.server.AppBean;
import com.limechain.sync.warpsync.WarpSyncState;
import com.limechain.utils.RpcUtils;
import com.limechain.utils.Stopwatch;
import lombok.extern.java.Log;
import org.teavm.jso.browser.Window;

import java.util.logging.Level;

@Log
public class BlockAnnounceService {

    private final String protocolId;
    private boolean isRegistered = false;

    // 10 second threshold starts the fallback after one.
    private static final long FALLBACK_THRESHOLD = 10_000;

    public BlockAnnounceService(String protocolId) {
        this.protocolId = protocolId;
    }

    public void sendHandshake() {
        try {
            if (!isRegistered) {
                Stopwatch stopwatch = new Stopwatch();
                BlockAnnounceEngine.registerHandler(new BlockAnnounceHandler(stopwatch), protocolId);
                isRegistered = true;

                registerFallbackRpcScheduler(stopwatch);
            }
        } catch (IllegalStateException e) {
            log.warning("Error registering block announce handler");
        }
        try {
            BlockAnnounceEngine.sendHandshakeToAll(BlockAnnounceEngine.getHandshake(), protocolId);
        } catch (IllegalStateException e) {
            log.warning("Error sending block announce handshake request");
        }
    }

    private static void registerFallbackRpcScheduler(Stopwatch stopwatch) {
        Window.setInterval(() -> new Thread(() -> {
            if (stopwatch.getElapsedTime() > FALLBACK_THRESHOLD) {
                ChainGetHeaderResult rpcResult = ChainRpcClient.getHeader(null);
                BlockHeader fallbackHeader = RpcUtils.toBlockHeader(rpcResult);
                AppBean.getBean(WarpSyncState.class).syncRuntimeUpdate(fallbackHeader);

                log.log(Level.INFO, "Synced block announce via RPC for block #" + fallbackHeader.getBlockNumber() +
                        " with hash:0x" + fallbackHeader.getHash() +
                        " parentHash:" + fallbackHeader.getParentHash() +
                        " stateRoot:" + fallbackHeader.getStateRoot());
            }
        }).start(), 6_000);
    }
}
