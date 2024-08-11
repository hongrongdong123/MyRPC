package com.hrd.rpc.fault.tolerant;

import com.hrd.rpc.loadbalancer.LoadBalancer;
import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.model.ServiceMetaInfo;
import com.hrd.rpc.transport.protocol.ProtocolMessage;

import java.util.List;
import java.util.concurrent.Callable;

public interface TolerantStrategy {

    RpcResponse doTolerant(List<ServiceMetaInfo> serviceMetaInfoList,
                           ProtocolMessage<RpcRequest> requestMessage,
                           LoadBalancer loadBalancer, String clientIp, Callable<RpcResponse> callable);
}
