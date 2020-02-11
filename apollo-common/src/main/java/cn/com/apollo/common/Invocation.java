package cn.com.apollo.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiaming
 */
public class Invocation implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 方法名称
     */
    private String methodName;
    /**
     * 调用参数
     */
    private Object[] args;

    private Class<?>[] parameterTypes;

    /**
     * 接口属性
     */
    private Map<String, String> attributes;

    public Invocation(String methodName, final Object[] args) {
        this.methodName = methodName;
        this.args = args;
    }

    public Invocation() {
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }


    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void addAttributes(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        if (attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.putIfAbsent(key, value);
    }

    public Object[] getArgs() {
        return args;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(final Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
}