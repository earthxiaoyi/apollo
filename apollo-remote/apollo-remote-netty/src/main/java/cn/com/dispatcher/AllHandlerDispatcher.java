package cn.com.dispatcher;

import cn.com.NettyChannel;
import cn.com.apollo.common.URI;
import cn.com.event.ChannelEventHandler;
import cn.com.handler.Handler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jiaming on 2019/5/3.
 */
public class AllHandlerDispatcher extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger(AllHandlerDispatcher.class);

    private static final ExecutorService executorService = new ThreadPoolExecutor(10, 10, 5000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1000), new ThreadFactory() {

        private AtomicInteger atomicInteger = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("all-cn.com.apollo.rpc.handler-dispatcher-" + atomicInteger.incrementAndGet());
            return thread;
        }
    });

    private URI uri;
    private Handler handler;

    public AllHandlerDispatcher(Handler handler, URI uri) {
        this.handler = handler;
        this.uri = uri;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        try {
            Channel channel = ctx.channel();
            NettyChannel nettyChannel = NettyChannel.getChannel(channel, uri);
            executorService.execute(new ChannelEventHandler(nettyChannel, handler, null, ChannelEventHandler.ChannelEventState.CONNECTED));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        try {
            Channel channel = ctx.channel();
            NettyChannel nettyChannel = NettyChannel.getChannel(channel, uri);
            executorService.execute(new ChannelEventHandler(nettyChannel, handler, null, ChannelEventHandler.ChannelEventState.DISCONNECTED));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        try {
            Channel channel = ctx.channel();
            NettyChannel nettyChannel = NettyChannel.getChannel(channel, uri);
            executorService.execute(new ChannelEventHandler(nettyChannel, handler, obj, ChannelEventHandler.ChannelEventState.RECEIVED));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        try {
            Channel channel = ctx.channel();
            NettyChannel nettyChannel = NettyChannel.getChannel(channel, uri);
            executorService.execute(new ChannelEventHandler(nettyChannel, handler, null, ChannelEventHandler.ChannelEventState.CAUGHT, cause));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
