package cn.com.apollo.proxy.jdk;

import cn.com.apollo.common.Invocation;
import cn.com.apollo.common.Result;
import cn.com.apollo.invoke.Invoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by jiaming on 2019/7/21.
 */
public class JdkProxyHandler implements InvocationHandler {

    private Class<?> target;
    private Invoker<?> invoker;
    private String interfaceName;

    public JdkProxyHandler(Class<?> target, Invoker<?> invoker, String interfaceName) {
        this.target = target;
        this.invoker = invoker;
        this.interfaceName = interfaceName;
    }

    @Override
    public Object invoke(Object proxy, Method method, final Object[] args) throws Throwable {
        Invocation invocation = new Invocation(method.getName(), args);
        invocation.addAttributes("interfaceName", interfaceName);
        invocation.setParameterTypes(method.getParameterTypes());
        Result result = invoker.invoke(invocation);
        return result.getData();
    }

    public <T> T getProxy() {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{target}, this);
    }

}