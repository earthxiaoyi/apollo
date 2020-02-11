package cn.com.apollo.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManager {

    private static final ConfigManager configManeger = new ConfigManager();

    private final Map<String, NameServiceConfig> nameServices = new ConcurrentHashMap<>();
    private final Map<String, ReferenceConfig> references = new ConcurrentHashMap<>();
    private final Map<String, ServiceConfig> services = new ConcurrentHashMap<>();

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        return configManeger;
    }

    public void addNameService(NameServiceConfig nameServiceConfig) {
        String key = nameServiceConfig.getId();
        if (!nameServices.containsKey(key)) {
            nameServices.put(key, nameServiceConfig);
        }
    }

    public void addNameServices(List<NameServiceConfig> nameServiceConfigs){
        for (NameServiceConfig nameServiceConfig : nameServiceConfigs) {
            nameServices.put(nameServiceConfig.getId(),nameServiceConfig);
        }
    }

    public Map<String, NameServiceConfig> getNameServiceConfig() {
        return nameServices;
    }

    public ReferenceConfig getReferenceConfig(String key){
        return references.get(key);
    }

}
