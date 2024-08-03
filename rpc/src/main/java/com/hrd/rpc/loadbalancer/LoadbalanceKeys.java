package com.hrd.rpc.loadbalancer;

/**
 * 负载均衡器键命常量
 * 简单随机
 * 加权随机
 * 简单轮询
 * 加权轮询
 */
public interface LoadbalanceKeys {

    /**
     * 简单随机
     */
    String RANDOM = "random";

    /**
     * 加权随机
     */
    String WEIGHTED_RANDOM = "weightedRandom";

    /**
     * 简单轮询
     */
    String ROUND_ROBIN = "roundRobin";

    /**
     * 加权轮询
     */
    String WEIGHTED_ROUND_ROBIN = "weightedRoundRobin";

    /**
     * 一致性哈希
     */
    String CONSISTENT_HASH = "consistentHash";

}
