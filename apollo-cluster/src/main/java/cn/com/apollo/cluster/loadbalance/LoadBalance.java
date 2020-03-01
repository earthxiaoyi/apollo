package cn.com.apollo.cluster.loadbalance;

import cn.com.apollo.common.Invocation;
import cn.com.apollo.common.spi.SPI;
import cn.com.apollo.rpc.invoke.Invoker;

import java.util.List;

/**
 * 负载均衡策略类
 */
@SPI("weightPollingLoadBalance")
public interface LoadBalance {

    <T> Invoker<T> select(List<Invoker<T>> invokers, Invocation invocation);

}
