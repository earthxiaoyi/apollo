package cn.com.exchange;

public interface Future {

    Object get();

    Object get(long timeout);
}
