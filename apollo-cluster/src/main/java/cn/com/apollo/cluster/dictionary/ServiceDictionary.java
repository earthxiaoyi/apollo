package cn.com.apollo.cluster.dictionary;

import cn.com.apollo.common.Invocation;
import cn.com.apollo.common.URI;
import cn.com.apollo.nameservice.Notify;
import cn.com.apollo.invoke.Invoker;

import java.util.List;

/**
 * 服务字典接口
 */
public interface ServiceDictionary<T> extends Notify {

    List<Invoker<T>> list(Invocation invocation);

    boolean isServiceAvailable();

    void destory();

    URI getUri();
}
