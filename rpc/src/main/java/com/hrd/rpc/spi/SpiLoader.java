package com.hrd.rpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.hrd.rpc.config.RegistryConfig;
import com.hrd.rpc.config.RpcConfig;
import com.hrd.rpc.registry.Registry;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description:Spi加载器
 */
@Slf4j
public class SpiLoader {

    /**
     * 存放接口（接口名称 -> （接口实现的名称 -> 实现类））
     * edg: (Registry -> (ZooKeeper -> com.hrd.rpc.registry.ZooKeeperRegistry) )
     *      (Serializer -> (JDKSerializer -> com.hrd.rpc.serializer.JDKSerializer)
     */
    private static final Map<String, Map<String, Class<?>>> interfaceMap = new ConcurrentHashMap<>();

    /**
     * 存放接口实现 （接口实现的名称 -> 实现类）实例缓存
     * edg: (ZooKeeper -> com.hrd.rpc.registry.ZooKeeperRegistry)
     */
    private static final Map<String, Object> instanceMap = new ConcurrentHashMap<>();

    /**
     * 系统 SPI 目录
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    /**
     * 用户自定义 SPI 目录
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[]{RPC_SYSTEM_SPI_DIR, RPC_CUSTOM_SPI_DIR};


    /**
     * 加载某个接口实现
     * @param loadClass
     * @return
     */
    public static Map<String, Class<?>> load(Class<?> loadClass) {
        log.info("加载的类型是 {} SPI", loadClass.getName());
        Map<String, Class<?>> map = new HashMap<>();
        //
        for (String scanDir : SCAN_DIRS) {
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            try {
                for (URL resource : resources) {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] strArray = line.split("=");
                        if (strArray.length > 1) {
                            String key = strArray[0];
                            String className = strArray[1];
                            map.put(key, Class.forName(className));
                        }
                    }

                }
            } catch (Exception e) {
                log.error("spi resource load error", e);
            }
        }
        interfaceMap.put(loadClass.getName(), map);
        return map;

    }

    /**
     *获取某个接口的实例
     * @param key
     * @param tClass
     * @return
     * @param <T>
     */
    public static <T> T getInstance(String key, Class<?> tClass) {
        String tClassName = tClass.getName();
        Map<String, Class<?>> classMap = interfaceMap.get(tClassName);
        if (classMap == null) {
            throw new RuntimeException(String.format("SpiLoader 未加载 %s 类型", tClassName));
        }
        if (!classMap.containsKey(key)) {
            throw new RuntimeException(String.format("SpiLoader 的 %s 不存在 key=%s 的类型", tClassName, key));
        }
        //获取到要加载的实现类型
        Class<?> implClass = classMap.get(key);
        //从实例缓存中加载指定类型的实例
        String implClassName = implClass.getName();
        if (!instanceMap.containsKey(implClassName)) {
            try {
                instanceMap.put(implClassName, implClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                String errorMsg = String.format("%s 类实例化失败", implClassName);
                throw new RuntimeException(errorMsg, e);
            }
        }
        return (T) instanceMap.get(implClassName);
    }
}
