package com.hrd.rpc.bootstrap;

import com.hrd.rpc.RpcApplication;
import com.hrd.rpc.config.RegistryConfig;
import com.hrd.rpc.config.RpcConfig;
import com.hrd.rpc.model.ServiceMetaInfo;
import com.hrd.rpc.model.ServiceRegisterInfo;
import com.hrd.rpc.registry.LocalRegistry;
import com.hrd.rpc.registry.Registry;
import com.hrd.rpc.registry.RegistryFactory;
import com.hrd.rpc.util.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * description:服务提供者启动类（初始化）
 */
@Slf4j
public class ProviderBootstrap {

    public static void provide(List<ServiceRegisterInfo<?>> serviceRegisterInfoList){
        //框架初始化
        RpcApplication.init();

        //全局配置
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        //服务注册
        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {
            String serviceName = serviceRegisterInfo.getServiceName();
            Class<?> serviceImpl = serviceRegisterInfo.getServiceImpl();


            //服务中心注册
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getRegistry(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServerWeight(rpcConfig.getServerWeight());
            serviceMetaInfo.setServiceVersion(rpcConfig.getVersion());
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());

            //本地注册
            LocalRegistry.registry(serviceName, serviceImpl, serviceMetaInfo);

            //服务中心注册
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 服务注册失败", e);
            }

        }
    }
}
