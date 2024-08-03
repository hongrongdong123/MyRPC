package com.hrd.rpc.model;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description:提供服务的服务器
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerModel {

    /**
     * 服务域名
     */
    private String serviceHost;

    /**
     * 服务端口号
     */
    private Integer servicePort;

    /**
     * 权重（用于负载均衡）
     */
    private Integer serverWeight = 0;

    /**
     * 动态权重（平滑加权轮询中用到）
     */
    private int currentWeight = 0;


    public String getIP() {
        return serviceHost + ":" + servicePort;
    }

    /**
     * 获取完整服务地址
     *
     * @return
     */
    public String getServiceAddress() {
        if (!StrUtil.contains(serviceHost, "http")) {
            return String.format("http://%s:%s", serviceHost, servicePort);
        }
        return String.format("%s:%s", serviceHost, servicePort);
    }
}
