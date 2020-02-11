package cn.com.apollo.cluster.loadbalance;

import cn.com.apollo.common.Invocation;
import cn.com.apollo.invoke.Invoker;

import java.util.List;
import java.util.Random;

/**
 * 加权随机算法
 */
public class WeightRandomLoadBalance extends AbstractLoadBalance {

    private final Random random = new Random();

    @Override
    public <T> Invoker<T> doSelect(List<Invoker<T>> invokers, Invocation invocation) {
        int length = invokers.size();
        int totalWeight = 0;
        boolean isSameWeight = true;
        for (int i = 0; i < length; i++) {
            Invoker invoker = invokers.get(i);
            int weight = getWeight(invoker, invocation);
            totalWeight += weight;
            if (isSameWeight && i > 0 &&
                    (weight != getWeight(invokers.get(i - 1), invocation))) {
                isSameWeight = false;
            }
        }
        if (totalWeight>0 && !isSameWeight) {
            int offset = random.nextInt(totalWeight);
            for (int i = 0; i < length; i++) {
                offset -= getWeight(invokers.get(i), invocation);
                if (offset < 0) {
                    return invokers.get(i);
                }
            }
        }
        //相同的权重
        return invokers.get(random.nextInt(length));
    }

}