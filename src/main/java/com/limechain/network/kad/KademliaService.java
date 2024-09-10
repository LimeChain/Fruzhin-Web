package com.limechain.network.kad;

import com.limechain.network.kad.dto.Host;
import com.limechain.network.kad.dto.PeerId;
import com.limechain.network.protocol.NetworkService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.teavm.jso.JSBody;

import java.util.logging.Level;

/**
 * Service used for operating the Kademlia distributed hash table.
 */
@Getter
@Log
public class KademliaService extends NetworkService {
    public static final int REPLICATION = 20;

    @Setter
    private Host host;
    private int successfulBootNodes;

    /**
     * Connects to boot nodes to the Kademlia dht
     *
     * @param bootNodes boot nodes set in ChainService
     * @return the number of successfully connected nodes
     */
    public int connectBootNodes(String[] bootNodes) {
        startNetwork(bootNodes);
        Object peer = getPeerId();
        while (peer.toString().equalsIgnoreCase("undefined")) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            peer = getPeerId();
        }
        String peerIdStr = peer.toString();
        byte[] privateKey = getPeerPrivateKey();
        byte[] publicKey = getPeerPublicKey();

        PeerId peerId = new PeerId(privateKey, publicKey, peerIdStr);
        this.host = new Host(peerId);

        successfulBootNodes = getPeerStoreSize();

        if (successfulBootNodes > 0)
            log.log(Level.INFO, "Successfully connected to " + successfulBootNodes + " boot nodes");
        else log.log(Level.SEVERE, "Failed to connect to boot nodes");
        return successfulBootNodes;
    }

    public void updateSuccessfulBootNodes() {
        successfulBootNodes = getPeerStoreSize();
    }

    @JSBody(params = {"bootNodes"}, script = "window.fruzhin.startLibp2p(bootNodes)")
    public static native void startNetwork(String[] bootNodes);

    @JSBody(script = "return window.fruzhin.libp?.peerId")
    public static native Object getPeerId();

    @JSBody(script = "return window.fruzhin.libp.peerId.privateKey")
    public static native byte[] getPeerPrivateKey();

    @JSBody(script = "return window.fruzhin.libp.peerId.publicKey")
    public static native byte[] getPeerPublicKey();

    @JSBody(script = "return window.fruzhin.libp.getConnections().length")
    public static native int getPeerStoreSize();

    /**
     * Populates Kademlia dht with peers closest in distance to a random id then makes connections with our node
     */
    @JSBody(script = "window.fruzhin.libp.peerStore.forEach( async (p) => {" +
                     "    for await (const foundPeer of dht.peerRouting.getClosestPeers(p.id.toBytes())){" +
                     "        if(foundPeer.peer?.multiaddrs?.length > 0){" +
                     "            try{window.fruzhin.libp.dial(foundPeer.peer)}finally{}" +
                     "        }" +
                     "    }" +
                     "});")
    public static native void findNewPeers();

}