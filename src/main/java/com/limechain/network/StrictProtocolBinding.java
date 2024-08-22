package com.limechain.network;

import com.limechain.network.wrapper.Stream;
import org.teavm.jso.JSBody;
import org.teavm.jso.core.JSPromise;

import java.util.concurrent.atomic.AtomicReference;

public abstract class StrictProtocolBinding {
    String protocolId;

    protected StrictProtocolBinding(String protocolId/*, T protocol*/) {
        this.protocolId = protocolId;
    }

    public Stream dialPeer(/*PeerId peer*/) {
        Object peer1 = getPeer();
        System.out.println("Peer: " + peer1);
        JSPromise<Object> dial = dial(peer1, protocolId);
        final var lock = new Object();
        AtomicReference<Stream> stream = new AtomicReference<>();

        dial.then((result) -> {
            stream.set((Stream) result);
            synchronized (lock) {
                lock.notify();
            }
            return null;
        });

        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Stream atomicStream = stream.get();

        System.out.println("Stream: " + toJson(atomicStream));
        return atomicStream;
    }

    @JSBody(params = {"object"}, script = "return JSON.stringify(object);")
    private static native String toJson(Object object);

    @JSBody(params = {"peerId", "protocolId"}, script = "return (async () => ItPbStream.pbStream(await libp.dialProtocol(peerId, protocolId)))()")
    private static native JSPromise<Object> dial(Object peerId, String protocolId);

    @JSBody(script = "return libp.getConnections()[0].remotePeer;")
    private static native Object getPeer();
}
