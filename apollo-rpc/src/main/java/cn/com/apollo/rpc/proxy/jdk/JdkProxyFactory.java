package cn.com.apollo.rpc.proxy.jdk;

import cn.com.apollo.common.Result;
import cn.com.apollo.common.URI;
import cn.com.apollo.rpc.invoke.AbstractProxyInvoker;
import cn.com.apollo.rpc.invoke.Invoker;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author jiaming
 */
public class JdkProxyFactory {

    public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaceClasses) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                interfaceClasses, new InvokerProxyHandler(invoker));
    }

    public static <T> Invoker<T> getInvoker(T proxy, Class<T> type, URI uri) {
        return new AbstractProxyInvoker<T>(proxy, type, uri) {

            @Override
            public Result doInvoke(T proxy, String methodName, Class<?>[] parameterTypes, Object[] arguments) throws Throwable {
                Result result = new Result();
                Method method = proxy.getClass().getMethod(methodName, parameterTypes);
                Object res = method.invoke(proxy, arguments);
                result.setData(res);
                return result;
            }

        };
    }

}
