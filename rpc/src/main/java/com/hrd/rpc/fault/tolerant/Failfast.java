package com.hrd.rpc.fault.tolerant;


import com.hrd.rpc.loadbalancer.LoadBalancer;
import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.model.ServerModel;
import com.hrd.rpc.model.ServiceMetaInfo;
import com.hrd.rpc.transport.protocol.ProtocolMessage;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * description:快速失败
 * 只发起一次调用，失败后立即报错，不进行重试。
 */
public class Failfast implements TolerantStrategy {


    @Override
    public RpcResponse doTolerant(List<ServiceMetaInfo> serviceMetaInfoList, ProtocolMessage<RpcRequest> requestMessage, LoadBalancer loadBalancer, String clientIp, Callable<RpcResponse> callable) {
        try {
            System.out.println("执行failfast容错策略，再发起一次调用");
            return callable.call();
        } catch (Exception e) {
            System.out.println("发生错误，使用Failfast（快速失败）策略，直接抛出异常，不重试");
            throw new RuntimeException(e);
        }
    }
}
