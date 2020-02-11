package cn.com.apollo.config;

import cn.com.apollo.common.Constant;
import cn.com.apollo.common.URI;
import cn.com.apollo.invoke.Invoker;
import cn.com.apollo.invoke.RpcInvoker;
import cn.com.apollo.proxy.jdk.JdkProxyHandler;
import cn.com.apollo.reference.ApolloReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.List;

public class ReferenceConfig {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private List<NameServiceConfig> nameServiceConfigList;
    private ApplicationConfig applicationConfig;

    private String id;
    private String interfaceName;
    private int timeout;
    private String group;
    private Class<?> interfaceClass;
    private String uri;
    private volatile boolean init = false;
    private Object obj;
    ApolloReference refer = new ApolloReference();

    public synchronized Object get() {
        if (init) {
            return obj;
        }
        //检查配置
        checkConfig();
        //加载注册中心
        List<NameServiceConfig> nameServices = getNameServiceConfigList();
        Invoker<?> invoker = null;
        if (nameServices == null || nameServices.isEmpty()) {
            if (getUri() != null) {
                try {
                    // 服务直连模式
                    String uri = getUri();
                    int i = uri.indexOf("\\:");
                    String host = uri.substring(0, i);
                    int port = Integer.parseInt(uri.substring(i + 1));
                    invoker = new RpcInvoker(interfaceClass, new URI(host, port, interfaceName));
                } catch (Exception e) {
                    throw new RuntimeException("uri config error,uri:" + getUri(), e);
                }
            }
        } else {
            NameServiceConfig nameServiceConfig = nameServices.get(0);
            //集群模式
            URI uri = new URI();
            uri.setHost(getHost());
            uri.setPort(Constant.PORT);
            uri.setServiceName(getInterfaceClass().getName());
            uri.putParameter(Constant.IO_DECODER, true);
            uri.setProtocol("apollo");
            invoker = refer.refer(getInterfaceClass(), nameServiceConfig, uri);
        }
        //创建代理类
        JdkProxyHandler proxyHandler = new JdkProxyHandler(getInterfaceClass(), invoker, getInterface());
        obj = proxyHandler.getProxy();
        init = true;
        return obj;
    }

    private void checkConfig() {
        String interfaceName = this.getInterface();
        if (interfaceName == null || interfaceName.length() == 0) {
            throw new RuntimeException("interfaceName is not null");
        }
        if (uri == null) {
            throw new RuntimeException("uri is not null");
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setInterface(String inteface) {
        this.interfaceName = inteface;
    }

    public String getInterface() {
        return interfaceName;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Class<?> getInterfaceClass() {
        if (interfaceClass != null) {
            return interfaceClass;
        }
        try {
            this.interfaceClass = Class.forName(interfaceName);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return this.interfaceClass;
    }

    public List<NameServiceConfig> getNameServiceConfigList() {
        return nameServiceConfigList;
    }

    public void setNameServiceConfigList(List<NameServiceConfig> nameServiceConfigList) {
        ConfigManager.getInstance().addNameServices(nameServiceConfigList);
        this.nameServiceConfigList = nameServiceConfigList;
    }

    public ApplicationConfig getApplicationConfig() {
        return applicationConfig;
    }

    public void setApplicationConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    protected String getHost() {
        String ip;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            ip = localHost.getHostAddress();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ip;
    }
}