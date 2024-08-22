package com.limechain.network.protocol.warp;

import com.limechain.network.protocol.warp.dto.WarpSyncRequest;
import com.limechain.network.protocol.warp.dto.WarpSyncResponse;
import com.limechain.network.protocol.warp.scale.reader.WarpSyncResponseScaleReader;
import com.limechain.network.wrapper.Stream;
import com.limechain.polkaj.reader.ScaleCodecReader;
import com.limechain.utils.StringUtils;
import org.teavm.jso.JSBody;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSPromise;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class WarpSyncProtocol /*extends ProtocolHandler<WarpSyncController>*/ {
    // Sizes taken from smoldot
    public static final int MAX_REQUEST_SIZE = 32;
    public static final int MAX_RESPONSE_SIZE = 16 * 1024 * 1024;

    public WarpSyncProtocol() {
//        super(MAX_REQUEST_SIZE, MAX_RESPONSE_SIZE);
    }

    /*@Override
    protected CompletableFuture<WarpSyncController> onStartInitiator(Stream stream) {
        stream.pushHandler(new Leb128LengthFrameDecoder());
        stream.pushHandler(new WarpSyncResponseDecoder());

        stream.pushHandler(new Leb128LengthFrameEncoder());
        stream.pushHandler(new ByteArrayEncoder());
        WarpSyncProtocol.Sender handler = new WarpSyncProtocol.Sender(stream);
        stream.pushHandler(handler);
        return CompletableFuture.completedFuture(handler);
    }*/

    static class Sender implements WarpSyncController {
        public static final int MAX_QUEUE_SIZE = 50;
//        private final LinkedBlockingDeque<CompletableFuture<WarpSyncResponse>> queue =
//                new LinkedBlockingDeque<>(MAX_QUEUE_SIZE);

        public Sender() {
        }

        //        @Override
        public void onMessage(Stream stream, WarpSyncResponse msg) {
//            Objects.requireNonNull(queue.poll()).complete(msg);
//            stream.closeWrite();
        }

        @Override
        public WarpSyncResponse send(WarpSyncRequest req, String protocolId) {
            System.out.println("Request: " + req.getBlockHash());
            final var lock = new Object();

            JSPromise<JSArray<Byte>> objectJSPromise = sendRequest(StringUtils.toHex(req.getBlockHash().getBytes()), protocolId);

            objectJSPromise.then((ttt) -> {
                System.out.println(ttt);
                System.out.println(ttt.get(0));
                System.out.println(ttt.get(ttt.getLength() - 1));
                byte[] bytes = new byte[ttt.getLength()];
                for (int i = 0; i < ttt.getLength(); i++) {
                    //bytes[i] = (byte) ((int) ttt.get(i)); //fails here
                }
//                byte[] bytes = StringUtils.fromHex(ttt);
                System.out.println("Received response: " + "  " + bytes);
                System.out.println("Received response len: " + bytes.length);

                                synchronized (lock) {
//                    System.out.println("Received response: " + bytes.length + "  " + bytes);
                    ScaleCodecReader scaleCodecReader = new ScaleCodecReader(bytes);
                    WarpSyncResponse responseaa = new WarpSyncResponseScaleReader().read(scaleCodecReader);
                System.out.println(responseaa);
//                    response.set(result);
                    lock.notify();
                }
                return null;
            });

            synchronized (lock) {
                try {
                    lock.wait();
//                    byte[] bytes = response.get();
//                    System.out.println("Received response: " + /*bytes.length +*/ "  " + bytes);
//                    ScaleCodecReader scaleCodecReader = new ScaleCodecReader(bytes);
//                    WarpSyncResponse responseaa = new WarpSyncResponseScaleReader().read(scaleCodecReader);
//                    System.out.println(responseaa);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }

        @JSBody(params = {"blockHash", "protocolId"}, script = "return (async () => {" +
                                                               "let peer = libp.getConnections()[0].remotePeer;" +
                                                               "let stream = await ItPbStream.pbStream(await libp.dialProtocol(peer, protocolId));" +
                                                               "stream.writeLP(new Uint8Array([...blockHash.matchAll(/../g)].map(m => parseInt(m[0], 16))));" +
                                                               "return Array.from((await stream.readLP()).subarray());})()")
        private static native JSPromise<JSArray<Byte>> sendRequest(String blockHash, String protocolId);

    }
}
