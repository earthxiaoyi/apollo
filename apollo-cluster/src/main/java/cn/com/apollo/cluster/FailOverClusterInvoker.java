package cn.com.apollo.cluster;

import cn.com.apollo.cluster.dictionary.ServiceDictionary;
import cn.com.apollo.cluster.loadbalance.LoadBalance;
import cn.com.apollo.common.Constant;
import cn.com.apollo.common.Invocation;
import cn.com.apollo.common.Result;
import cn.com.apollo.common.URI;
import cn.com.apollo.invoke.Invoker;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jiaming
 * @param <T>
 */
public class FailOverClusterInvoker<T> extends AbstractClusterInvoker<T> {

    public FailOverClusterInvoker(ServiceDictionary serviceDictionary) {
        super(serviceDictionary);
    }

    @Override
    public Result doInvoke(final List<Invoker<T>> invokers, Invocation invocation, LoadBalance loadBalance) {
        //check Invoker list
        List<Invoker<T>> newInvoker = invokers;
        checkInvokers(newInvoker);
        //获取重试次数
        String methodName = invocation.getMethodName();
        URI URI = getUri();
        int retry = URI.getServiceMethodParameter(methodName, Constant.RETRY_KEY, Constant.RETRY_NUMS);
        Exception exception = null;
        //被调用过得invoke
        List<Invoker<T>> invoked = new ArrayList<>(newInvoker.size());
        for (int i = 0; i < retry; i++) {
            //重试前重新列举invokers
            if (i > 0) {
                newInvoker = list(invocation);
                checkInvokers(newInvoker);
            }
            Invoker<T> invoker = select(invocation, loadBalance, newInvoker, invoked);
            invoked.add(invoker);
            try {
                //调用服务
                Result result = invoker.invoke(invocation);
                if (exception != null) {
                    log.warn("service cn.com.apollo.rpc.invoke retry the method " + invocation.getMethodName());
                }
                return result;
            } catch (Exception e) {
                exception = e;
            } catch (Throwable e) {
                exception = new RuntimeException(e.getMessage(), e);
            }
        }
        throw new RuntimeException("cn.com.apollo.rpc.invoke the method fail", exception);
    }

    private void checkInvokers(List<Invoker<T>> list) {
        if (list == null || list.isEmpty()) {
            throw new RuntimeException("service provider is null");
        }
    }

    @Override
    public Class getInterface() {
        return this.getClass();
    }

    @Override
    public void destory() {
        super.destory();
    }
}
