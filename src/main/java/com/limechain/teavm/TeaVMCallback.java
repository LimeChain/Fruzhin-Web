package com.limechain.teavm;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSError;

@JSFunctor
public interface TeaVMCallback extends JSObject {
    void apply(JSError error, String response);
}
