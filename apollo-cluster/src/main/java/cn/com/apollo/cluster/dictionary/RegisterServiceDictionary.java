package cn.com.apollo.cluster.dictionary;

import cn.com.NettyClient;
import cn.com.apollo.common.Constant;
import cn.com.apollo.common.Invocation;
import cn.com.apollo.common.URI;
import cn.com.apollo.invoke.Invoker;
import cn.com.apollo.invoke.RpcInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 服务字典
 */
public class RegisterServiceDictionary<T> implements ServiceDictionary<T> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private volatile List<Invoker<T>> invokers;
    private Class<T> serviceType;
    private volatile Map<String, Invoker<T>> invokerMap;
    private volatile boolean destory = false;

    public RegisterServiceDictionary(Class<T> serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public List<Invoker<T>> list(Invocation invocation) {

        return invokers;
    }

    @Override
    public boolean isServiceAvailable() {
        return false;
    }

    @Override
    public void destory() {
        if (destory) {
            return;
        }
        for (Invoker<T> invoker : invokers) {
            try {
                invoker.destory();
            } catch (Throwable e) {
                log.error("invoker destory exception", e);
            }
        }
    }

    @Override
    public URI getUri() {
        return invokers.isEmpty() ? null : invokers.get(0).getUri();
    }

    @Override
    public synchronized void notify(List<URI> services) {
        if (services == null || services.isEmpty()) {
            return;
        }
        List<URI> newUriList = new ArrayList<>(services.size());
        newUriList.addAll(services);
        refreshInvokerList(newUriList);
    }

    private void refreshInvokerList(List<URI> newUriList) {
        if (newUriList == null || newUriList.isEmpty()) {
            return;
        }
        //TODO 需要做个销毁逻辑。
        //获取老的urlInvokerMap,做为下一步销毁用
        Map<String, Invoker<T>> oldInvokerMap = this.invokerMap;
        Map<String, Invoker<T>> newInvokerMap = new HashMap<>();
        Set<String> keys = new TreeSet<>();
        for (URI uri : newUriList) {
            //根据uri创建Invoker
            String key = uri.getUriString();
            if (keys.contains(key)) {
                continue;
            }
            Map<String, Invoker<T>> invokerMap = this.invokerMap;
            Invoker<T> invoker = invokerMap == null ? null : invokerMap.get(key);
            if (invoker == null) {
                invoker = new RpcInvoker<>(serviceType, uri, getClients(uri));
            }
            newInvokerMap.put(key, invoker);
            keys.add(key);
        }
        keys.clear();
        List<Invoker<T>> newInvokerList = Collections.unmodifiableList(new ArrayList<>(newInvokerMap.values()));
        this.invokers = newInvokerList;
        this.invokerMap = newInvokerMap;
        destoryOldInvokerMap(oldInvokerMap);
    }

    private void destoryOldInvokerMap(Map<String, Invoker<T>> oldInvokerMap) {
        if (oldInvokerMap != null && !oldInvokerMap.isEmpty()) {
            for (Map.Entry<String, Invoker<T>> entry : oldInvokerMap.entrySet()) {
                Invoker<T> invoker = entry.getValue();
                try {
                    invoker.destory();
                } catch (Exception e) {
                    log.error("destory old invoker exception", e);
                }
            }
        }
    }

    private NettyClient[] getClients(URI uri) {
        Integer connect = uri.getParameter(Constant.CONNECT, Constant.DEFUAL_CONNECT);
        final NettyClient[] clients = new NettyClient[connect];
        for (int i = 0; i < clients.length; i++) {
            clients[0] = new NettyClient(uri);
        }
        return clients;
    }


}
