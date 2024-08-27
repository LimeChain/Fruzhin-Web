package com.limechain.rpc.dto;

import com.limechain.teavm.annotation.Reflectable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Reflectable
@NoArgsConstructor
public class RpcResponse {

    private int id;
    private final String jsonrpc = "2.0";
    private Object result;
    private RpcError error;
}
