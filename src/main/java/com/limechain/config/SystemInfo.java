package com.limechain.config;

import com.limechain.utils.DivLogger;
import lombok.Getter;

import java.util.logging.Level;

//TODO: Cleanup

/**
 * Configuration class used to hold and information used by the system rpc methods
 */
@Getter
public class SystemInfo {
    //    private final String role;
    private final ChainService chainService;
    //    private final String hostIdentity;
    private String hostName = "Fruzhin";
    private String hostVersion = "0.0.1";

    private static final DivLogger log = new DivLogger();

    public SystemInfo(ChainService chainService) {
//        this.role = network.getNodeRole().name();
        this.chainService = chainService;
//        this.hostIdentity = network.getHost().getPeerId().toString();
        logSystemInfo();
    }

    /**
     * Logs system info on node startup
     */
    public void logSystemInfo() {
        String lemonEmoji = new String(Character.toChars(0x1F34B));
        String pinEmoji = new String(Character.toChars(0x1F4CC));
        String clipboardEmoji = new String(Character.toChars(0x1F4CB));
        String labelEmoji = new String(Character.toChars(0x1F3F7));
        String authEmoji = new String(Character.toChars(0x1F464));

        log.log(Level.INFO, lemonEmoji + "LimeChain Fruzhin");
        log.log(Level.INFO, pinEmoji + "Version: " + hostVersion);
        log.log(Level.INFO, clipboardEmoji + "Chain specification: " + chainService.getChain().getValue());
        log.log(Level.INFO, labelEmoji + "Host name: " + hostName);
//        log.log(Level.INFO, authEmoji + "Role: " + role);
//        log.log(Level.INFO, "Local node identity is: " + hostIdentity);
        log.log(Level.INFO, "Operating System: " + System.getProperty("os.name"));
    }
}
