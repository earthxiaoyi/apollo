package cn.com.apollo.handler;

import cn.com.apollo.common.*;
import cn.com.channel.Channel;
import cn.com.handler.Handler;
import cn.com.apollo.invoke.Invoker;
import cn.com.apollo.model.ServiceModel;
import cn.com.apollo.model.ServiceProviders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class ApolloHandler implements Handler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void connect(Channel channel, Object msg) {

    }

    @Override
    public void disconnect(Channel channel, Object msg) {

    }

    @Override
    public void recevied(Channel channel, Object msg) {
        if (msg instanceof Request) {
            Request request = (Request) msg;
            Invocation invocation = (Invocation) request.getData();
            Invoker<?> invoker = getInvoker(channel, invocation);
            Result result;
            Response response = new Response();
            Header resHeader = new Header();
            try {
                result = invoker.invoke(invocation);
                //响应请求
                resHeader.setId(request.getHeader().getId());
                resHeader.setEventType(Constant.RESPONSE);
                resHeader.setStatus(Constant.SUCCESS);
            } catch (Exception e) {
                resHeader.setId(request.getHeader().getId());
                resHeader.setEventType(Constant.RESPONSE);
                resHeader.setStatus(Constant.BAD_RESPONSE);
                result = new Result(e);
            }
            response.setHeader(resHeader);
            response.setData(result);
            channel.send(response, true);
        }
    }

    private Invoker<?> getInvoker(Channel channel, Invocation invocation) {
        InetSocketAddress inetSocketAddress = channel.getInetSocketAddress();
        int port = inetSocketAddress.getPort();
        String group = invocation.getAttributes().getOrDefault(Constant.GROUP, Constant.GROUP_DEFAULT_VALUE);
        String version = invocation.getAttributes().getOrDefault(Constant.VERSION, Constant.VERSION_DEFUALT_VALUE);
        String serviceInterface = invocation.getAttributes().get("interfaceName");
        String serviceKey = serviceKey(serviceInterface, port, group, version);
        ServiceModel serviceModel = ServiceProviders.getServiceModel(serviceKey);
        if (serviceModel == null) {
            log.warn("没有找到serviceKey:{} 对应的Invoker", serviceKey);
            return null;
        }
        return (Invoker) serviceModel.getService();
    }

    private String serviceKey(String serviceInterface, int port, String group, String version) {
        StringBuilder sb = new StringBuilder();
        sb.append(serviceInterface);
        sb.append(".");
        sb.append(port);
        sb.append(".");
        sb.append(group);
        sb.append(".");
        sb.append(version);
        return sb.toString();
    }

    @Override
    public void sent(Channel channel, Object result) {

    }

    @Override
    public void cause(Channel channel, Throwable cause) {

    }
}
