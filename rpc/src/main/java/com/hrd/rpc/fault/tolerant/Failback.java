package com.hrd.rpc.fault.tolerant;


import com.hrd.rpc.loadbalancer.LoadBalancer;
import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.model.ServiceMetaInfo;
import com.hrd.rpc.transport.protocol.ProtocolMessage;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * description:失败自动恢复
 * 调用失败后，定时重发请求，通常用于消息通知类操作
 */
public class Failback implements TolerantStrategy {


    @Override
    public RpcResponse doTolerant(List<ServiceMetaInfo> serviceMetaInfoList, ProtocolMessage<RpcRequest> requestMessage, LoadBalancer loadBalancer, String clientIp, Callable<RpcResponse> callable) {
        return null;
    }
}
