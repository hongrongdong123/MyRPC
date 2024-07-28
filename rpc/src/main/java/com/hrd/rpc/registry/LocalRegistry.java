package com.hrd.rpc.registry;

import com.hrd.rpc.model.ServiceMetaInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description:本地注册中心
 */
public class LocalRegistry {
    /**
     * 储存注册的服务信息
     * key服务名称
     * value真正提供服务的类
     */
    private static final Map<String, Class<?>> serviceImplmap = new ConcurrentHashMap<>();


    /**
     * 注册服务
     * @param serviceName
     * @param implClass
     */
    public static  void registry(String serviceName, Class<?> implClass, ServiceMetaInfo serviceMetaInfo) {
        serviceImplmap.put(serviceName, implClass);
    }

    /**
     * 获取服务实例
     * @param serviceName
     * @return
     */
    public static Class<?> getServiceImpl(String serviceName) {
        return serviceImplmap.get(serviceName);
    }


    /**
     * 删除服务
     * @param serviceName
     */
    public static void remove(String serviceName) {
        serviceImplmap.remove(serviceName);
    }
}
