package com.limechain.config;

import com.limechain.network.Network;
import com.limechain.storage.block.SyncState;
import com.limechain.sync.warpsync.WarpSyncMachine;
import com.limechain.sync.warpsync.WarpSyncState;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring configuration class used to instantiate beans.
 */
public class CommonConfig {

    private static final Map<Class<?>, Object> beans = new HashMap<>();

    public static void start() {
        getBean(SystemInfo.class);
        getBean(WarpSyncMachine.class);
    }

    @SuppressWarnings("unchecked")
    protected static <T> T getBean(Class<T> beanClass) {
        if (beans.containsKey(beanClass)) {
            return (T) beans.get(beanClass);
        } else {
            Object bean;
            switch (beanClass.getSimpleName()) {
                case "ChainService":
                    bean = new ChainService();
                    break;
                case "SyncState":
                    bean = new SyncState(getBean(ChainService.class));
                    break;
                case "SystemInfo":
                    bean = new SystemInfo(getBean(ChainService.class));
                    break;
                case "Network":
                    bean = new Network(getBean(ChainService.class));
                    break;
                case "WarpSyncState":
                    bean = new WarpSyncState(getBean(SyncState.class), getBean(Network.class));
                    break;
                case "WarpSyncMachine":
                    bean = new WarpSyncMachine(getBean(Network.class),
                            getBean(ChainService.class),
                            getBean(SyncState.class),
                            getBean(WarpSyncState.class));
                    break;
                default:
                    return null;
            }
            beans.put(beanClass, bean);
            return (T) bean;
        }
    }
}
