package com.hrd.rpc.loadbalancer;

import com.hrd.rpc.model.ServerModel;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * description:简单随机的负载均衡器
 */
public class RandomLoadbalancer implements LoadBalancer{

    @Override
    public ServerModel select(String clientIP, List<ServerModel> serverModelList) {
        // 随机数范围[0,serverListSize)
        int index = ThreadLocalRandom.current().nextInt(serverModelList.size());
        System.out.println(serverModelList.get(index).getServiceAddress());
        return serverModelList.get(index);
    }
}
