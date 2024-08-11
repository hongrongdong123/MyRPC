package com.hrd.rpc.fault.tolerant;


import com.hrd.rpc.loadbalancer.LoadBalancer;
import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.model.ServiceMetaInfo;
import com.hrd.rpc.transport.protocol.ProtocolMessage;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * description:安全失败
 * 失败时直接忽略，返回一个默认的空结果（如 null）。
 */
public class Failsafe implements TolerantStrategy {


    @Override
    public RpcResponse doTolerant(List<ServiceMetaInfo> serviceMetaInfoList, ProtocolMessage<RpcRequest> requestMessage, LoadBalancer loadBalancer, String clientIp, Callable<RpcResponse> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            return new RpcResponse("调用时出错,执行安全失败策略", null,"调用时出错,执行安全失败策略", null);
        }
    }
}
