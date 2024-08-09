package com.hrd;

import com.hrd.rpc.RpcApplication;
import com.hrd.rpc.bootstrap.ConsumerBootstrap;
import com.hrd.rpc.proxy.ServiceProxyFactory;

/**
 * description:
 */
public class Consumer {
    public static void main(String[] args) {

        //框架初始化
        ConsumerBootstrap.init();

        HelloService helloService = ServiceProxyFactory.getProxy(HelloService.class);
        for (int i = 0; i < 1; i++) {
            String result = helloService.hello("洪荣东");
            System.out.println(result);
        }

    }
}
