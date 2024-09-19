package com.limechain.rpc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WebsocketState {

    CONNECTING(0),
    OPEN(1),
    CLOSING(2),
    CLOSED(3);

    private final int intValue;
}
