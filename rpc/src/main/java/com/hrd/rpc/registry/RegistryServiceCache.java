package com.hrd.rpc.registry;

import com.hrd.rpc.model.ServiceMetaInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * description:服务本地缓存
 */
public class RegistryServiceCache {

    /**
     * 缓存列表 （服务完整信息）
     * 服务端可以将一个服务部署在多台服务器上，那么相同的服务，但是服务的域名端口不同
     */
    List<ServiceMetaInfo> serviceMetaInfoList;

    /**
     * 缓存 服务名称+版本
     * 一个serviceKey可以对应多个ServiceMetaInfo
     */
    List<String> serviceKeyList = new ArrayList<>();




    /**
     * 读缓存，获取服务列表
     * @return
     */
      List<ServiceMetaInfo> getServiceCache() {
        return this.serviceMetaInfoList;
    }

    /**
     * 写缓存
     * @param serviceMetaInfoList
     */
    void writeServiceCache(List<ServiceMetaInfo> serviceMetaInfoList) {
          this.serviceMetaInfoList = serviceMetaInfoList;
    }


    boolean hasServiceKey(String serviceKey) {
        return this.serviceKeyList.contains(serviceKey);
    }



}
