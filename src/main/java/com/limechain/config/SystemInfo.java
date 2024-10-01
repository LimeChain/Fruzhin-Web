package com.limechain.config;

import lombok.Getter;
import lombok.extern.java.Log;

//TODO: Cleanup

/**
 * Configuration class used to hold and information used by the system rpc methods
 */
@Getter
@Log
public class SystemInfo {
    //    private final String role;
    private final ChainService chainService;
    //    private final String hostIdentity;
    private String hostName = "Fruzhin";
    private String hostVersion = "0.0.1";

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

        log.fine(lemonEmoji + "LimeChain Fruzhin");
        log.fine(pinEmoji + "Version: " + hostVersion);
        log.fine(clipboardEmoji + "Chain specification: " + chainService.getChain().getValue());
        log.fine(labelEmoji + "Host name: " + hostName);
//        log.fine(authEmoji + "Role: " + role);
//        log.fine("Local node identity is: " + hostIdentity);
        log.fine("Operating System: " + System.getProperty("os.name"));
    }
}
