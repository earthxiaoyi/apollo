package cn.com.channel;

import cn.com.exchange.Future;

import java.net.InetSocketAddress;

public interface Channel {
    Future send(Object message, int timeout);

    void send(Object message, boolean sent);

    void close();

    boolean isConnected();

    void connect();

    InetSocketAddress getInetSocketAddress();
}
