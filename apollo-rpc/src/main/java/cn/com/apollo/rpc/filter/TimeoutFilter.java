package cn.com.apollo.rpc.filter;

import cn.com.apollo.common.Constant;
import cn.com.apollo.common.spi.Active;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.apollo.common.Invocation;
import cn.com.apollo.common.Result;

import cn.com.apollo.rpc.invoke.Invoker;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiaming
 */
@Active(group = {Constant.PROVIDER})
public class TimeoutFilter implements Filter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String TIMEOUT_FILTER_START_TIME = "timeout_filter_start_time";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws Exception {
        if (invocation.getAttributes() != null) {
            long startTime = System.currentTimeMillis();
            Map<String, String> attributes = invocation.getAttributes();
            attributes.put(TIMEOUT_FILTER_START_TIME, String.valueOf(startTime));
        } else {
            Map<String, String> attributes = new HashMap<>(8);
            long startTime = System.currentTimeMillis();
            attributes.put(TIMEOUT_FILTER_START_TIME, String.valueOf(startTime));
            invocation.setAttributes(attributes);
        }
        return invoker.invoke(invocation);
    }

    @Override
    public Result onResponse(Invoker<?> invoker, Result result, Invocation invocation) {
        String startTime = invocation.getAttributes().get(TIMEOUT_FILTER_START_TIME);
        if (startTime != null) {
            long var = System.currentTimeMillis() - Long.parseLong(startTime);
            int timeout = invoker.getUri().getParameter(Constant.TIME_OUT, Constant.TIMEOUT).intValue();
            if (var > timeout) {
                log.warn("cn.com.apollo.rpc.invoke the method:" + invocation.getMethodName() + " timeout,args:" +
                        Arrays.toString(invocation.getArgs()) + " uri:" + invoker.getUri().getUriString() +
                        " cost:" + var + " ms.");
            }
        }
        return result;
    }

}
