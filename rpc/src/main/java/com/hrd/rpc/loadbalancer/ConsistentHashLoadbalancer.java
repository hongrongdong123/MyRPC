package com.hrd.rpc.loadbalancer;

import cn.hutool.core.collection.CollectionUtil;
import com.hrd.rpc.model.ServerModel;

import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * description:一致性哈希的负载均衡器
 */
public class ConsistentHashLoadbalancer implements LoadBalancer{
    private ConsistentHashSelector consistentHashSelector;

    @Override
    public ServerModel select(String clientIP, List<ServerModel> serverModelList) {
        initialSelector(serverModelList);
        return consistentHashSelector.select(clientIP);
    }

    private class ConsistentHashSelector {
        //用于记录服务提供者列表的哈希值。这个哈希值用于判断服务提供者列表是否发生了变化。
        private Integer identityHashCode;
        //虚拟节点数量
        private Integer VIRTUAL_NODES_NUM = 16;
        //一致性哈希环
        private TreeMap<Integer /* hashcode */, ServerModel> serverNodes = new TreeMap<Integer, ServerModel>();

        //构建哈希环
        public ConsistentHashSelector(Integer identityHashCode, List<ServerModel> serverList) {
            this.identityHashCode = identityHashCode;
            TreeMap<Integer, ServerModel> newServerNodes = new TreeMap<Integer, ServerModel>();
            for (ServerModel server : serverList) {
                // 虚拟节点
                for (int i = 0; i < VIRTUAL_NODES_NUM; i++) {
                    int virtualKey = hashCode(server.getIP() + "_" + i);
                    newServerNodes.put(virtualKey, server);
                }
            }
            serverNodes = newServerNodes;
        }

        //根据客户端ip路由
        public ServerModel select(String clientIP) {
            //计算客户端哈希值
            int clientHashCode = hashCode(clientIP);
            // 找到第一个大于客户端哈希值的服务器
            SortedMap<Integer, ServerModel> tailMap = serverNodes.tailMap(clientHashCode, false);
            if (CollectionUtil.isEmpty(tailMap)) {
                Integer firstKey = serverNodes.firstKey();
                return serverNodes.get(firstKey);
            }
            // 找不到表示在最后一个节点和第一个节点之间 ->选择第一个节点
            Integer firstKey = tailMap.firstKey();
            return tailMap.get(firstKey);
        }

        //计算哈希值
        private int hashCode(String key) {
            return Objects.hashCode(key);
        }

        // 提供者列表哈希值 -> 如果新增或者删除提供者会发生变化
        public Integer getIdentityHashCode() {
            return identityHashCode;
        }
    }

    private void initialSelector(List<ServerModel> serverList) {
        // 计算提供者列表哈希值
        Integer newIdentityHashCode = System.identityHashCode(serverList);
        // 提供者列表哈希值没有变化则无需重新构建哈希环
        if (null != consistentHashSelector && (null != consistentHashSelector.getIdentityHashCode() && newIdentityHashCode == consistentHashSelector.getIdentityHashCode())) {
            return;
        }
        // 提供者列表哈希值发生变化则重新构建哈希环
        consistentHashSelector = new ConsistentHashSelector(newIdentityHashCode, serverList);
    }

}
