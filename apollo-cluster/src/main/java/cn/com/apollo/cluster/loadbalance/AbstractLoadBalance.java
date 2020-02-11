package cn.com.apollo.cluster.loadbalance;

import cn.com.apollo.common.Constant;
import cn.com.apollo.common.Invocation;
import cn.com.apollo.invoke.Invoker;

import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, Invocation invocation) {
        if (invokers == null || invokers.isEmpty()) {
            return null;
        }
        if (invokers.size() == 1) {
            return invokers.get(0);
        }
        return doSelect(invokers, invocation);
    }

    protected abstract <T> Invoker<T> doSelect(List<Invoker<T>> invokers, Invocation invocation);

    protected int getWeight(Invoker<?> invoker, Invocation invocation) {
        int weight = invoker.getUri().getServiceMethodParameter(invocation.getMethodName(),
                        Constant.WEIGHT, Constant.DEFAULT_WEIGHT);
        return weight;
    }

}
