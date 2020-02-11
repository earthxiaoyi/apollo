package cn.com.apollo.reference;


import cn.com.apollo.common.URI;
import cn.com.apollo.config.NameServiceConfig;
import cn.com.apollo.invoke.Invoker;

public interface Reference {

    <T> Invoker<T> refer(Class<?> type, NameServiceConfig nameServiceConfig, URI uri);

}
