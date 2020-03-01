package cn.com.apollo.rpc.proxy.jdk;

import cn.com.apollo.common.Invocation;
import cn.com.apollo.common.Result;
import cn.com.apollo.rpc.invoke.Invoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author jiaming
 */
public class InvokerProxyHandler implements InvocationHandler {

    private Invoker<?> invoker;

    public InvokerProxyHandler(Invoker<?> invoker) {
        this.invoker = invoker;
    }

    @Override
    public Object invoke(Object proxy, Method method, final Object[] args) throws Throwable {
        Invocation invocation = new Invocation(method.getName(), args);
        invocation.addAttributes("interfaceName", invoker.getUri().getServiceName());
        invocation.setParameterTypes(method.getParameterTypes());
        Result result = invoker.invoke(invocation);
        return result.getData();
    }

}