package com.limechain.rpc.dto;

import com.limechain.teavm.annotation.Reflectable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Reflectable
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest {

    private int id;
    private final String jsonrpc = "2.0";
    private String method;
    private List<Object> params;
}
