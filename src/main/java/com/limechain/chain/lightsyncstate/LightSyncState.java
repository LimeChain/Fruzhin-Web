package com.limechain.chain.lightsyncstate;

import com.limechain.chain.lightsyncstate.scale.AuthoritySetReader;
import com.limechain.chain.lightsyncstate.scale.EpochChangesReader;
import com.limechain.network.protocol.warp.dto.BlockHeader;
import com.limechain.network.protocol.warp.scale.reader.BlockHeaderReader;
import com.limechain.polkaj.reader.ScaleCodecReader;
import com.limechain.utils.StringUtils;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Map;

@Getter
@ToString
public class LightSyncState {
    private BlockHeader finalizedBlockHeader;
    private EpochChanges epochChanges;
    private AuthoritySet grandpaAuthoritySet;

    public static LightSyncState decode(Map<String, String> lightSyncStateMap) {
        String header = lightSyncStateMap.get("finalizedBlockHeader");
        String epochChanges = lightSyncStateMap.get("babeEpochChanges");
        String grandpaAuthoritySet = lightSyncStateMap.get("grandpaAuthoritySet");

        if (header == null) {
            throw new IllegalStateException("finalizedBlockHeader is null");
        }
        if (epochChanges == null) {
            throw new IllegalStateException("epochChanges is null");
        }
        if (grandpaAuthoritySet == null) {
            throw new IllegalStateException("grandpaAuthoritySet is null");
        }


        LightSyncState lightSyncState = new LightSyncState();
        byte[] bytes = StringUtils.hexToBytes(header);
        lightSyncState.finalizedBlockHeader = new BlockHeaderReader()
                .read(new ScaleCodecReader(bytes));

        byte[] bytes1 = StringUtils.hexToBytes(epochChanges);
        lightSyncState.epochChanges = new EpochChangesReader()
                .read(new ScaleCodecReader(bytes1));

        lightSyncState.grandpaAuthoritySet = new AuthoritySetReader()
                .read(new ScaleCodecReader(StringUtils.hexToBytes(grandpaAuthoritySet)));

        System.out.println(lightSyncState);
        return lightSyncState;
    }
}
