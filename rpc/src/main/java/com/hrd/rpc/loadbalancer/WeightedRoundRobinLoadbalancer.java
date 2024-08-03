package com.hrd.rpc.loadbalancer;

import cn.hutool.core.collection.CollectionUtil;
import com.hrd.rpc.model.ServerModel;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description:加权轮询（平滑加权轮询）的负载均衡器
 */
public class WeightedRoundRobinLoadbalancer implements LoadBalancer{

    private AtomicInteger atomicIndex = new AtomicInteger(0);

    private List<ServerModel> serverModels; // 保存服务列表(每次会更新动态权重)

    @Override
    public ServerModel select(String clientIP, List<ServerModel> serverModelList) {

        if (CollectionUtil.isEmpty(serverModels)) {
            serverModels = serverModelList;
        }

        int totalWeight = 0;
        int firstWeight = serverModels.get(0).getServerWeight();
        boolean sameWeight = true;
        for (ServerModel server : serverModels) {
            totalWeight += server.getServerWeight();
            if (sameWeight && server.getServerWeight() != firstWeight) {
                sameWeight = false;
            }
            // 设置动态权重 -> currentWeight += weight
            server.setCurrentWeight(server.getCurrentWeight() + server.getServerWeight());
        }
        if (!sameWeight) {
            // 最大动态权重服务器 -> max(currentWeight)
            ServerModel maxCurrentWeightServer = serverModels.stream().max((s1, s2) -> s1.getCurrentWeight() - s2.getCurrentWeight()).get();
            // 设置最大动态权重 -> max(currentWeight) - totalWeight
            maxCurrentWeightServer.setCurrentWeight(maxCurrentWeightServer.getCurrentWeight() - totalWeight);
            // 返回最大动态权重服务器
            return maxCurrentWeightServer;
        }
        // 权重相同依次轮询
        int index = atomicIndex.getAndIncrement() % serverModels.size();
        return serverModels.get(index);
    }
}
