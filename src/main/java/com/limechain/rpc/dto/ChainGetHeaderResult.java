package com.limechain.rpc.dto;

import com.limechain.teavm.annotation.Reflectable;
import lombok.Data;

@Data
@Reflectable
public class ChainGetHeaderResult {

    private String parentHash;
    private String number;
    private String stateRoot;
    private String extrinsicsRoot;
    private Digest digest;
}