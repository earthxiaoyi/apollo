package cn.com.apollo.cluster.loadbalance;

import cn.com.apollo.common.Invocation;
import cn.com.apollo.invoke.Invoker;

import java.util.List;
import java.util.Random;

/**
 * 负载均衡之：随机算法
 */
public class RandomLoadBalance implements LoadBalance {

    private final Random random = new Random();

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, Invocation invocation) {
        int size = invokers.size();
        int index = random.nextInt(size);
        return invokers.get(index);
    }
}
