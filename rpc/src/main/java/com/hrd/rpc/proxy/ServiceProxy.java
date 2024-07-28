package com.hrd.rpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.serializer.JdkSerializer;
import com.hrd.rpc.serializer.Serializer;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


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
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .parameters(args)
                .build();

        try {
            //序列化
            byte[] requestSerialized = serializer.serialize(rpcRequest);
            //发送请求(此处的发送请求的url写死了，应该使用注册中心和服务发现解决)
            HttpResponse httpResponse = HttpRequest.post("http://localhost:9999")
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
