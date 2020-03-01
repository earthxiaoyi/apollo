package cn.com.apollo.rpc.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class ServiceProviders {

    private final static Logger log = LoggerFactory.getLogger(ServiceProviders.class);

    /**
     * 所有暴露的本地服务，存储格式为：
     * key：interfaceName+"."+group+"."+version
     * value：service
     */
    private static final ConcurrentHashMap<String, ServiceModel> EXPORT_MAP = new ConcurrentHashMap<>();

    public static void setServiceModel(String serviceKey, ServiceModel model) {
        if (EXPORT_MAP.putIfAbsent(serviceKey, model) != null) {
            log.warn("already same service " + serviceKey);
        }
    }

    public static ServiceModel getServiceModel(String serviceKey) {
        if (serviceKey == null || serviceKey.length() == 0) {
            throw new IllegalArgumentException("serviceKey is not null");
        }
        return EXPORT_MAP.get(serviceKey);
    }

    public static ConcurrentHashMap<String, ServiceModel> getExportMap() {
        return EXPORT_MAP;
    }


}
