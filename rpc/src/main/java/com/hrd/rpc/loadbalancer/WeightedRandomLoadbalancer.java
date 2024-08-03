package com.hrd.rpc.loadbalancer;

import com.hrd.rpc.model.ServerModel;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * description:加权随机的负载均衡器
 */
public class WeightedRandomLoadbalancer implements LoadBalancer{
    @Override
    public ServerModel select(String clientIP, List<ServerModel> serverModelList) {
        // 所有服务器总权重
        int totalWeight = 0;
        // 第一个服务器权重
        int firstWeight = serverModelList.get(0).getServerWeight();
        // 所有服务器权重相等
        boolean sameWeight = true;
        // 遍历所有服务器
        for (ServerModel server : serverModelList) {
            // 计算总权重
            totalWeight += server.getServerWeight();
            // 任意一个invoker权重不等于第一个权重则设置sameWeight=false
            if (sameWeight && server.getServerWeight() != firstWeight) {
                sameWeight = false;
            }
        }
        // 权重不相等则根据权重选择
        if (!sameWeight) {
            // 在总区间范围[0,totalWeight)生成随机数A
            Integer offset = ThreadLocalRandom.current().nextInt(totalWeight);
            // 遍历所有服务器区间
            for (ServerModel server : serverModelList) {
                // 如果A在server区间直接返回
                if (offset <= server.getServerWeight()) {
                    return server;
                }
                // 如果A不在server区间则减去此区间范围并继续匹配其它区间
                offset -= server.getServerWeight();
            }
        }
        // 所有服务器权重相等则随机选择
        return serverModelList.get(ThreadLocalRandom.current().nextInt(serverModelList.size()));
    }
}
