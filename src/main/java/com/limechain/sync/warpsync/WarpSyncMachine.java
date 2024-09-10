package com.limechain.sync.warpsync;

import com.limechain.chain.ChainService;
import com.limechain.chain.lightsyncstate.Authority;
import com.limechain.chain.lightsyncstate.LightSyncState;
import com.limechain.network.Network;
import com.limechain.network.protocol.warp.dto.WarpSyncFragment;
import com.limechain.polkaj.Hash256;
import com.limechain.storage.block.SyncState;
import com.limechain.sync.warpsync.action.FinishedAction;
import com.limechain.sync.warpsync.action.RequestFragmentsAction;
import com.limechain.sync.warpsync.action.RpcFallbackAction;
import com.limechain.sync.warpsync.action.WarpSyncAction;
import com.limechain.tuple.Pair;
import com.limechain.utils.DivLogger;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Level;

@Getter
@Setter
public class WarpSyncMachine {

    private static final DivLogger log = new DivLogger();

    private Queue<WarpSyncFragment> fragmentsQueue;
    private WarpSyncAction warpSyncAction;
    private boolean isProtocolSync;
    private final PriorityQueue<Pair<BigInteger, Authority[]>> scheduledAuthorityChanges;
    private final ChainInformation chainInformation;
    private final ChainService chainService;
    private final WarpSyncState warpState;
    private final Network networkService;
    private final SyncState syncState;
    private final List<Runnable> onFinishCallbacks;

    public WarpSyncMachine(Network network, ChainService chainService, SyncState syncState,
                           WarpSyncState warpSyncState) {
        this.networkService = network;
        this.chainService = chainService;
        this.syncState = syncState;

        this.warpState = warpSyncState;
        this.scheduledAuthorityChanges = new PriorityQueue<>(Comparator.comparing(Pair::getValue0));
        this.chainInformation = new ChainInformation();
        this.onFinishCallbacks = new ArrayList<>();
        this.isProtocolSync = false;
    }

    public void nextState() {
        warpSyncAction.next(this);
    }

    public void handleState() {
        warpSyncAction.handle(this);
    }

    public boolean isSyncing() {
        return this.warpSyncAction.getClass() != FinishedAction.class;
    }

    public void start() {
        if (isProtocolSync) {
            System.out.println("Warping via warp protocol... ");
            LightSyncState initState = LightSyncState.decode(this.chainService.getChainSpec().getLightSyncState());

            if (this.syncState.getLastFinalizedBlockNumber()
                .compareTo(initState.getFinalizedBlockHeader().getBlockNumber()) < 0) {
                this.syncState.setLightSyncState(initState);
            }

            final Hash256 initStateHash = this.syncState.getLastFinalizedBlockHash();
            // Always start with requesting fragments
            System.out.println("Requesting fragments... " + initStateHash);
            this.warpSyncAction = new RequestFragmentsAction(initStateHash);
        } else {
            System.out.println("Warping via RPC... ");
            this.warpSyncAction = new RpcFallbackAction();
        }

        new Thread(() -> {
            while (this.warpSyncAction.getClass() != FinishedAction.class) {
                this.handleState();
                this.nextState();
            }

            finishWarpSync();
        }).start();
    }

    public void stop() {
        System.out.println("Stopping warp sync machine");
        this.warpSyncAction = null;
        System.out.println("Warp sync machine stopped.");
    }

    private void finishWarpSync() {
        this.warpState.setWarpSyncFinished(true);
        this.syncState.persistState();
        syncState.saveIsProtocolSync(isProtocolSync);
        System.out.println("Warp sync finished.");
        log.log(Level.INFO, "Highest known block at #" + syncState.getLastFinalizedBlockNumber());
        this.onFinishCallbacks.forEach(Runnable::run);
        this.networkService.sendBlockAnnounceHandshake();
        this.networkService.sendNeighbourMessages();
    }

    public void onFinish(Runnable function) {
        onFinishCallbacks.add(function);
    }
}
