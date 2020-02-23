package cn.com.apollo.config;

import cn.com.apollo.common.Constant;
import cn.com.apollo.common.URI;
import cn.com.apollo.common.spi.ServiceLoad;
import cn.com.apollo.common.util.IPHelper;
import cn.com.apollo.export.Export;

import java.util.List;

public class ServiceConfig<T> {

    private List<NameServiceConfig> nameServiceConfigList;
    private ApplicationConfig applicationConfig;

    private Export export = ServiceLoad.getServiceLoad(Export.class).getDefaultService();

    private String id;
    private String interfaceName;
    private Class<?> interfaceClass;
    private volatile T ref;
    private int timeout;
    private int port;
    private int groupName;
    private String version;
    private int weight;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInterface() {
        return interfaceName;
    }

    public void setInterface(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public Object getRef() {
        return ref;
    }


    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getGroupName() {
        return groupName;
    }

    public void setGroupName(int groupName) {
        this.groupName = groupName;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setRef(T ref) {
        this.ref = ref;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public synchronized Class<?> getInterfaceClass() {
        if (interfaceClass == null) {
            try {
                Class<?> aClass = Class.forName(interfaceName);
                return aClass;
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
        return interfaceClass;
    }

    public synchronized void export() {
        checkConfig();
        NameServiceConfig nameServiceConfig = nameServiceConfigList.get(0);
        ConfigManager.getInstance().addNameService(nameServiceConfig);
        URI uri = new URI();
        uri.setPort(port);
        uri.setHost(IPHelper.getHostFirstIp());
        uri.setServiceName(interfaceName);
        uri.setProtocol(Constant.PROTOCOL);
        //TODO 组装service标签参数
        export.export(ref, interfaceClass, nameServiceConfig, uri);
    }

    /**
     * 检查服务发布所需要的配置
     */
    protected void checkConfig() {
        if (getPort() <= 0 || getPort() > 65535) {
            //检查register
            if (getNameServiceConfigList() == null || getNameServiceConfigList().isEmpty()) {
                throw new IllegalArgumentException("nameService is not null");
            }
        }
        //检查interface 是否为null
        String anInterface = getInterface();
        if (anInterface == null || anInterface.length() == 0) {
            throw new IllegalArgumentException("interface is not null");
        }
        if (interfaceClass == null) {
            try {
                interfaceClass = Class.forName(interfaceName);
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
        //检查本地bean
        if (getRef() == null) {
            throw new IllegalArgumentException("ref is not null");
        }
        //检查ref是否为interface的子类
        if (!interfaceClass.isInstance(ref)) {
            throw new IllegalArgumentException("ref：" + ref + " is not subClass of " + getInterfaceClass());
        }
        //检查application
        if (getApplicationConfig() == null) {
            throw new IllegalArgumentException("application is not null");
        }
    }

    public List<NameServiceConfig> getNameServiceConfigList() {
        return nameServiceConfigList;
    }

    public void setNameServiceConfigList(List<NameServiceConfig> nameServiceConfigList) {
        this.nameServiceConfigList = nameServiceConfigList;
    }

    public ApplicationConfig getApplicationConfig() {
        return applicationConfig;
    }

    public void setApplicationConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }
}
