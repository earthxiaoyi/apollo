package cn.com.apollo.invoke;

import cn.com.apollo.common.Invocation;
import cn.com.apollo.common.Result;
import cn.com.apollo.common.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jiaming
 * @param <T>
 */
public abstract class AbstractProxyInvoker<T> implements Invoker<T> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Class<T> type;
    private final URI uri;
    private T proxy;

    public AbstractProxyInvoker(T proxy, Class<T> type, URI uri) {
        this.type = type;
        this.uri = uri;
        this.proxy = proxy;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Result invoke(Invocation invocation) {
        //获取参数
        Result result;
        try {
            result = doInvoke(proxy, invocation.getMethodName(), invocation.getParameterTypes(),
                    invocation.getArgs());
        } catch (Throwable e) {
            throw new RuntimeException("fail to cn.com.apollo.rpc.invoke the method " + invocation.getMethodName() + " uri " + uri.getUriString(), e);
        }
        return result;
    }

    @Override
    public URI getUri() {
        return this.uri;
    }

    @Override
    public void destory() {

    }

    public abstract Result doInvoke(T proxy, String methodName, Class<?>[] parameterTypes, Object[] arguments) throws Throwable;

    @Override
    public Class<T> getInterface() {
        return type;
    }
}
