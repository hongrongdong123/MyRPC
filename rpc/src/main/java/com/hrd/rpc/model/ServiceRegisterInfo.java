package com.hrd.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description:
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ServiceRegisterInfo<T> {
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * 服务实现类
     */
    private Class<? extends T> serviceImpl;

}
