package com.limechain.teavm;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

public class TeaVMScheduler {

    public static void schedule(TeaVMRunnable task, int intervalMillis) {
        scheduleNative(task, intervalMillis);
    }

    @JSBody(params = {"callback", "interval"}, script = "setInterval(callback, interval);")
    private static native void scheduleNative(TeaVMRunnable callback, int intervalMillis);

    @JSFunctor
    public interface TeaVMRunnable extends JSObject {
        void run();
    }
}
