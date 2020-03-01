package cn.com.apollo.cluster;

import cn.com.apollo.cluster.dictionary.ServiceDictionary;
import cn.com.apollo.rpc.invoke.Invoker;

public class FailOverCluster implements Cluster {

    private static final String NAME = "failOverCluster";

    @Override
    public <T> Invoker<T> add(ServiceDictionary<T> dictionary) {
        return new FailOverClusterInvoker<T>(dictionary);
    }

}
