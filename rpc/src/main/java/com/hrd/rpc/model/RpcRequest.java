package com.hrd.rpc.model;

import com.hrd.rpc.serializer.Serializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * description:Rpc请求实体
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     *方法名称
     */
    private String methodName;
    /**
     *参数类型列表
     */
    private Class[] parameterTypes;
    /**
     *参数列表
     */
    private Object[] parameters;
}
