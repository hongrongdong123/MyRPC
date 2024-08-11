package com.hrd.rpc.fault.tolerant;


import com.hrd.rpc.loadbalancer.LoadBalancer;
import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.model.ServiceMetaInfo;
import com.hrd.rpc.transport.protocol.ProtocolMessage;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * description:并行调用
 * 并行调用多个服务提供者，只要有一个成功即返回。
 */
public class Forking implements TolerantStrategy {


    @Override
    public RpcResponse doTolerant(List<ServiceMetaInfo> serviceMetaInfoList, ProtocolMessage<RpcRequest> requestMessage, LoadBalancer loadBalancer, String clientIp, Callable<RpcResponse> callable) {
        return null;
    }
}
