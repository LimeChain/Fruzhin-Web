package com.limechain.config;

import com.limechain.chain.Chain;
import com.limechain.constants.RpcConstants;
import com.limechain.utils.DivLogger;
import lombok.Getter;

import java.util.List;
import java.util.logging.Level;

/**
 * Configuration class used to store any Host specific information
 */
@Getter
public class HostConfig {
    /**
     * Chain the Host is running on
     */
    private final Chain chain;
    //    private final NodeRole nodeRole;
    private final String rpcNodeAddress;

    private final String polkadotGenesisPath = "genesis/polkadot.json";
    private final String kusamaGenesisPath = "genesis/ksmcc3.json";
    private final String westendGenesisPath = "genesis/westend2.json";
    private final String localGenesisPath = "genesis/westend-local.json";

    private static final DivLogger log = new DivLogger();

    public HostConfig() {
        log.log(Level.INFO, "Loading app config...");
        this.chain = Chain.POLKADOT;
//        Optional
//                .ofNullable(network.isEmpty() ? WESTEND : fromString(network))
//                .orElseThrow(() -> new InvalidChainException(String.format("\"%s\" is not a valid chain.", network)));

//        this.nodeRole = NodeRole.LIGHT;

        log.log(Level.INFO, "Loading rpcNodeAddress...");
        switch (chain.getValue()) {
            case "POLKADOT", "LOCAL":
                rpcNodeAddress = RpcConstants.POLKADOT_WS_RPC;
                break;
            case "KUSAMA":
                rpcNodeAddress = RpcConstants.KUSAMA_WS_RPC;
                break;
            case "WESTEND":
                rpcNodeAddress = RpcConstants.WESTEND_WS_RPC;
                break;
            default:
                rpcNodeAddress = RpcConstants.POLKADOT_WS_RPC;
        }

        log.log(Level.INFO, "✅️Loaded app config for chain " + chain);
    }

    /**
     * Gets the genesis file path based on the chain the node is configured
     *
     * @return genesis(chain spec) file path
     */
    public String getGenesisPath() {
        return switch (chain) {
            case POLKADOT -> polkadotGenesisPath;
            case KUSAMA -> kusamaGenesisPath;
            case WESTEND -> westendGenesisPath;
            case LOCAL -> localGenesisPath;
        };
    }

    public List<String> getHttpsRpcEndpoints() {
        return switch (chain) {
            case POLKADOT -> RpcConstants.POLKADOT_HTTPS_RPC;
            case KUSAMA -> RpcConstants.KUSAMA_HTTPS_RPC;
            case WESTEND -> RpcConstants.WESTEND_HTTPS_RPC;
            case LOCAL -> List.of();
        };
    }
}
