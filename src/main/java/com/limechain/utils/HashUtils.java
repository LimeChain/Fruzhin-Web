package com.limechain.utils;

import lombok.experimental.UtilityClass;
import org.teavm.jso.JSBody;
import org.teavm.jso.core.JSString;

@UtilityClass
public class HashUtils {

    @JSBody(params = {"inputHex"}, script = "{" +
                                            "let bytes = new Uint8Array([...inputHex.matchAll(/../g)].map(m => parseInt(m[0], 16)));" +
                                            "return Blake2b.hash(bytes,undefined,32);" + "}")
    public static native JSString hashWithBlake2b(String inputHex);
}
