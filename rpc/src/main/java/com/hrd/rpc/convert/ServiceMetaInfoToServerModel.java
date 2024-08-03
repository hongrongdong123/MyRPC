package com.hrd.rpc.convert;

import com.hrd.rpc.model.ServerModel;
import com.hrd.rpc.model.ServiceMetaInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 */
public class ServiceMetaInfoToServerModel {

    public static ServerModel convert(ServiceMetaInfo serviceMetaInfo) {
        return ServerModel.builder()
                .serviceHost(serviceMetaInfo.getServiceHost())
                .servicePort(serviceMetaInfo.getServicePort())
                .serverWeight(serviceMetaInfo.getServerWeight())
                .build();
    }

    public static List<ServerModel> convert(List<ServiceMetaInfo> serviceMetaInfoList) {
        List<ServerModel> serverModelList = new ArrayList<>();
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            serverModelList.add(convert(serviceMetaInfo));
        }
        return serverModelList;
    }
}
