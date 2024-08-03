package com.hrd.rpc.loadbalancer;

import com.hrd.rpc.spi.SpiLoader;

/**
 * description:
 */
public class LoadbalancerFactory {

    private LoadBalancer LOANBALANCE = new RoundRobinLoadbalancer();

    static {
        SpiLoader.load(LoadBalancer.class);
    }

    public static LoadBalancer getLoadbalancer(String loadbalancer) {
        return SpiLoader.getInstance(loadbalancer, LoadBalancer.class);
    }
}
