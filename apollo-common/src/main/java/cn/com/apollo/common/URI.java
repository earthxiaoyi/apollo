package cn.com.apollo.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author jiaming
 */
public class URI implements Serializable {

    private static final long serialVersionUID = -1L;

    private String protocol;
    private String host;
    private int port;
    private String serviceName;
    private Map<String, String> parameter = new HashMap<>();

    public URI() {
    }

    public URI(String host, int port, String serviceName) {
        this.host = host;
        this.port = port;
        this.serviceName = serviceName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, String> getParameter() {
        return parameter;
    }

    public int getServiceMethodParameter(String method, String key, int defaultValue) {
        String value = parameter.get(method + "." + key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        return i;
    }

    public void setParameter(Map<String, String> parameter) {
        this.parameter = parameter;
    }

    public void addParameter(String key, String value) {
        if (key != null || value != null) {
            this.parameter.putIfAbsent(key, value);
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getParameter(String key) {
        if (key == null) {
            return null;
        }
        return parameter.get(key);
    }

    public Integer getParameter(String key, Integer defaultValue) {
        if (key == null) {
            return null;
        }
        String value = parameter.get(key);
        if (value != null && !"".equals(value.trim())) {
            return Integer.parseInt(value);
        }
        return defaultValue;
    }

    public Long getParameter(String key, Long defaultValue) {
        if (key == null) {
            return null;
        }
        String value = parameter.get(key);
        if (value != null && !"".equals(value.trim())) {
            return Long.valueOf(value);
        }
        return defaultValue;
    }

    public void putParameter(String key, boolean value) {
        parameter.put(key, String.valueOf(value));
    }

    public boolean getParameter(String key, boolean defaultValue) {
        if (key == null) {
            return false;
        }
        String value = parameter.get(key);
        if (value != null && !"".equals(value.trim())) {
            return Boolean.valueOf(value);
        }
        return defaultValue;
    }

    public String getParameter(String key, String defaultValue) {
        if (key == null) {
            return null;
        }
        String value = parameter.get(key);
        if (value != null && !"".equals(value.trim())) {
            return value;
        }
        return defaultValue;
    }

    public String getServiceIdentity() {
        return getUriString();
    }

    public String getUriString() {
        return buildString();
    }

    private String buildString() {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol);
        sb.append("://");
        String host = getHost();
        if (host != null && !"".equals(host.trim())) {
            sb.append(host);
            if (port > 0) {
                sb.append(":");
                sb.append(port);
            }
        }
        if (serviceName != null && !"".equals(serviceName)) {
            sb.append("/");
            sb.append(serviceName);
        }
        buildParameter(sb, parameter);
        return sb.toString();
    }

    private void buildParameter(StringBuilder sb, Map<String, String> parameter) {
        if (parameter.isEmpty()) {
            return;
        }
        Map<String, String> orderParameter = new TreeMap<>();
        orderParameter.putAll(parameter);
        boolean first = true;
        for (Map.Entry<String, String> entry : orderParameter.entrySet()) {
            if (first) {
                sb.append("?");
                first = false;
            } else {
                sb.append("&");
            }
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue() == null ? "" : entry.getValue().trim());
        }
    }

    public void putParameter(String key, String value) {
        parameter.put(key, value);
    }

    public static URI toURI(String s) {
        if (s == null || s.length() == 0) {
            throw new IllegalArgumentException("touri fail,uri is not null");
        }
        URI uri = new URI();
        //parse:protocol host port interface
        int i = s.indexOf("?");
        if (i >= 0) {
            Map<String, String> params = new HashMap<>(8);
            String parametersStr = s.substring(i + 1);
            String[] parameters = parametersStr.split("&");
            for (String parameter : parameters) {
                String key = parameter.substring(0, parameter.indexOf("="));
                String value = parameter.substring(parameter.indexOf("=") + 1);
                params.put(key, value);
            }
            uri.setParameter(params);
        }
        i = s.indexOf("://");
        if (i >= 0) {
            String protocol = s.substring(0, i);
            uri.setProtocol(protocol);
        }
        if (i >= 0) {
            String hostPortService = s.substring(i + 3);
            String hostPort = hostPortService.substring(0, hostPortService.indexOf("/"));
            String serviceName = hostPortService.substring(hostPortService.indexOf("/") + 1);
            String host = hostPort.substring(0, hostPort.indexOf(":"));
            String port = hostPort.substring(hostPort.indexOf(":") + 1);
            uri.setHost(host);
            uri.setPort(Integer.valueOf(port));
            uri.setServiceName(serviceName);
        }

        return uri;
    }

    public String getServiceKey() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getServiceName());
        sb.append(".");
        sb.append(this.getPort());
        sb.append(".");
        sb.append(this.getParameter(Constant.GROUP, Constant.GROUP_DEFAULT_VALUE));
        sb.append(".");
        sb.append(this.getParameter(Constant.VERSION, Constant.VERSION_DEFUALT_VALUE));
        return sb.toString();
    }

    @Override
    public String toString() {
        return buildString();
    }
}
