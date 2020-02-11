package cn.com.apollo.filter;

import cn.com.apollo.common.Invocation;
import cn.com.apollo.common.Result;
import cn.com.apollo.common.URI;
import cn.com.apollo.common.spi.ServiceLoad;
import cn.com.apollo.invoke.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author jiaming
 */
public class InvokerFilterChain {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public static <T> Invoker<T> buildInvokerChain(final Invoker<?> invoker, String filterKey, String group) {
        Invoker lastInvoker = invoker;
        String serviceFilter = invoker.getUri().getParameter(filterKey);
        String[] values = serviceFilter == null ? new String[]{} : serviceFilter.split("\\,");
        List<Filter> filters = ServiceLoad.getServiceLoad(Filter.class).getActiveServices(invoker.getUri(), values, group);
        for (int i = filters.size() - 1; i >= 0; i--) {

            final Filter filter = filters.get(i);
            final Invoker<T> next = lastInvoker;
            lastInvoker = new Invoker() {

                @Override
                public Class getInterface() {
                    return invoker.getInterface();
                }

                @Override
                public boolean isAvailable() {
                    return invoker.isAvailable();
                }

                @Override
                public Result invoke(Invocation invocation) throws Exception {
                    Result result = filter.invoke(next, invocation);
                    return filter.onResponse(invoker, result, invocation);
                }

                @Override
                public URI getUri() {
                    return invoker.getUri();
                }

                @Override
                public void destory() {
                    invoker.destory();
                }
            };
        }
        return lastInvoker;
    }


}
