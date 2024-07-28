package com.hrd;

import com.hrd.rpc.proxy.ServiceProxyFactory;

/**
 * description:
 */
public class Consumer {
    public static void main(String[] args) {
        HelloService helloService = ServiceProxyFactory.getProxy(HelloService.class);
        String result = helloService.hello("word");
        System.out.println(result);
    }
}
