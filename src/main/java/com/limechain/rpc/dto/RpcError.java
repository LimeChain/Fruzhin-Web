package com.limechain.rpc.dto;

import com.limechain.teavm.annotation.Reflectable;
import lombok.Data;

@Data
@Reflectable
public class RpcError {

    private int code;
    private String message;
    private String data;
}
