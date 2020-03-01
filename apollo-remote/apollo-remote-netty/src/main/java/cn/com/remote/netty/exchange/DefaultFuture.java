package cn.com.remote.netty.exchange;

import cn.com.apollo.common.Constant;
import cn.com.apollo.common.Header;
import cn.com.apollo.common.Request;
import cn.com.apollo.common.Response;
import cn.com.apollo.common.exception.RpcException;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jiaming on 2019/7/22.
 */
public class DefaultFuture implements Future {

    private static final Logger logger = LoggerFactory.getLogger(DefaultFuture.class);

    private static final Map<Long, DefaultFuture> FUTURES = new ConcurrentHashMap<>();

    private static final Map<Long, Channel> CHANNELS = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService SCHEDULED_SERVICE = new ScheduledThreadPoolExecutor(2, new ThreadFactory() {

        private AtomicInteger atomicInteger = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("schedule-pool-" + atomicInteger.incrementAndGet());
            return thread;
        }
    });

    private long id;
    private int timeout;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private volatile Response response;
    private Request request;
    private long start = System.currentTimeMillis();
    private long send;

    public DefaultFuture(int timeOut, Request request) {
        this.timeout = timeOut;
        /*this.channel = channel;*/
        this.request = request;
        this.id = request.getHeader().getId();
        /*CHANNELS.put(request.getHeader().getId(), channel);*/
        FUTURES.put(request.getHeader().getId(), this);
    }

    public static DefaultFuture getDefaultFuture(int timeOut, Request request) {
        DefaultFuture defaultFuture = new DefaultFuture(timeOut, request);
        SCHEDULED_SERVICE.schedule(new TimeOutCheck(defaultFuture), timeOut, TimeUnit.MILLISECONDS);
        return defaultFuture;
    }

    public boolean isDone() {
        return response != null;
    }

    @Override
    public Object get() {
        return this.get(timeout);
    }

    @Override
    public Object get(long timeout) {
        long start = System.currentTimeMillis();
        //阻塞
        if (!isDone()) {
            lock.lock();
            try {
                while (!isDone()) {
                    condition.await(timeout, TimeUnit.MILLISECONDS);
                    if (isDone() || (System.currentTimeMillis() - start) > timeout) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                logger.warn(e.getMessage(), e);
            } finally {
                lock.unlock();
            }
        }
        if (!isDone()) {
            throw new RpcException("invoke timeout,request invoker id :" + request.getHeader().getId() + ",cost:" + (System.currentTimeMillis() - start));
        }
        return response;
    }

    public static void receive(Response response) {
        try {
            if (null != response) {
                DefaultFuture defaultFuture = FUTURES.remove(response.getHeader().getId());
                if (null != defaultFuture) {
                    defaultFuture.doReceive(response);
                } else {
                    logger.warn("invoke id is not get defaultFuture,invoker id:{}", response.getHeader().getId());
                }
            }
        } finally {
            //删除channel
            /*CHANNELS.remove(response.getHeader().getId());*/
        }
    }

    private void doReceive(Response response) {
        lock.lock();
        try {
            this.response = response;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public int getTimeout() {
        return timeout;
    }

    public long getStart() {
        return start;
    }

    public boolean isSend() {
        return send > 0;
    }

    public static void setSend(Request request) {
        DefaultFuture defaultFuture = FUTURES.get(request.getHeader().getId());
        if (null != defaultFuture) {
            defaultFuture.setSend(System.currentTimeMillis());
        }
    }

    public void setSend(long send) {
        this.send = send;
    }

    public Response getResponse() {
        return response;
    }

    private static class TimeOutCheck implements Runnable {

        private DefaultFuture defaultFuture;

        public TimeOutCheck(DefaultFuture defaultFuture) {
            this.defaultFuture = defaultFuture;
        }

        @Override
        public void run() {
            if (defaultFuture == null || defaultFuture.isDone()) {
                return;
            }
            Response timeoutResponse = new Response();
            Header header = new Header();
            header.setId(defaultFuture.getId());
            header.setEventType(Constant.RESPONSE);
            header.setStatus(defaultFuture.isSend() ? Constant.RESPONSE_TIMEOUT : Constant.REQUEST_TIMEOUT);
            timeoutResponse.setHeader(header);
            defaultFuture.doReceive(timeoutResponse);
        }
    }

    public long getId() {
        return id;
    }
}
