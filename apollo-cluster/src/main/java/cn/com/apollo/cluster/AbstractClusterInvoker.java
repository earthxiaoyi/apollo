package cn.com.apollo.cluster;

import cn.com.apollo.cluster.dictionary.ServiceDictionary;
import cn.com.apollo.cluster.loadbalance.LoadBalance;
import cn.com.apollo.common.Invocation;
import cn.com.apollo.common.Result;
import cn.com.apollo.common.URI;
import cn.com.apollo.common.spi.ServiceLoad;
import cn.com.apollo.rpc.invoke.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractClusterInvoker<T> implements Invoker {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private ServiceDictionary<T> serviceDictionary;

    public AbstractClusterInvoker(ServiceDictionary serviceDictionary) {
        this.serviceDictionary = serviceDictionary;
    }

    @Override
    public Result invoke(final Invocation invocation) {
        List<Invoker<T>> list = list(invocation);
        LoadBalance loadBalance = getLoadBalance();
        return doInvoke(list, invocation, loadBalance);
    }

    protected List<Invoker<T>> list(Invocation invocation) {
        return serviceDictionary.list(invocation);
    }

    @Override
    public URI getUri() {
        return serviceDictionary.getUri();
    }

    public Invoker<T> select(Invocation invocation, LoadBalance loadBalance,
                             List<Invoker<T>> invokers, List<Invoker<T>> invoked) {
        if (invokers == null || invokers.isEmpty()) {
            return null;
        }
        if (invokers.size() == 1) {
            return invokers.get(0);
        }
        Invoker<T> selected = loadBalance.select(invokers, invocation);
        boolean result1 = (invoked != null && invoked.contains(selected));
        boolean result2 = (!selected.isAvailable() && getUri() != null);
        if (result1 || result2) {
            Invoker<T> reselected = reselect(invocation, loadBalance, invoked, invokers);
            if (reselected != null) {
                selected = reselected;
            } else {
                int index = invokers.indexOf(selected);
                try {
                    selected = invokers.get((index + 1) % invokers.size());
                } catch (Exception e) {
                    log.warn("重选失败");
                }
            }
        }
        return selected;
    }

    private Invoker<T> reselect(Invocation invocation, LoadBalance loadBalance,
                                List<Invoker<T>> selected, List<Invoker<T>> invokers) {
        List<Invoker<T>> reselectInvokers = new ArrayList<>(invokers.size() > 1 ? invokers.size() - 1 : invokers.size());

        for (Invoker<T> invoker : invokers) {
            if (!invoker.isAvailable()) {
                continue;
            }
            if (selected != null && !selected.contains(invoker)) {
                reselectInvokers.add(invoker);
            }
        }
        if (!reselectInvokers.isEmpty()) {
            return select(invocation, loadBalance, reselectInvokers, selected);
        }

        if (selected != null && !selected.isEmpty()) {
            for (Invoker<T> tInvoker : selected) {
                if (tInvoker.isAvailable() && !reselectInvokers.contains(tInvoker)) {
                    reselectInvokers.add(tInvoker);
                }
            }
        }
        if (!reselectInvokers.isEmpty()) {
            return select(invocation, loadBalance, reselectInvokers, selected);
        }
        return null;
    }

    public abstract Result doInvoke(List<Invoker<T>> list, Invocation invocation, LoadBalance loadBalance);

    public LoadBalance getLoadBalance() {
        return ServiceLoad.getServiceLoad(LoadBalance.class).getDefaultService();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void destory() {
        try {
            serviceDictionary.destory();
        } catch (Exception e) {
            log.warn("serviceDictionary destory fail," + e.getMessage(), e);
        }
    }

}
