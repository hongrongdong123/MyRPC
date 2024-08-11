package com.hrd.rpc.fault.tolerant;


import com.hrd.rpc.loadbalancer.LoadBalancer;
import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.model.ServiceMetaInfo;
import com.hrd.rpc.transport.protocol.ProtocolMessage;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * description:广播调用
 * 广播调用所有服务提供者，逐个调用，任意一个失败都会抛出异常。
 */
public class Broadcast implements TolerantStrategy {


    @Override
    public RpcResponse doTolerant(List<ServiceMetaInfo> serviceMetaInfoList, ProtocolMessage<RpcRequest> requestMessage, LoadBalancer loadBalancer, String clientIp, Callable<RpcResponse> callable) {
        return null;
    }
}
