package com.limechain.rpc.dto;

import com.limechain.teavm.annotation.Reflectable;
import lombok.Data;

@Data
@Reflectable
public class RoundState {
    private int round;
    private int totalWeight;
    private int thresholdWeight;
    private Votes prevotes;
    private Votes precommits;
}
