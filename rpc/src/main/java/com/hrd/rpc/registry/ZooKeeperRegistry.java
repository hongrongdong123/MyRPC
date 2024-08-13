package com.hrd.rpc.registry;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.StrUtil;
import com.hrd.rpc.config.RegistryConfig;
import com.hrd.rpc.model.ServiceMetaInfo;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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
     * 正在监听的 key 集合
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

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

            // 添加服务节点的watch监听
            addWatch(ZK_ROOT_PATH);
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
            String serviceKey = serviceMetaInfo.getServiceKey();

            if (registryServiceCache.hasServiceKey(serviceKey)) {
                List<ServiceMetaInfo> list = registryServiceCache.serviceCache.get(serviceKey);
                list.add(serviceMetaInfo);
                registryServiceCache.writeServiceCache(serviceKey, list);
            }else {
                List<ServiceMetaInfo> list = new ArrayList<>();
                list.add(serviceMetaInfo);
                registryServiceCache.writeServiceCache(serviceKey, list);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    private void addWatch(String path) {
        PathChildrenCache cache = new PathChildrenCache(client, path, true);

        PathChildrenCacheListener listener = (client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    System.out.println("服务节点新增: " + event.getData().getPath());
                    // 处理新增的服务节点
                    handleServiceNodeChange(event.getData().getPath(), event.getType());
                    break;
                case CHILD_UPDATED:
                    System.out.println("服务节点更新: " + event.getData().getPath());
                    // 处理更新的服务节点
                    handleServiceNodeChange(event.getData().getPath(), event.getType());
                    break;
                case CHILD_REMOVED:
                    System.out.println("服务节点删除: " + event.getData().getPath());
                    // 处理删除的服务节点
                    handleServiceNodeChange(event.getData().getPath(), event.getType());
                    break;
                default:
                    break;
            }
        };

        cache.getListenable().addListener(listener);

        try {
            cache.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start PathChildrenCache", e);
        }
    }

    private void handleServiceNodeChange(String path, PathChildrenCacheEvent.Type eventType) {
        // 从Zookeeper中重新获取服务列表
        String serviceKey = path.substring(path.lastIndexOf("/") + 1);
        List<ServiceMetaInfo> updatedServiceList = serviceDiscover(serviceKey);

        // 更新本地缓存
        registryServiceCache.writeServiceCache(serviceKey, updatedServiceList);

        // 如果需要处理更多的逻辑，可以在这里扩展
    }



    @Override
    public List<ServiceMetaInfo> serviceDiscover(String serviceKey) {

        //如果本地缓存有该服务，优先从本地缓存获取服务
        if(registryServiceCache.hasServiceKey(serviceKey)) {
            List<ServiceMetaInfo> serviceCache = registryServiceCache.getServiceCache(serviceKey);
            return serviceCache;
        }
        //本地缓存没有，从注册中心获取服务
        try {
            //查询服务信息
            Collection<ServiceInstance<ServiceMetaInfo>> serviceInstances = serviceDiscovery.queryForInstances(serviceKey);

            //解析服务信息
            List<ServiceMetaInfo> serviceMetaInfoList = serviceInstances.stream().map(ServiceInstance::getPayload).collect(Collectors.toList());

            //服务写入缓存
            registryServiceCache.writeServiceCache(serviceKey, serviceMetaInfoList);

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
                    .serviceType(ServiceType.DYNAMIC)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
