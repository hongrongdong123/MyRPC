package com.hrd.rpc.config;

import com.hrd.rpc.registry.RegistryKeys;
import lombok.Data;

/**
 * description:
 */
@Data
public class RegistryConfig {

    /**
     * 注册中心类别
     */
    private String registry = RegistryKeys.ZOOKEEPER;

    /**
     * 注册中心地址
     */
    private String address = "localhost:2181";

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 超时时间（单位毫秒）
     */
    private Long timeout = 10000L;
}
