package cn.com.apollo.rpc.invoke;


import cn.com.apollo.common.Invocation;
import cn.com.apollo.common.Result;
import cn.com.apollo.common.URI;
import cn.com.apollo.common.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInvoker<T> implements Invoker<T> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Class<T> type;
    private final URI uri;

    public AbstractInvoker(Class<T> type, URI uri) {
        this.type = type;
        this.uri = uri;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Result invoke(Invocation invocation) {
        //获取参数
        Result result = null;
        try {
            result = doInvoke(invocation,uri);
        } catch (Exception e) {
            throw new RpcException(e.getMessage(),e);
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

    public abstract Result doInvoke(Invocation invocation,URI uri);

    @Override
    public Class<T> getInterface() {
        return type;
    }
}
