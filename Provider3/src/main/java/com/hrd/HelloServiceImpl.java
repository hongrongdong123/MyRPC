package com.hrd;

/**
 * description:
 */
public class HelloServiceImpl implements HelloService{
    @Override
    public String hello(String str) {
        return "hello" + str;
    }
}
