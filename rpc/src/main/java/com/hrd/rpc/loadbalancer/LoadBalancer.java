package com.hrd.rpc.loadbalancer;

import com.hrd.rpc.model.ServerModel;
import com.hrd.rpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * 负载均衡接口
 */
public interface LoadBalancer {

     ServerModel select(String clientIP, List<ServerModel> serverModelList);
}
