package com.limechain.network.protocol.warp.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;

@Getter
@Setter
@ToString
public class WarpSyncResponse {
    private WarpSyncFragment[] fragments;
    private boolean isFinished;

    @Override
    public String toString() {
        return "WarpSyncResponse{" +
                "fragments=" + Arrays.toString(fragments) +
                ", isFinished=" + isFinished +
                '}';
    }
}
