package cn.com.apollo.invoke;

import cn.com.NettyClient;
import cn.com.apollo.common.*;
import cn.com.apollo.common.exception.RpcException;
import cn.com.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 远程rpc调用对象
 * Created by jiaming.jiang on 2019/8/21.
 */
public class RpcInvoker<T> extends AbstractInvoker<T> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AtomicInteger index = new AtomicInteger(0);
    private final Channel[] channels;

    @Override
    public Result doInvoke(Invocation invocation, URI uri) {
        Channel currentClient;
        int length = channels.length;
        if (length == 1) {
            currentClient = channels[0];
        } else {
            currentClient = channels[index.incrementAndGet() % length];
        }
        //建立连接
        if (!currentClient.isConnected()) {
            currentClient.connect();
        }

        Integer timeout = uri.getParameter(Constant.TIME_OUT, Constant.TIMEOUT);
        Request request = new Request();
        Header header = new Header();
        header.setEventType(Constant.REQUEST);
        header.setStatus(Constant.SUCCESS);
        header.setSerialType(Constant.KRYO_SERIALIZE);
        header.setId(request.newId());
        request.setHeader(header);
        request.setData(invocation);
        Response response = (Response) currentClient.send(request, timeout).get();
        Result result;
        if (0 == response.getHeader().getStatus()) {
            //success
            result = (Result) response.getData();
        } else {
            throw new RpcException("invoke exception");
        }
        return result;
    }

    public RpcInvoker(Class type, URI uri) {
        super(type, uri);
        Channel[] channels = getChannels(uri);
        this.channels = channels;
    }

    public RpcInvoker(Class type, URI uri, final Channel[] channels) {
        super(type, uri);
        this.channels = channels;
    }

    private Channel[] getChannels(URI uri) {
        NettyClient nettyClient = new NettyClient(uri);
        Channel[] remoteChannels = new Channel[1];
        remoteChannels[0] = nettyClient;
        return remoteChannels;
    }

    @Override
    public boolean isAvailable() {
        for (Channel client : channels) {
            if (client.isConnected()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destory() {
        for (Channel client : channels) {
            //TODO 未来需要关闭心跳检查任务，断线重连任务
            client.close();
        }
    }
}