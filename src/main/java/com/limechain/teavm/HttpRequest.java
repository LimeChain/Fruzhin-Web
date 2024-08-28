package com.limechain.teavm;

import lombok.extern.java.Log;
import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSError;

import java.util.logging.Level;

@Log
public class HttpRequest {

    @Async
    public static native String asyncHttpRequest(String method, String url, String body);

    private static void asyncHttpRequest(String method, String url, String body, AsyncCallback<String> callback) {
        createHttpRequest(method, url, body, (error, response) -> {
            if (error != null) {
                log.log(Level.WARNING, error.getMessage());
            } else {
                callback.complete(response);
            }
        });
    }

    @JSBody(params = {"method", "url", "body", "callback"}, script = "return asyncHttpRequest(method, url, body, callback);")
    private static native void createHttpRequest(String method, String url, String body, HttpRequestCallback callback);

    @JSBody(params = {"method", "url", "body"}, script = "return httpRequestSync(method, url, body);")
    public static native String createHttpRequest(String method, String url, String body);

    @JSFunctor
    private interface HttpRequestCallback extends JSObject {
        void apply(JSError error, String response);
    }
}
