package com.limechain.network;

import com.limechain.config.AppBean;
import com.limechain.config.ChainService;
import com.limechain.network.kad.KademliaService;
import com.limechain.network.protocol.blockannounce.BlockAnnounceService;
import com.limechain.network.protocol.grandpa.GrandpaService;
import com.limechain.network.protocol.warp.WarpSyncService;
import com.limechain.network.protocol.warp.dto.WarpSyncResponse;
import com.limechain.sync.warpsync.WarpSyncState;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.Random;
import java.util.logging.Level;

//TODO: Cleanup

/**
 * A Network class that handles all peer connections and Kademlia
 */
@Log
public class Network {
    private static final Random RANDOM = new Random();
    @Getter
    private final String[] bootNodes;
    //    private final ConnectionManager connectionManager;
    @Getter
    private KademliaService kademliaService;
    private WarpSyncService warpSyncService;
    private GrandpaService grandpaService;
    private BlockAnnounceService blockAnnounceService;
    private boolean started = false;

    /**
     * Initializes a host for the peer connection,
     * Initializes the Kademlia service
     * Manages if nodes running locally are going to be allowed
     * Connects Kademlia to boot nodes
     *
     * @param chainService chain specification information containing boot nodes
     */
    public Network(ChainService chainService) {
        this.bootNodes = chainService.getChainSpec().getBootNodes();
//        this.connectionManager = ConnectionManager.getInstance();
        this.initializeProtocols(chainService);
    }

    private void initializeProtocols(ChainService config) {

        String chainId = config.getChainSpec().getProtocolId();
        String warpProtocolId = ProtocolUtils.getWarpSyncProtocol(chainId);
        String blockAnnounceProtocolId = ProtocolUtils.getBlockAnnounceProtocol(chainId);
        String grandpaProtocolId = ProtocolUtils.getGrandpaProtocol();

        kademliaService = new KademliaService();
        warpSyncService = new WarpSyncService(warpProtocolId);
        blockAnnounceService = new BlockAnnounceService(blockAnnounceProtocolId);
        grandpaService = new GrandpaService(grandpaProtocolId);

    }

//    private Ed25519PrivateKey loadPrivateKeyFromDB(KVRepository<String, Object> repository) {
//        Ed25519PrivateKey privateKey;
//
//        Optional<Object> peerIdKeyBytes = repository.find(DBConstants.PEER_ID);
//        if (peerIdKeyBytes.isPresent()) {
//            privateKey = Ed25519Utils.loadPrivateKey((byte[]) peerIdKeyBytes.get());
//            log.log(Level.INFO, "PeerId loaded from database!");
//        } else {
//            privateKey = Ed25519Utils.generateKeyPair();
//            repository.save(DBConstants.PEER_ID, privateKey.raw());
//            log.log(Level.INFO, "Generated new peerId!");
//        }
//        return privateKey;
//    }

    public void start() {
        log.log(Level.INFO, "Starting network module...");

        kademliaService.connectBootNodes(this.bootNodes);
        started = true;
        log.log(Level.INFO, "Started network module!");
    }

    public void stop() {
        log.log(Level.INFO, "Stopping network module...");
        started = false;
//        connectionManager.removeAllPeers();
//        host.stop();
        log.log(Level.INFO, "Stopped network module!");
    }

    public void updateCurrentSelectedPeer() {
        kademliaService.updateSuccessfulBootNodes();
    }

    /**
     * Periodically searches for new peers and connects to them
     * Logs the number of connected peers excluding boot nodes
     * By default Spring Boot uses a thread pool of size 1, so each call will be executed one at a time.
     */
//    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void findPeers() {
        if (!started) {
            return;
        }
        kademliaService.findNewPeers();
    }

    //    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void pingPeers() {
        log.log(Level.INFO, "Pinging peers...");
//        connectionManager.getPeerIds().forEach(this::ping);
    }

//    private void ping(PeerId peerId) {
//        try {
//            Long latency = ping.ping(host, host.getAddressBook(), peerId);
//            log.log(Level.INFO, String.format("Pinged peer: %s, latency %s ms", peerId, latency));
//        } catch (Exception e) {
//            log.log(Level.WARNING, String.format("Failed to ping peer: %s. Removing from active connections", peerId));
//            if (this.currentSelectedPeer.equals(peerId)) {
//                updateCurrentSelectedPeer();
//            }
//        }
//    }

    //    public BlockResponse syncBlock(PeerId peerId, BigInteger lastBlockNumber) {
//        this.currentSelectedPeer = peerId;
//        // TODO: fields, hash, direction and maxBlocks values not verified
//        // TODO: when debugging could not get a value returned
//        return this.makeBlockRequest(
//                new BlockRequestDto(19, null, lastBlockNumber.intValue(), Direction.Ascending, 1));
//    }
//
//    public BlockResponse makeBlockRequest(BlockRequestDto blockRequestDto) {
//        return syncService.getProtocol().remoteBlockRequest(
//                this.host,
//                this.currentSelectedPeer,
//                blockRequestDto
//        );
//    }
//
//    public SyncMessage.StateResponse makeStateRequest(String blockHash, ByteString after) {
//        return stateService.getProtocol().remoteStateRequest(
//                this.host,
//                this.currentSelectedPeer,
//                blockHash,
//                after
//        );
//    }
//
    public WarpSyncResponse makeWarpSyncRequest(String blockHash) {
        return this.warpSyncService.getProtocol().warpSyncRequest(
                blockHash);
    }

    //
//    public LightClientMessage.Response makeRemoteReadRequest(String blockHash, String[] keys) {
//        if (isPeerInvalid()) return null;
//
//        return this.lightMessagesService.getProtocol().remoteReadRequest(
//                this.host,
//                this.currentSelectedPeer,
//                blockHash,
//                keys);
//
//    }
//
//    private boolean isPeerInvalid() {
//        if (this.currentSelectedPeer == null) {
//            log.log(Level.WARNING, "No peer selected for warp sync request.");
//            return true;
//        }
//
//        if (this.host.getAddressBook().get(this.currentSelectedPeer).join() == null) {
//            log.log(Level.WARNING, "Peer not found in address book.");
//            return true;
//        }
//        return false;
//    }
//

    public void sendBlockAnnounceHandshake() {
        new Thread(() ->
                blockAnnounceService.sendHandshake()
        ).start();
    }

    //    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void sendNeighbourMessages() {
        if (!AppBean.getBean(WarpSyncState.class).isWarpSyncFinished()) {
            return;
        }
        grandpaService.sendHandshake();
    }
}
