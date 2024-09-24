package com.limechain.chain.spec;

import com.limechain.teavm.annotation.Reflectable;
import com.limechain.utils.json.JsonUtil;
import com.limechain.utils.json.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * Contains the chain spec data, deserialized and parsed in-memory into appropriate structures
 */
@Getter
@Setter
@Reflectable
public class ChainSpec implements Serializable {
    private String id;
    private String name;
    private String protocolId;
    private String[] bootNodes;
    private Map<String, String> lightSyncState;

    /**
     * Loads chain specification data from json file and maps its fields
     *
     * @param pathToChainSpecJSON path to the chain specification json file
     * @return class instance mapped to the json file
     */
    public static ChainSpec newFromJSON(String pathToChainSpecJSON) {
        ObjectMapper mapper = new ObjectMapper(false);
        String jsonChainSpec = JsonUtil.readJsonFromFile(pathToChainSpecJSON);
        return mapper.mapToClass(jsonChainSpec, ChainSpec.class);
    }
}
