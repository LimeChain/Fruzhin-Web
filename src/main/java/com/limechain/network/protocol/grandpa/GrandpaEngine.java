package com.limechain.network.protocol.grandpa;

import com.limechain.exception.scale.ScaleEncodingException;
import com.limechain.network.ConnectionManager;
import com.limechain.network.protocol.blockannounce.messages.BlockAnnounceHandshakeBuilder;
import com.limechain.network.protocol.grandpa.messages.GrandpaMessageType;
import com.limechain.network.protocol.grandpa.messages.commit.CommitMessage;
import com.limechain.network.protocol.grandpa.messages.commit.CommitMessageScaleReader;
import com.limechain.network.protocol.grandpa.messages.neighbour.NeighbourMessage;
import com.limechain.network.protocol.grandpa.messages.neighbour.NeighbourMessageBuilder;
import com.limechain.network.protocol.grandpa.messages.neighbour.NeighbourMessageScaleReader;
import com.limechain.network.protocol.grandpa.messages.neighbour.NeighbourMessageScaleWriter;
import com.limechain.polkaj.reader.ScaleCodecReader;
import com.limechain.polkaj.writer.ScaleCodecWriter;
import com.limechain.rpc.server.AppBean;
import com.limechain.sync.warpsync.WarpSyncState;
import com.limechain.utils.StringUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Engine for handling transactions on GRANDPA streams.
 */
@Log
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class GrandpaEngine {
    private static final int HANDSHAKE_LENGTH = 1;

    protected ConnectionManager connectionManager;
    private static NeighbourMessageBuilder neighbourMessageBuilder = new NeighbourMessageBuilder();
    private static BlockAnnounceHandshakeBuilder handshakeBuilder = new BlockAnnounceHandshakeBuilder();

    public GrandpaEngine() {
        connectionManager = ConnectionManager.getInstance();
    }

    public static void handleResponderStreamMessage(byte[] message, String peerId) {
        GrandpaMessageType messageType = getGrandpaMessageType(message);

        if (messageType == null) {
            log.log(Level.WARNING,
                    String.format("Unknown grandpa message type \"%d\" from Peer %s", message[0], peerId));
            return;
        }

        switch (messageType) {
            case COMMIT -> handleCommitMessage(message, peerId);
            case NEIGHBOUR -> handleNeighbourMessage(message, peerId);
            default -> log.log(Level.WARNING,
                    String.format("Unknown grandpa message type \"%s\" from Peer %s", messageType, peerId));
        }
    }

    private static GrandpaMessageType getGrandpaMessageType(byte[] message) {
        if (message.length == HANDSHAKE_LENGTH) {
            return GrandpaMessageType.HANDSHAKE;
        }
        return GrandpaMessageType.getByType(message[0]);
    }

    private static void handleNeighbourMessage(byte[] message, String peerId) {
        ScaleCodecReader reader = new ScaleCodecReader(message);
        NeighbourMessage neighbourMessage = reader.read(NeighbourMessageScaleReader.getInstance());
        log.log(Level.INFO, "Received neighbour message from Peer " + peerId + "\n" + neighbourMessage);
        new Thread(() -> AppBean.getBean(WarpSyncState.class).syncNeighbourMessage(neighbourMessage, peerId)).start();
    }

    private static void handleCommitMessage(byte[] message, String peerId) {
        ScaleCodecReader reader = new ScaleCodecReader(message);
        CommitMessage commitMessage = reader.read(CommitMessageScaleReader.getInstance());
        AppBean.getBean(WarpSyncState.class).syncCommit(commitMessage, peerId);
    }

    /**
     * Send our GRANDPA handshake on a given <b>initiator</b> stream.
     *
     * @return Grandpa handshake String
     */
    public static String getHandshake() {
        byte[] handshake = new byte[]{(byte) handshakeBuilder.getBlockAnnounceHandshake().getNodeRole()};
        return StringUtils.toHex(handshake);
    }

    /**
     * Create and get our GRANDPA neighbour message
     *
     * @return Grandpa neighbour message as hex
     */
    public static String getNeighbourMessage() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (ScaleCodecWriter writer = new ScaleCodecWriter(buf)) {
            writer.write(NeighbourMessageScaleWriter.getInstance(), neighbourMessageBuilder.getNeighbourMessage());
        } catch (IOException e) {
            throw new ScaleEncodingException(e);
        }

        return StringUtils.toHex(buf.toByteArray());
    }

    @JSBody(params = {"handshake", "protocolId"}, script = "libp.getConnections().forEach(async (peer) => {" +
                                                           "   let stream = await ItPbStream.pbStream(await libp.dialProtocol(peer.remotePeer, protocolId));" +
                                                           "    stream.write(h2b(handshake));" + "});")
    public static native void sendHandshakeToAll(String handshake, String protocolId);

    @JSBody(params = {"grandpaExport", "protocolId"}, script =
            "libp.handle(protocolId, async ({connection, stream}) => {" +
            "            ItPipe.pipe(stream, async function (source) {" +
            "                for await (const msg of source) {" + "                    let subarr = msg.subarray();" +
            "                    if(subarr.length == 1) {" +
            "                        let handshake = grandpaExport.getHandshake();" +
            "                        (await ItPbStream.pbStream(stream)).writeLP(h2b(handshake));" +
            "                    } else if (subarr.length > 1) {" +
            "                        if(subarr.slice(1)[0] == 2) {" +
            "                           let niehgbourMessage = grandpaExport.getNeighbourMessage();" +
            "                           (await ItPbStream.pbStream(stream)).writeLP(h2b(niehgbourMessage));" +
            "                        }" +
            "                        grandpaExport.handleMessage(b2h(subarr.slice(1)), connection.remotePeer.toString());" +
            "                    }" + "                }" + "            });" + "        });")
    public static native void registerHandler(JSObject grandpaExport, String protocolId);

}
