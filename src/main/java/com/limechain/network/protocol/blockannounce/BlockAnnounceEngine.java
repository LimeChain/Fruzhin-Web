package com.limechain.network.protocol.blockannounce;

import com.limechain.exception.scale.ScaleEncodingException;
import com.limechain.network.protocol.blockannounce.messages.BlockAnnounceHandshakeBuilder;
import com.limechain.network.protocol.blockannounce.messages.BlockAnnounceMessage;
import com.limechain.network.protocol.blockannounce.scale.BlockAnnounceHandshakeScaleWriter;
import com.limechain.network.protocol.blockannounce.scale.BlockAnnounceMessageScaleReader;
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

@Log
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockAnnounceEngine {
    protected static BlockAnnounceHandshakeBuilder handshakeBuilder = new BlockAnnounceHandshakeBuilder();

    public static void handleBlockAnnounce(byte[] msg, String peerId) {
        ScaleCodecReader reader = new ScaleCodecReader(msg);
        BlockAnnounceMessage announce = reader.read(new BlockAnnounceMessageScaleReader());
//        connectionManager.updatePeer(peerId, announce);
        AppBean.getBean(WarpSyncState.class).syncBlockAnnounce(announce);
        log.log(Level.FINE, "Received block announce for block #" + announce.getHeader().getBlockNumber() +
                            " from " + peerId +
                            " with hash:0x" + announce.getHeader().getHash() +
                            " parentHash:" + announce.getHeader().getParentHash() +
                            " stateRoot:" + announce.getHeader().getStateRoot());
    }

    public static String getHandshake() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (ScaleCodecWriter writer = new ScaleCodecWriter(buf)) {
            writer.write(new BlockAnnounceHandshakeScaleWriter(), handshakeBuilder.getBlockAnnounceHandshake());
        } catch (IOException e) {
            throw new ScaleEncodingException(e);
        }
        return StringUtils.toHex(buf.toByteArray());
    }

    @JSBody(params = {"handshake", "protocolId"}, script = "window.fruzhin.libp.getConnections().forEach(async (peer) => {" +
                                                           "   let stream = await ItPbStream.pbStream(await window.fruzhin.libp.dialProtocol(peer.remotePeer, protocolId));" +
                                                           "    stream.writeLP(Ed25519.h2b(handshake));" +
                                                           "});")
    public static native void sendHandshakeToAll(String handshake, String protocolId);


    @JSBody(params = {"announceExport", "protocolId"}, script =
            "window.fruzhin.libp.handle(protocolId, async ({connection, stream}) => {" +
            "    ItPipe.pipe(stream, async function (source) {" +
            "        for await (const msg of source) {" +
            "            let subarr = msg.subarray();" +
            "            if(subarr.length === 69) {" +
            "                let handshake = announceExport.getHandshake();" +
            "                (await ItPbStream.pbStream(stream)).writeLP(Ed25519.h2b(handshake));" +
            "            } else if (subarr.length > 1) {" +
            "                 announceExport.blockAnnounce(Ed25519.b2h(subarr.slice(2)), connection.remotePeer.toString());" +
            "            }" +
            "        }" +
            "    });" +
            "});" +
            "fruzhin.libp.addEventListener('peer:connect', async (evt) => {" +
            "    let handshake = announceExport.getHandshake();" +
            "    (await ItPbStream.pbStream(await window.fruzhin.libp.dialProtocol(evt.detail, protocolId))).writeLP(Ed25519.h2b(handshake));" +
            "});")
    public static native void registerHandler(JSObject announceExport, String protocolId);
}
