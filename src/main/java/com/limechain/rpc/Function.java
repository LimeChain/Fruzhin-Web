package com.limechain.rpc;

import org.teavm.jso.JSObject;

public interface Function extends JSObject {

    String sendRequest(String method, String[] params);
}
