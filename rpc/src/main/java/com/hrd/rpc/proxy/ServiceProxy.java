package com.hrd.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.hrd.rpc.RpcApplication;
import com.hrd.rpc.config.RpcConfig;
import com.hrd.rpc.constant.RpcConstant;
import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.model.ServiceMetaInfo;
import com.hrd.rpc.registry.LocalRegistry;
import com.hrd.rpc.registry.Registry;
import com.hrd.rpc.registry.RegistryFactory;
import com.hrd.rpc.serializer.JdkSerializer;
import com.hrd.rpc.serializer.Serializer;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
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
        Serializer serializer = new JdkSerializer();
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
            //获得服务名称、服务信息id，以便从注册中心获得服务实例serviceInstance

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
            String post = serviceMetaInfoList.get(0).getServiceAddress();
            // 服务的host、port
            HttpResponse httpResponse = HttpRequest.post(post)
                    .body(requestSerialized)
                    .execute();

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
