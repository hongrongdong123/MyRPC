package com.hrd.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.hrd.rpc.RpcApplication;
import com.hrd.rpc.config.RpcConfig;
import com.hrd.rpc.constant.RpcConstant;
import com.hrd.rpc.convert.ServiceMetaInfoToServerModel;
import com.hrd.rpc.fault.retry.RetryStrategy;
import com.hrd.rpc.fault.retry.RetryStrategyFactory;
import com.hrd.rpc.fault.tolerant.TolerantStrategy;
import com.hrd.rpc.fault.tolerant.TolerantStrategyFactory;
import com.hrd.rpc.loadbalancer.LoadBalancer;
import com.hrd.rpc.loadbalancer.LoadbalancerFactory;
import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.model.ServerModel;
import com.hrd.rpc.model.ServiceMetaInfo;
import com.hrd.rpc.registry.Registry;
import com.hrd.rpc.registry.RegistryFactory;
import com.hrd.rpc.serializer.Serializer;
import com.hrd.rpc.serializer.SerializerFactory;
import com.hrd.rpc.transport.netty.NettyClient;
import com.hrd.rpc.transport.protocol.ProtocolConstant;
import com.hrd.rpc.transport.protocol.ProtocolMessage;
import com.hrd.rpc.transport.protocol.ProtocolMessageSerializerEnum;
import com.hrd.rpc.transport.protocol.ProtocolMessageTypeEnum;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.List;


/**
 * description:JDK动态代理
 */
public class ServiceProxy implements InvocationHandler {
    /**
     *
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //指定序列化器
        Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        //构造请求

        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .parameters(args)
                .build();

        try {
            //序列化（其实在netty的编码器中会进行序列化，这里进行序列化的作用时计算消息体的长度）
            byte[] requestSerialized = serializer.serialize(rpcRequest);

            //服务发现
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.getRegistry(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscover(serviceMetaInfo.getServiceKey());
            if (CollUtil.isEmpty(serviceMetaInfoList)) {
                throw new RuntimeException("暂无服务地址");
            }

            //负载均衡
            //获取消费方的ip，用于一致性哈希负载均衡时路由对应的服务端服务器
            InetAddress localHost = InetAddress.getLocalHost();
            String clientIp = localHost.getHostAddress();
            //获取服务方地址
            LoadBalancer loadbalancer = LoadbalancerFactory.getLoadbalancer(rpcConfig.getLoadBalance());
            ServerModel serviceServer = loadbalancer.select(clientIp, ServiceMetaInfoToServerModel.convert(serviceMetaInfoList));
            String post = serviceServer.getServiceAddress();
            //使用netty发送请求
            //构造自定义协议的消息
            ProtocolMessage<RpcRequest> requestMessage = new ProtocolMessage<>();
            ProtocolMessage.Header header = new ProtocolMessage.Header();
            header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
            header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
            header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
            header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
            header.setBodyLength(requestSerialized.length);
            header.setRequestId(IdUtil.getSnowflakeNextId());// 生成全局请求 ID
            requestMessage.setHeader(header);
            requestMessage.setBody(rpcRequest);
            //加入重试机制
            RpcResponse rpcResponse;
            String retryStrategy = RpcApplication.getRpcConfig().getRetryStrategy();
            RetryStrategy retry = RetryStrategyFactory.getInstance(retryStrategy);
            try {
                 rpcResponse = retry.doRetry(() ->
                        NettyClient.initAndSend(serviceServer.getServiceHost(),
                                serviceServer.getServicePort(), requestMessage));
            } catch (Exception e) {
                //容错策略
                String tolerantStrategy = RpcApplication.getRpcConfig().getTolerantStrategy();
                TolerantStrategy tolerant = TolerantStrategyFactory.getInstance(tolerantStrategy);
                rpcResponse = tolerant.doTolerant(serviceMetaInfoList, requestMessage, loadbalancer, clientIp,
                        ()->NettyClient.initAndSend(serviceServer.getServiceHost(),
                                serviceServer.getServicePort(), requestMessage));
            }
            return rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
