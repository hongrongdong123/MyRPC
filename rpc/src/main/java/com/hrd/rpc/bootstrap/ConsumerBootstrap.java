package com.hrd.rpc.bootstrap;

import com.hrd.rpc.RpcApplication;
import com.hrd.rpc.config.RpcConfig;

/**
 * description:服务调用者启动类
 */
public class ConsumerBootstrap {
    public static void init() {
        //框架初始化
        RpcApplication.init();

    }
}
