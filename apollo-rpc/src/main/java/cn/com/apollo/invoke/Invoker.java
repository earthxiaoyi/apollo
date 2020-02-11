package cn.com.apollo.invoke;

import cn.com.apollo.common.Invocation;
import cn.com.apollo.common.Result;
import cn.com.apollo.common.URI;

public interface Invoker<T> {

    Class<T> getInterface();

    boolean isAvailable();

    Result invoke(final Invocation invocation) throws Exception;

    URI getUri();

    void destory();
}
