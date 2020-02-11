package cn.com.apollo.nameservice;

import cn.com.apollo.common.URI;

public interface NameService {

    void register(String serviceName, URI uri);

    URI subscribe(String serviceName, URI uri);

    void onListener(Notify notify);
}
