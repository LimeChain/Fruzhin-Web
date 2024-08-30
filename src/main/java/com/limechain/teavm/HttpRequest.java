package com.limechain.teavm;

import lombok.extern.java.Log;
import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;
import org.teavm.jso.JSBody;

import java.util.logging.Level;

@Log
public class HttpRequest {

    @Async
    public static native String asyncHttpRequest(String method, String url, String body);

    private static void asyncHttpRequest(String method, String url, String body, AsyncCallback<String> callback) {
        createAsyncHttpRequest(method, url, body, (error, response) -> {
            if (error != null) {
                log.log(Level.WARNING, error.getMessage());
            } else {
                callback.complete(response);
            }
        });
    }

    @JSBody(params = {"method", "url", "body", "callback"}, script = "return asyncHttpRequest(method, url, body, callback);")
    public static native void createAsyncHttpRequest(String method, String url, String body, TeaVMCallback callback);

    @JSBody(params = {"method", "url", "body"}, script = "return httpRequestSync(method, url, body);")
    public static native String createHttpRequest(String method, String url, String body);
}



