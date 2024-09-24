package com.limechain.config;

import com.limechain.chain.Chain;
import com.limechain.chain.spec.ChainSpec;
import com.limechain.polkaj.Hash256;
import com.limechain.utils.DivLogger;
import com.limechain.utils.json.ObjectMapper;
import lombok.Getter;

import java.util.logging.Level;

// TODO: Cleanup

/**
 * Configuration class used to store any Host specific information
 */
@Getter
public class ChainService {

    private static final String GENESIS_DIR_FORMAT = "genesis/%s.json";
    private static final String WSS_PROTOCOL = "wss://";
    private static final String HTTPS_PROTOCOL = "https://";

    /**
     * The chain that the light client is communicating with.
     */
    private Chain chain;
    /**
     * The chain spec of the chain.
     */
    private ChainSpec chainSpec;

    private static final DivLogger log = new DivLogger();

    public void init(String chainString) {
        log.log(Level.INFO, "Loading chain context...");

        Chain cliChain = Chain.fromString(chainString);
        try {
            if (cliChain != null) {
                chain = cliChain;
                chainSpec = ChainSpec.newFromJSON(getGenesisPath());
            } else {
                chainSpec = new ObjectMapper(false).mapToClass(chainString, ChainSpec.class);
                chain = Chain.fromChainId(chainSpec.getId());
            }
        } catch (Exception e) {
            System.out.println("Something went wrong while loading chain data. " + e.getMessage());
        }

        log.log(Level.INFO, "✅️Loaded chain context for the " + chain.getValue() + " chain.");
    }

    /**
     * Gets the genesis file path based on the chain the node is configured
     *
     * @return genesis(chain spec) file path
     */
    public String getGenesisPath() {
        return String.format(GENESIS_DIR_FORMAT, chain.getId());
    }

    public String getWsRpcEndpoint() {
        return WSS_PROTOCOL + chain.getRpcEndpoint();
    }

    public String getHttpsEndpoint() {
        return HTTPS_PROTOCOL + chain.getRpcEndpoint();
    }

    public Hash256 getGenesisBlockHash() {
        return Hash256.from(chain.getGenesisBlockHash());
    }
}
