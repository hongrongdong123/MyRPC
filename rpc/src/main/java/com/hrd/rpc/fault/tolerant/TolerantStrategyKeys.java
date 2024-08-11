package com.hrd.rpc.fault.tolerant;

public interface TolerantStrategyKeys {
    /**
     * 故障恢复
     */
    String FAIL_BACK = "failback";

    /**
     * 快速失败
     */
    String FAIL_FAST = "failfast";

    /**
     * 故障转移
     */
    String FAIL_OVER = "failover";

    /**
     * 安全失败
     */
    String FAIL_SAFE = "failsafe";

    /**
     * 广播调用
     */
    String BROADCAST = "broadcast";

    /**
     * 并行调用
     */
    String FORKING = "forking";
}
