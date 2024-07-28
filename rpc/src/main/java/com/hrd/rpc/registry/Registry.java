package com.hrd.rpc.registry;


import com.hrd.rpc.config.RegistryConfig;
import com.hrd.rpc.model.ServiceMetaInfo;

import java.util.List;

public interface Registry {


    /**
     * 初始化
     * @param registryConfig
     */
    void init(RegistryConfig registryConfig);

    /**
     * 注册服务
     */
    void register(ServiceMetaInfo serviceMetaInfo);

    /**
     * 服务发现
     * @param serviceKey 服务名称+服务版本号
     * @return
     */
    List<ServiceMetaInfo> serviceDiscover(String serviceKey);


}
