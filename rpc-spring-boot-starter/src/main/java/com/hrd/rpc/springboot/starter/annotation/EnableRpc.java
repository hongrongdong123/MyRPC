package com.hrd.rpc.springboot.starter.annotation;

import com.hrd.rpc.springboot.starter.bootstrap.RpcConsumerBootstrap;
import com.hrd.rpc.springboot.starter.bootstrap.RpcInitBootstrap;
import com.hrd.rpc.springboot.starter.bootstrap.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description:启用rpc注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcConsumerBootstrap.class, RpcInitBootstrap.class, RpcProviderBootstrap.class})
public @interface EnableRpc {
    /**
     * 需要启动 server
     *
     * @return
     */
    boolean needServer() default true;
}
