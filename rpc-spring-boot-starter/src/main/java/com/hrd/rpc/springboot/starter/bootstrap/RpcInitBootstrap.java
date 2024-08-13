package com.hrd.rpc.springboot.starter.bootstrap;

import com.hrd.rpc.RpcApplication;
import com.hrd.rpc.config.RpcConfig;
import com.hrd.rpc.springboot.starter.annotation.EnableRpc;
import com.hrd.rpc.transport.netty.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * description:
 */
@Slf4j
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {

    /**
     * Spring 初始化时执行，初始化 RPC 框架
     * @param importingClassMetadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取 EnableRpc 注解的属性值
        boolean needServer = (boolean)importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName())
                .get("needServer");

        //初始化Rpc框架（配置和注册中心）
        RpcApplication.init();

        //全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // 异步启动服务器
        if (needServer) {
            new Thread(() -> {
                try {
                    NettyServer.startServer(rpcConfig.getServerHost(), rpcConfig.getServerPort());
                } catch (Exception e) {
                    log.error("Netty 服务器启动失败", e);
                }
            }).start();
        } else {
            log.info("不启动 server");
        }

    }
}
