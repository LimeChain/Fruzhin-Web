package com.limechain.rpc.dto;

import com.limechain.teavm.annotation.Reflectable;
import lombok.Data;

import java.util.List;

@Data
@Reflectable
public class Votes {

    private int currentWeight;
    private List<String> missing;
}
