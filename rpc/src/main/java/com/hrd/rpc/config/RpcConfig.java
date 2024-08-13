package com.hrd.rpc.config;

import com.hrd.rpc.fault.retry.RetryStrategyKeys;
import com.hrd.rpc.fault.tolerant.TolerantStrategyKeys;
import com.hrd.rpc.loadbalancer.LoadbalanceKeys;
import com.hrd.rpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * description:
 */
@Data
public class RpcConfig {

    /**
     * 名称
     */
    private String name = "my-rpc";

    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;

    /**
     * 服务器权重，用于加权的负载均衡策略
     */
    private Integer serverWeight = 0;

    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.JDK;

    /**
     * 负载均衡器
     */
    private String loadBalance = LoadbalanceKeys.RANDOM;

    /**
     * 重试策略
     */
    private String retryStrategy = RetryStrategyKeys.FIXED_INTERVAL;

    /**
     * 容错策略
     */
    private String tolerantStrategy = TolerantStrategyKeys.FAIL_FAST;
    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();
}
