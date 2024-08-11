package com.hrd.rpc.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import com.hrd.rpc.config.RpcConfig;
import com.hrd.rpc.model.ServerModel;

/**
 * description:
 */
public class ConfigUtil {


    /**
     * 加载配置对象
     * @param tClass
     * @param prefix
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix, "");
    }

    /**
     * 加载配置对象，可区分开发环境
     * @param tClass
     * @param prefix
     * @param environment
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
        StringBuilder stringBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            stringBuilder.append("-").append(environment);
        }
        stringBuilder.append(".properties");
        Props props = new Props(stringBuilder.toString());
        return props.toBean(tClass, prefix);
    }






}
