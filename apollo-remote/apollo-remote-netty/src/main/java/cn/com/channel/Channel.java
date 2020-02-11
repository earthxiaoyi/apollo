package cn.com.channel;

import cn.com.exchange.Future;

import java.net.InetSocketAddress;

public interface Channel {
    Future send(Object obj, int timeout);

    void send(Object message, boolean sent);

    Future send(Object obj);

    void close();

    boolean isConnected();

    void connect();

    InetSocketAddress getInetSocketAddress();
}
