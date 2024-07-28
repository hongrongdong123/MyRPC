package com.hrd;

import com.hrd.rpc.registry.LocalRegistry;
import com.hrd.rpc.server.HttpServer;

/**
 * description:
 */
public class Server {
    public static void main(String[] args) {
        //注册服务
        LocalRegistry.registry(HelloService.class.getName(), HelloServiceImpl.class);
        //创建tomcat服务器
        new HttpServer().start("localhost", 9999);

    }
}
