package com.limechain.rpc.dto;

import com.limechain.teavm.annotation.Reflectable;
import lombok.Data;

import java.util.List;

@Data
@Reflectable
public class GrandpaRoundStateResult {

    private int setId;
    private RoundState best;
    private List<RoundState> background;
}