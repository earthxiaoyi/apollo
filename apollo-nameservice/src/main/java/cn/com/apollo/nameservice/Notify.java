package cn.com.apollo.nameservice;

import cn.com.apollo.common.URI;

import java.util.List;

public interface Notify {

    void notify(List<URI> services);

}
