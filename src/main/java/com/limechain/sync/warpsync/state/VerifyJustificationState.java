package com.limechain.sync.warpsync.state;

import com.limechain.chain.lightsyncstate.Authority;
import com.limechain.network.protocol.warp.dto.ConsensusEngine;
import com.limechain.network.protocol.warp.dto.HeaderDigest;
import com.limechain.network.protocol.warp.dto.WarpSyncFragment;
import com.limechain.network.protocol.warp.dto.WarpSyncJustification;
import com.limechain.sync.JustificationVerifier;
import com.limechain.sync.warpsync.SyncedState;
import com.limechain.sync.warpsync.WarpSyncMachine;
import com.limechain.sync.warpsync.dto.AuthoritySetChange;
import com.limechain.sync.warpsync.dto.GrandpaMessageType;
import com.limechain.sync.warpsync.scale.ForcedChangeReader;
import com.limechain.sync.warpsync.scale.ScheduledChangeReader;
import io.emeraldpay.polkaj.scale.ScaleCodecReader;
import lombok.extern.java.Log;
import org.javatuples.Pair;

import java.math.BigInteger;
import java.util.Queue;
import java.util.logging.Level;

// VerifyJustificationState is going to be instantiated a lot of times
// Maybe we can make it a singleton in order to reduce performance overhead?
@Log
public class VerifyJustificationState implements WarpSyncState {
    private final SyncedState syncedState = SyncedState.getInstance();
    private Exception error;

    @Override
    public void next(WarpSyncMachine sync) {
        if (this.error != null) {
            // Not sure what state we should transition to here.
            sync.setWarpSyncState(new FinishedState());
            return;
        }

        if (!sync.getFragmentsQueue().isEmpty()) {
            sync.setWarpSyncState(new VerifyJustificationState());
        } else if (syncedState.isFinished()) {
            sync.setWarpSyncState(new RuntimeDownloadState());
        } else {
            sync.setWarpSyncState(new RequestFragmentsState(syncedState.getLastFinalizedBlockHash()));
        }
    }

    @Override
    public void handle(WarpSyncMachine sync) {
        try {
            handleScheduledEvents(sync);

            WarpSyncFragment fragment = sync.getFragmentsQueue().poll();
            log.log(Level.INFO, "Verifying justification...");
            WarpSyncJustification justification;
            if (fragment == null) {
                throw new RuntimeException("No such fragment");
            }
            boolean verified = JustificationVerifier.verify(
                    fragment.getJustification().precommits,
                    fragment.getJustification().round);
            if (!verified) {
                throw new RuntimeException("Justification could not be verified.");
            }

            // Set the latest finalized header and number
            // TODO: Persist header to DB?
            syncedState.setStateRoot(fragment.getHeader().getStateRoot());
            syncedState.setLastFinalizedBlockHash(fragment.getJustification().targetHash);
            syncedState.setLastFinalizedBlockNumber(fragment.getJustification().targetBlock);

            try {
                handleAuthorityChanges(sync, fragment);
                log.log(Level.INFO, "Verified justification. Block hash is now at #"
                        + syncedState.getLastFinalizedBlockNumber() + ": "
                        + syncedState.getLastFinalizedBlockHash().toString()
                        + " with state root " + syncedState.getStateRoot());
            } catch (Exception error) {
                this.error = error;
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Error while verifying justification: " + e.getMessage());
            this.error = e;
        }
    }

    public void handleAuthorityChanges(WarpSyncMachine sync, WarpSyncFragment fragment) {
        // Update authority set and set id
        AuthoritySetChange authorityChanges;
        for (HeaderDigest digest : fragment.getHeader().getDigest()) {
            if (digest.getId() == ConsensusEngine.GRANDPA) {
                ScaleCodecReader reader = new ScaleCodecReader(digest.getMessage());
                GrandpaMessageType type = GrandpaMessageType.fromId(reader.readByte());

                switch (type) {
                    case SCHEDULED_CHANGE -> {
                        ScheduledChangeReader authorityChangesReader = new ScheduledChangeReader();
                        authorityChanges = authorityChangesReader.read(reader);
                        sync.getScheduledAuthorityChanges()
                                .add(new Pair<>(authorityChanges.getDelay(), authorityChanges.getAuthorities()));
                        return;
                    }
                    case FORCED_CHANGE -> {
                        ForcedChangeReader authorityForcedChangesReader = new ForcedChangeReader();
                        authorityChanges = authorityForcedChangesReader.read(reader);
                        sync.getScheduledAuthorityChanges()
                                .add(new Pair<>(authorityChanges.getDelay(), authorityChanges.getAuthorities()));
                        return;
                    }
                    case ON_DISABLED -> {
                        log.log(Level.SEVERE, "'ON DISABLED' grandpa message not implemented");
                        return;
                    }
                    case PAUSE -> {
                        log.log(Level.SEVERE, "'PAUSE' grandpa message not implemented");
                        return;
                    }
                    case RESUME -> {
                        log.log(Level.SEVERE, "'RESUME' grandpa message not implemented");
                        return;
                    }
                    default -> {
                        log.log(Level.SEVERE, "Could not get grandpa message type");
                        throw new IllegalStateException("Unknown grandpa message type");
                    }
                }
            }
        }
    }

    public void handleScheduledEvents(WarpSyncMachine sync) {
        Queue<Pair<BigInteger, Authority[]>> eventQueue = sync.getScheduledAuthorityChanges();
        Pair<BigInteger, Authority[]> data = eventQueue.peek();
        while (data != null) {
            if (data.getValue0().compareTo(syncedState.getLastFinalizedBlockNumber()) != 1) {
                syncedState.setAuthoritySet(data.getValue1());
                syncedState.setSetId(syncedState.getSetId().add(BigInteger.ONE));
                sync.getScheduledAuthorityChanges().poll();
            } else break;
            data = eventQueue.peek();
        }
    }

}
