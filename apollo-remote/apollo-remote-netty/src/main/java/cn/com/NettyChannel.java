package cn.com;

import cn.com.apollo.common.Constant;
import cn.com.apollo.common.Request;
import cn.com.apollo.common.URI;
import cn.com.apollo.common.exception.RemoteException;
import cn.com.apollo.common.exception.RpcException;
import cn.com.exchange.DefaultFuture;
import cn.com.exchange.Future;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class NettyChannel extends AbstractChannel {

    private static final ConcurrentHashMap<Channel, NettyChannel> CHANNEL_MAP = new ConcurrentHashMap<>();

    private Channel channel;
    private volatile boolean destory = false;
    private volatile boolean isClose = false;

    public NettyChannel(URI uri, Channel channel) {
        super(uri);
        this.channel = channel;
    }

    @Override
    public Future send(Object obj, int timeout) {
        if (null == obj) {
            throw new RpcException("send message is not be null");
        }
        if (destory || null == channel || !channel.isActive()) {
            throw new RemoteException("connect is closed not send message,host:" + getUri().getHost());
        }
        boolean success;
        try {
            ChannelFuture future = channel.writeAndFlush(obj);
            success = future.await(timeout, TimeUnit.MILLISECONDS);
            Throwable cause = future.cause();
            if (cause != null) {
                throw cause;
            }
        } catch (Throwable e) {
            throw new RpcException("fail to invoke " + channel.remoteAddress(), e);
        }
        if (!success) {
            throw new RpcException("fail to invoke " + channel.remoteAddress());
        }
        DefaultFuture future = DefaultFuture.getDefaultFuture(timeout, (Request) obj);
        return future;
    }

    @Override
    public void send(Object message, boolean sent) {
        if (null == message) {
            throw new IllegalArgumentException("message is not be null");
        }
        if (destory || null == channel || !channel.isActive()) {
            throw new RemoteException("connect is closed,not send message,host:" + getUri().getHost());
        }
        boolean success = true;
        try {
            ChannelFuture future = channel.writeAndFlush(message);
            if (sent) {
                success = future.await(Constant.TIMEOUT, TimeUnit.MILLISECONDS);
            }
            Throwable cause = future.cause();
            if (cause != null) {
                throw cause;
            }
        } catch (Throwable e) {
            throw new RpcException("fail invoke server：" + channel.remoteAddress() + "", e);
        }
        if (!success) {
            throw new RpcException("fail invoke server：" + channel.remoteAddress());
        }
    }

    @Override
    public Future send(Object obj) {
        return send(obj, Constant.TIMEOUT);
    }

    @Override
    public void close() {
        try {
            channel.close();
            isClose = true;
        } catch (Throwable e) {
            throw e;
        }
    }

    @Override
    public boolean isConnected() {
        return !isClose && channel.isActive();
    }

    @Override
    public void connect() {

    }

    @Override
    public InetSocketAddress getInetSocketAddress() {
        return (InetSocketAddress) channel.localAddress();
    }

    @Override
    Channel getChannel() {
        return channel;
    }

    public static NettyChannel getChannel(Channel channel, URI uri) {
        NettyChannel nc = CHANNEL_MAP.get(channel);
        if (nc == null) {
            NettyChannel nettyChannel = new NettyChannel(uri, channel);
            if (channel.isActive()) {
                nc = CHANNEL_MAP.putIfAbsent(channel, nettyChannel);
            }
            if (nc == null) {
                nc = nettyChannel;
            }
        }
        return nc;
    }

}
