package com.limechain.teavm;

import lombok.extern.java.Log;
import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;
import org.teavm.jso.JSBody;

import java.util.logging.Level;

@Log
public class HttpRequest {

    @Async
    public static native String sendHttpRequest(String method, String url, String body);

    private static void sendHttpRequest(String method, String url, String body, AsyncCallback<String> callback) {
        sendHttpRequestNative(method, url, body, (error, response) -> {
            if (error != null) {
                log.log(Level.WARNING, error.getMessage());
            } else {
                callback.complete(response);
            }
        });
    }

    @JSBody(params = {"method", "url", "body", "callback"}, script = "fetch(" +
            "url, {" +
            "   method: method," +
            "   headers: {" +
            "       'Content-Type': 'application/json'}," +
            "   body: method === 'POST' ? body : undefined" +
            "})" +
            ".then(response => {" +
            "   if (!response.ok) {" +
            "       throw new Error(`Request failed with status: ${response.status}`);" +
            "   }" +
            "   return response.text();" +
            "})" +
            ".then(result => {" +
            "   callback(null, result);" +
            "})" +
            ".catch(error => {" +
            "   callback(new Error(`Error during sending request: ${error.message}`), null);" +
            "});")
    public static native void sendHttpRequestNative(String method, String url, String body, TeaVMCallback callback);
}



