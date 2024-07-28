package com.hrd.rpc.registry;

import com.hrd.rpc.spi.SpiLoader;

/**
 * description:
 */
public class RegistryFactory {

    /**
     * 默认注册中心
     */
    private static final Registry DEFAULT_REGISTRY = new ZooKeeperRegistry();

    static {
        SpiLoader.load(Registry.class);
    }

    public static Registry getRegistry(String registryName) {
        return SpiLoader.getInstance(registryName, Registry.class);
    }

}
