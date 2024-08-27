package com.limechain.rpc.dto;

import com.limechain.teavm.annotation.Reflectable;
import lombok.Data;

import java.util.List;

@Data
@Reflectable
public class Digest {

    private List<String> logs;
}
