package com.limechain.network.protocol.warp;

import com.limechain.network.protocol.warp.dto.WarpSyncRequest;
import com.limechain.network.protocol.warp.dto.WarpSyncResponse;
import com.limechain.network.protocol.warp.scale.reader.WarpSyncResponseScaleReader;
import com.limechain.polkaj.reader.ScaleCodecReader;
import com.limechain.utils.StringUtils;
import org.teavm.jso.JSBody;
import org.teavm.jso.core.JSPromise;
import org.teavm.jso.core.JSString;

import java.util.concurrent.atomic.AtomicReference;

public class WarpSyncProtocol {

    public WarpSyncProtocol() {
    }

    static class Sender implements WarpSyncController {

        public Sender() {
        }

        @Override
        public WarpSyncResponse send(WarpSyncRequest req, String protocolId) {
            final var lock = new Object();

            AtomicReference<byte[]> response = new AtomicReference<>();
            JSPromise<JSString> objectJSPromise =
                    sendRequest(StringUtils.toHex(req.getBlockHash().getBytes()), protocolId);

            objectJSPromise.then((ttt) -> {
                synchronized (lock) {
                    String str = ttt.stringValue();
                    byte[] bytes = StringUtils.fromHex(str);

                    response.set(bytes);
                    lock.notify();
                }
                return null;
            });

            synchronized (lock) {
                try {
                    lock.wait();
                    byte[] bytes = response.get();
                    ScaleCodecReader scaleCodecReader = new ScaleCodecReader(bytes);

                    return new WarpSyncResponseScaleReader().read(scaleCodecReader);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @JSBody(params = {"blockHash", "protocolId"}, script = "return (async () => {" +
                                                               "    let peer = window.fruzhin.libp.getConnections()[0].remotePeer;" +
                                                               "    let stream = await ItPbStream.pbStream(await window.fruzhin.libp.dialProtocol(peer, protocolId));" +
                                                               "    stream.writeLP(new Uint8Array([...blockHash.matchAll(/../g)].map(m => parseInt(m[0], 16))));" +
                                                               "    let bytes = (await stream.readLP()).subarray();" +
                                                               "    return [...bytes].map(n => n.toString(16).padStart(2, '0')).join('');" +
                                                               "})()")
        private static native JSPromise<JSString> sendRequest(String blockHash, String protocolId);

    }
}
