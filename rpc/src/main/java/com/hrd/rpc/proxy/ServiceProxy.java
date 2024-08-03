package com.hrd.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.hrd.rpc.RpcApplication;
import com.hrd.rpc.config.RpcConfig;
import com.hrd.rpc.constant.RpcConstant;
import com.hrd.rpc.convert.ServiceMetaInfoToServerModel;
import com.hrd.rpc.loadbalancer.LoadBalancer;
import com.hrd.rpc.loadbalancer.LoadbalancerFactory;
import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.model.ServerModel;
import com.hrd.rpc.model.ServiceMetaInfo;
import com.hrd.rpc.registry.LocalRegistry;
import com.hrd.rpc.registry.Registry;
import com.hrd.rpc.registry.RegistryFactory;
import com.hrd.rpc.serializer.JdkSerializer;
import com.hrd.rpc.serializer.JsonSerializer;
import com.hrd.rpc.serializer.Serializer;
import com.hrd.rpc.serializer.SerializerFactory;

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
            //序列化
            byte[] requestSerialized = serializer.serialize(rpcRequest);
            //发送请求

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
            //负载均衡（暂未实现）
            //获取消费方的ip，用于一致性哈希负载均衡时路由对应的服务端服务器
            InetAddress localHost = InetAddress.getLocalHost();
            String clientIp = localHost.getHostAddress();
            //
            LoadBalancer loadbalancer = LoadbalancerFactory.getLoadbalancer(rpcConfig.getLoadBalance());
            ServerModel serviceServer = loadbalancer.select(clientIp, ServiceMetaInfoToServerModel.convert(serviceMetaInfoList));
            //ServiceMetaInfo service = LoadbalanceFactory.getLoadblance.select(String clientIp, List<ServiceMetaInfo>);
            String post = serviceServer.getServiceAddress();
            // 服务的host、port
            HttpResponse httpResponse = HttpRequest.post(post)
                    .body(requestSerialized)
                    .execute();
            System.out.println(post);
            //反序列化
            byte[] bytes = httpResponse.bodyBytes();
            RpcResponse rpcResponse = serializer.deserialize(bytes, RpcResponse.class);
            return rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
