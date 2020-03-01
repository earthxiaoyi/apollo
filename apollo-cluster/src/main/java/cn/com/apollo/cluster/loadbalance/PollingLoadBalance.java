package cn.com.apollo.cluster.loadbalance;

import cn.com.apollo.common.Invocation;
import cn.com.apollo.rpc.invoke.Invoker;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负载均衡算法之：轮询算法
 */
public class PollingLoadBalance implements LoadBalance {

    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, Invocation invocation) {
        int size = invokers.size();
        int index = atomicInteger.get();
        if (index > size) {
            index = atomicInteger.get() % size;
        }
        Invoker invoker = invokers.get(index);
        atomicInteger.incrementAndGet();
        return invoker;
    }

}