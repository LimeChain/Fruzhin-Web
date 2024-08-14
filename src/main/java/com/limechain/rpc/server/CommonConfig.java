package com.limechain.rpc.server;

import com.limechain.chain.ChainService;
import com.limechain.config.HostConfig;
import com.limechain.config.SystemInfo;
import com.limechain.network.Network;
import com.limechain.storage.LocalStorage;
import com.limechain.storage.block.SyncState;
import com.limechain.sync.warpsync.WarpSyncMachine;
import com.limechain.sync.warpsync.WarpSyncState;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring configuration class used to instantiate beans.
 */
public class CommonConfig {

    private static Map<Class<?>, Object> beans = new HashMap<>();

    public static void start() {
        getBean(SystemInfo.class);
        getBean(HostConfig.class);
        getBean(LocalStorage.class);
        getBean(WarpSyncMachine.class);
    }

    protected static Object getBean(Class<?> beanClass) {
        if (beans.containsKey(beanClass)) {
            return beans.get(beanClass);
        } else {
            switch (beanClass.getSimpleName()) {
                case "HostConfig":
                    HostConfig hostConfig = hostConfig();
                    beans.put(beanClass, hostConfig);
                    return hostConfig;
                case "KVRepository":
                    LocalStorage repository = repository((HostConfig) getBean(HostConfig.class));
                    beans.put(beanClass, repository);
                    return repository;
                case "ChainService":
                    ChainService chainService = chainService((HostConfig) getBean(HostConfig.class));
                    beans.put(beanClass, chainService);
                    return chainService;
                case "SyncState":
                    SyncState syncState = syncState((LocalStorage) getBean(LocalStorage.class));
                    beans.put(beanClass, syncState);
                    return syncState;
                case "SystemInfo":
                    SystemInfo systemInfo = systemInfo((HostConfig) getBean(HostConfig.class), (SyncState) getBean(SyncState.class));
                    beans.put(beanClass, systemInfo);
                    return systemInfo;
                case "Network":
                    Network network = network((ChainService) getBean(ChainService.class),
                        (HostConfig) getBean(HostConfig.class), (LocalStorage) getBean(LocalStorage.class));
                    beans.put(beanClass, network);
                    return network;
                case "WarpSyncState":
                    WarpSyncState warpSyncState = warpSyncState((Network) getBean(Network.class),
                        (SyncState) getBean(SyncState.class), (LocalStorage) getBean(LocalStorage.class));
                    beans.put(beanClass, warpSyncState);
                    return warpSyncState;
                case "WarpSyncMachine":
                    WarpSyncMachine warpSyncMachine = warpSyncMachine((Network) getBean(Network.class),
                        (ChainService) getBean(ChainService.class), (SyncState) getBean(SyncState.class),
                        (WarpSyncState) getBean(WarpSyncState.class));
                    beans.put(beanClass, warpSyncMachine);
                    return warpSyncMachine;
                default:
                    return null;
            }
        }
    }

    private static HostConfig hostConfig() {
        return new HostConfig();
    }

    private static LocalStorage repository(HostConfig hostConfig) {
        return null;//DBInitializer.initialize(hostConfig.getChain());
    }

    private static ChainService chainService(HostConfig hostConfig) {
        return new ChainService(hostConfig);
    }

    private static SyncState syncState(LocalStorage repository) {
        return new SyncState(repository);
    }

    private static SystemInfo systemInfo(HostConfig hostConfig, SyncState syncState) {
        return new SystemInfo(hostConfig, syncState);
    }

    private static Network network(ChainService chainService, HostConfig hostConfig,
                                   LocalStorage repository) {
        return new Network(chainService, hostConfig, repository);
    }

    private static WarpSyncState warpSyncState(Network network, SyncState syncState,
                                               LocalStorage repository) {
        return new WarpSyncState(syncState, network, repository);
    }

    private static WarpSyncMachine warpSyncMachine(Network network, ChainService chainService, SyncState syncState,
                                                   WarpSyncState warpSyncState) {
        return new WarpSyncMachine(network, chainService, syncState, warpSyncState);
    }

}
