package com.hrd.rpc.registry;

import cn.hutool.core.util.StrUtil;
import com.hrd.rpc.config.RegistryConfig;
import com.hrd.rpc.model.ServiceMetaInfo;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description:
 */
public class ZooKeeperRegistry implements Registry{

    private  CuratorFramework client;

    private ServiceDiscovery<ServiceMetaInfo> serviceDiscovery;

    /**
     * 本地服务缓存
     */
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();



    /**
     * 根节点
     */
    private static final String ZK_ROOT_PATH = "/rpc/zk";

    @Override
    public void init(RegistryConfig registryConfig) {

        RetryPolicy retryPolicy = new BoundedExponentialBackoffRetry(1000, 3, 10000);
        client = CuratorFrameworkFactory.builder()
                .connectString(registryConfig.getAddress())
                .retryPolicy(retryPolicy)
                .build();

        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMetaInfo.class)
                .client(client)
                .basePath(ZK_ROOT_PATH)
                .serializer(new JsonInstanceSerializer<>(ServiceMetaInfo.class))
                .build();

        try {
            client.start();
            serviceDiscovery.start();
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 注册服务
     * @param serviceMetaInfo
     */
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) {
        try {
            //注册到zk中
            serviceDiscovery.registerService(buildServiceInstance(serviceMetaInfo));

            //将serviceKey写入本地缓存
            registryServiceCache.serviceKeyList.add(serviceMetaInfo.getServiceKey());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscover(String serviceKey) {
        //如果本地缓存有该服务，优先从本地缓存获取服务
        if(registryServiceCache.hasServiceKey(serviceKey)) {
            List<ServiceMetaInfo> serviceCache = registryServiceCache.getServiceCache();
            return serviceCache;
        }
        //本地缓存没有，从注册中心获取服务
        try {
            //查询服务信息
            Collection<ServiceInstance<ServiceMetaInfo>> serviceInstances = serviceDiscovery.queryForInstances(serviceKey);

            //解析服务信息
            List<ServiceMetaInfo> serviceMetaInfoList = serviceInstances.stream().map(ServiceInstance::getPayload).collect(Collectors.toList());

            //服务写入缓存
            registryServiceCache.writeServiceCache(serviceMetaInfoList);

            return serviceMetaInfoList;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }

    }

    public ServiceInstance<ServiceMetaInfo> buildServiceInstance(ServiceMetaInfo serviceMetaInfo) {
        String serviceAddress = serviceMetaInfo.getServiceHost() + ":" + serviceMetaInfo.getServicePort();
        try {
            ServiceInstanceBuilder<ServiceMetaInfo> serviceInstanceBuilder = ServiceInstance.builder();
            return serviceInstanceBuilder
                    .id(serviceAddress)
                    .name(serviceMetaInfo.getServiceKey())
                    .address(serviceAddress)
                    .port(serviceMetaInfo.getServicePort())
                    .payload(serviceMetaInfo)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
