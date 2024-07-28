package com.hrd.rpc.proxy;

import java.lang.reflect.Proxy;

/**
 * description:服务代理工厂（工厂模式，用于创建代理类）
 */
public class ServiceProxyFactory {

    /**
     * 根据服务类获取d代理对象
     * @param serviceClass
     * @return
     * @param <T>
     */
    public static <T> T getProxy(Class<T> serviceClass) {
        try {
            return (T) Proxy.newProxyInstance(
                    serviceClass.getClassLoader(),
                    new Class[]{serviceClass},
                    new ServiceProxy());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
