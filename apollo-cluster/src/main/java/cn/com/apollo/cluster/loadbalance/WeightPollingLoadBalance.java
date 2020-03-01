package cn.com.apollo.cluster.loadbalance;

import cn.com.apollo.common.Invocation;
import cn.com.apollo.rpc.invoke.Invoker;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jiaming
 */
public class WeightPollingLoadBalance extends AbstractLoadBalance {

    private ConcurrentMap<String, ConcurrentHashMap<String, WeightPollingLoad>> serviceMethodMap =
            new ConcurrentHashMap<>();
    private long period = 60000;
    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, Invocation invocation) {
        String sKey = invokers.get(0).getUri().getServiceName() + "." + invocation.getMethodName();
        ConcurrentHashMap<String, WeightPollingLoad> map = serviceMethodMap.get(sKey);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            serviceMethodMap.putIfAbsent(sKey, map);
        }
        int totalWeight = 0;
        int maxCurrent = 0;
        long currentTime = System.currentTimeMillis();
        Invoker<T> invoker = null;
        WeightPollingLoad selectedWPL = null;
        for (Invoker<T> tInvoker : invokers) {
            String serviceIdentity = tInvoker.getUri().getServiceIdentity();
            WeightPollingLoad weightPolling = map.get(serviceIdentity);
            int weight = getWeight(tInvoker, invocation);
            if (weightPolling == null) {
                weightPolling = new WeightPollingLoad();
                weightPolling.setWeight(weight);
                map.putIfAbsent(serviceIdentity, weightPolling);
            }
            int current = weightPolling.addCurrentWeight();
            weightPolling.setLastUpdate(currentTime);
            if (current > maxCurrent) {
                maxCurrent = current;
                invoker = tInvoker;
                selectedWPL = weightPolling;
            }
            totalWeight += weight;
        }

        //更新WeightPollingLoad中挂掉的节点
        if (!atomicBoolean.get() && invokers.size() != map.size()) {
            if (atomicBoolean.compareAndSet(false, true)) {
                try {
                    ConcurrentHashMap<String, WeightPollingLoad> newMap = new ConcurrentHashMap<>();
                    newMap.putAll(map);
                    Iterator<Map.Entry<String, WeightPollingLoad>> it = newMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, WeightPollingLoad> next = it.next();
                        WeightPollingLoad weightPollingLoad = next.getValue();
                        if ((currentTime - weightPollingLoad.getLastUpdate()) > period) {
                            it.remove();
                        }
                    }
                    serviceMethodMap.put(sKey, newMap);
                } finally {
                    atomicBoolean.set(false);
                }
            }
        }

        if (selectedWPL != null) {
            //被选中的减去总权重
            selectedWPL.reduceTotalWeight(totalWeight);
            return invoker;
        }
        return invokers.get(0);
    }

   static class WeightPollingLoad {

        private int weight;
        private AtomicInteger currentWeight = new AtomicInteger(0);
        private long lastUpdate;

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public int addCurrentWeight() {
            return currentWeight.addAndGet(weight);
        }

        public void reduceTotalWeight(int totalWeight) {
            currentWeight.addAndGet(-1 * totalWeight);
        }

        public long getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }

}
