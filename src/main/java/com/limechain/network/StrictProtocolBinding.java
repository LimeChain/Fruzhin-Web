package com.limechain.network;

public abstract class StrictProtocolBinding {
    String protocolId;

    protected StrictProtocolBinding(String protocolId/*, T protocol*/) {
        this.protocolId = protocolId;
    }

}
