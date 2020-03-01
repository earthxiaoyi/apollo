package cn.com.apollo.rpc.model;

public class ServiceModel {

    private Object service;
    private Class<?> interfaceClass;
    private String serviceName;

    public ServiceModel(Object service, Class<?> interfaceClass, String serviceName) {
        this.service = service;
        this.interfaceClass = interfaceClass;
        this.serviceName = serviceName;
    }

    public Object getService() {
        return service;
    }

    public void setService(Object service) {
        this.service = service;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
