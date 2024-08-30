package com.limechain.rpc;

import org.teavm.jso.JSObject;

/**
 * A functional interface used to export rpc functionalities to the user. A function which conforms with "sendRequest"
 * signature can be exported via {@link com.limechain.Main}{@code .exportAPI(Function, JSString)}
 */
@FunctionalInterface
public interface Function extends JSObject {

    String sendRequest(String method, String[] params);
}
