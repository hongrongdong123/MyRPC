package com.hrd.rpc.loadbalancer;

import com.hrd.rpc.model.ServerModel;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description:简单轮询的负载均衡器
 */
public class RoundRobinLoadbalancer implements LoadBalancer{

    private AtomicInteger atomicIndex = new AtomicInteger(0);

    @Override
    public ServerModel select(String clientIP, List<ServerModel> serverModelList) {
        // atomicIndex自增大于服务器数量时取余
        int index = atomicIndex.getAndIncrement() % serverModelList.size();
        return serverModelList.get(index);
    }
}
