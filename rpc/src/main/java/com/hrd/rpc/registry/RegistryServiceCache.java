package com.hrd.rpc.registry;

import com.hrd.rpc.model.ServiceMetaInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description:服务本地缓存
 */
public class RegistryServiceCache {


    /**
     * 缓存
     * key 服务名称+版本
     * value 服务提供节点信息列表
     * 服务端可以将一个服务部署在多台服务器上，那么相同的服务，但是服务的域名端口不同
     */
    Map<String, List<ServiceMetaInfo>> serviceCache = new ConcurrentHashMap<>();


    /**
     * 读缓存，获取服务列表
     * @return
     */
      List<ServiceMetaInfo> getServiceCache(String serviceKey) {
        return this.serviceCache.get(serviceKey);
    }

    /**
     * 写缓存
     * @param serviceMetaInfoList
     */
    void writeServiceCache(String serviceKey, List<ServiceMetaInfo> serviceMetaInfoList) {
          this.serviceCache.put(serviceKey, serviceMetaInfoList);
    }


    /**
     * 判断本地是否存在所需服务缓存信息
     * @param serviceKey
     * @return
     */
    boolean hasServiceKey(String serviceKey) {
        return this.serviceCache.containsKey(serviceKey);
    }



}
