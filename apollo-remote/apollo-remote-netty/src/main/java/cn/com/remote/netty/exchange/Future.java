package cn.com.remote.netty.exchange;

public interface Future {

    Object get();

    Object get(long timeout);
}
