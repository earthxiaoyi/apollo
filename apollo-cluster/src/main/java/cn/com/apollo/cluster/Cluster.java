package cn.com.apollo.cluster;

import cn.com.apollo.cluster.dictionary.ServiceDictionary;
import cn.com.apollo.common.spi.SPI;
import cn.com.apollo.rpc.invoke.Invoker;

@SPI("failOverCluster")
public interface Cluster {

    <T> Invoker<T> add(ServiceDictionary<T> dictionary);

}
