package com.limechain.rpc;

import com.limechain.config.HostConfig;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple load balancer that switches between provided endpoints. Each consecutive call takes the latter endpoint
 * in the provided list.
 */
public class LoadBalancer {

    private final List<String> endpoints;
    private final AtomicInteger index;

    public LoadBalancer(HostConfig hostConfig) {
        this.endpoints = hostConfig.getHttpsRpcEndpoints();
        this.index = new AtomicInteger(0);
    }

    public String getNextEndpoint() {
        int currentIndex = index.getAndUpdate(i -> (i + 1) % endpoints.size());
        return endpoints.get(currentIndex);
    }
}
