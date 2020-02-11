package cn.com.apollo.filter;

import cn.com.apollo.common.Invocation;
import cn.com.apollo.common.Result;
import cn.com.apollo.common.spi.SPI;
import cn.com.apollo.invoke.Invoker;

/**
 * @author jiaming
 */
@SPI
public interface Filter {

    /**
     * 拦截器调用方法
     *
     * @param invoker
     * @param invocation
     * @return
     * @throws Exception
     */
    Result invoke(Invoker<?> invoker, Invocation invocation) throws Exception;

    /**
     * 返回结果
     *
     * @param invoker
     * @param result
     * @param invocation
     * @return result
     */
    default Result onResponse(Invoker<?> invoker, Result result, Invocation invocation) {
        return result;
    }

}