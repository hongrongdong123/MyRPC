package com.hrd.rpc.fault.tolerant;

import com.hrd.rpc.convert.ServiceMetaInfoToServerModel;
import com.hrd.rpc.loadbalancer.LoadBalancer;
import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.model.ServerModel;
import com.hrd.rpc.model.ServiceMetaInfo;
import com.hrd.rpc.transport.netty.NettyClient;
import com.hrd.rpc.transport.protocol.ProtocolMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.PublicKey;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * description:失败自动切换
 * 在调用失败时，自动切换到其他可用的服务提供者节点并重试。
 * (这里没有切换服务提供者节点，只是简单重试)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Failover implements TolerantStrategy {

    private int maxRetries = 3; // 最大重试次数
    private int initialInterval = 1000; // 初始重试间隔
    private double backoffMultiplier = 2; // 退避系数
    private double jitterFactor = 0.2; // 抖动因子
    private List<ServerModel> serverModelList;//存放服务提供者节点，用于失败时切换节点
    private ServerModel serviceServer;//每一次调用的节点
    private LoadBalancer loadBalancer;//负载均衡器，用于选择服务节点


    public RpcResponse doRetry(ProtocolMessage<RpcRequest> requestMessage, String clientIp) {

        int tryTimes = maxRetries;
        int interval = initialInterval;
        // 计算抖动范围
        double jitter = 1 + (new Random().nextDouble() * 2 - 1) * jitterFactor;

        while (true) {
            try {
                //执行调用
                return realInvoke(serviceServer, requestMessage);
            } catch (Exception e) {

                //重试次数减一
                tryTimes--;
                //更新服务提供节点列表
                serverModelList.remove(serviceServer);
                if (serverModelList.size() == 0) {
                    log.error("服务重试时没有可用节点");
                    throw new RuntimeException("服务重试时没有可用节点");

                }
                //选择下一次服务提供节点
                updateServiceServer(clientIp);
                try {
                    //重试间隔
                    Thread.sleep(interval);
                    interval = (int) (interval * 2 * jitter);
                } catch (InterruptedException ex) {
                    log.error("在进行重试时发生异常.", ex);
                }
                if (tryTimes <= 0) {
                    log.error("对方法进行远程调用时，重试{}次，依然不可调用", maxRetries, e);
                    break;
                }
            }
        }
        throw new RuntimeException("服务调用失败");
    }

    public RpcResponse doTolerant(List<ServiceMetaInfo> serviceMetaInfoList,
                              ProtocolMessage<RpcRequest> requestMessage,
                              LoadBalancer loadBalancer, String clientIp, Callable<RpcResponse> callable) {

        this.serverModelList = ServiceMetaInfoToServerModel.convert(serviceMetaInfoList);
        this.loadBalancer = loadBalancer;
        this.serviceServer = loadBalancer.select(clientIp, serverModelList);

        return doRetry(requestMessage, clientIp);
    }

    public RpcResponse realInvoke(ServerModel serviceServer, ProtocolMessage<RpcRequest> requestMessage) {
        return NettyClient.initAndSend(serviceServer.getServiceHost(),
                serviceServer.getServicePort(), requestMessage);

    }

    public void updateServiceServer(String client) {
        serviceServer = loadBalancer.select(client, serverModelList);
    }

}
