package com.hrd.rpc.fault.tolerant;

import com.hrd.rpc.spi.SpiLoader;

/**
 * description:
 */
public class TolerantStrategyFactory {
    static {

        SpiLoader.load(TolerantStrategy.class);
    }

    /**
     * 默认容错策略
     */
    private static final TolerantStrategy DEFAULT_RETRY_STRATEGY = new Failfast();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static TolerantStrategy getInstance(String key) {
        return SpiLoader.getInstance(key, TolerantStrategy.class);
    }

}
