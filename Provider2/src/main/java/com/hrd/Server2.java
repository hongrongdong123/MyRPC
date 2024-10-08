package com.hrd;

import com.hrd.rpc.RpcApplication;
import com.hrd.rpc.bootstrap.ProviderBootstrap;
import com.hrd.rpc.config.RpcConfig;
import com.hrd.rpc.model.ServiceRegisterInfo;
import com.hrd.rpc.server.HttpServer;
import com.hrd.rpc.transport.netty.NettyServer;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 */
public class Server2 {
    public static void main(String[] args) {
        //声明要暴露的服务
        List<ServiceRegisterInfo<?>> serviceList = new ArrayList<>();
        ServiceRegisterInfo<HelloService> service = new ServiceRegisterInfo<>(
                HelloService.class.getName(), HelloServiceImpl.class);
        serviceList.add(service);
        //调用rpc框架，将服务提供出去
        ProviderBootstrap.provide(serviceList);
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        String serverHost = rpcConfig.getServerHost();
        Integer serverPort = rpcConfig.getServerPort();

        //创建tomcat服务器
        //new HttpServer().start(serverHost, serverPort);
        NettyServer.startServer(serverHost, serverPort);

    }
}
