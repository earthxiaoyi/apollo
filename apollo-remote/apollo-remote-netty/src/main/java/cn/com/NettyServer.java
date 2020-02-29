package cn.com;

import cn.com.apollo.common.Constant;
import cn.com.apollo.common.URI;
import cn.com.code.NettyCodec;
import cn.com.dispatcher.AllHandlerDispatcher;
import cn.com.handler.DecoderHandler;
import cn.com.handler.Handler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author jiaming
 */
public class NettyServer {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private NioEventLoopGroup bossGroup = null;
    private NioEventLoopGroup workerGroup = null;
    private URI uri;
    private Channel channel;
    private Handler handler;

    public NettyServer(URI uri, Handler handler) {
        this.uri = uri;
        this.handler = handler;
    }

    public void start() {
        synchronized (NettyServer.class) {
            if (bossGroup != null || workerGroup != null) {
                return;
            }
            bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("nettyServerBoss", true));
            workerGroup = new NioEventLoopGroup(uri.getParameter(Constant.IO_THREAD, Constant.IO_THREAD_DEFAULT_NUM),
                    new DefaultThreadFactory("nettyServerWork", true));
            Handler decoderHandler = new DecoderHandler(handler);
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                        .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                Integer heartbeatTimeOut = uri.getParameter(Constant.HEARTBEAT_TIMEOUT, Constant.DEFAULT_HEARTBEAT_TIMEOUT * 3);
                                //注册解码器
                                NettyCodec codec = new NettyCodec(uri);
                                ch.pipeline().addLast(codec.getDecoder());
                                //注册编码器
                                ch.pipeline().addLast(codec.getEncoder());
                                ch.pipeline().addLast(new IdleStateHandler(heartbeatTimeOut, 0, 0, TimeUnit.MILLISECONDS));
                                //分发器
                                ch.pipeline().addLast(new AllHandlerDispatcher(decoderHandler, uri));
                            }

                        });
                InetSocketAddress inetSocketAddress = new InetSocketAddress(uri.getHost(), uri.getPort());
                ChannelFuture channelFuture = b.bind(inetSocketAddress);
                channelFuture.syncUninterruptibly();
                channel = channelFuture.channel();
            } catch (Throwable e) {
                log.error("netty server start fail exception:", e);
            }
        }
    }

    public void close() {
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
        try {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
    }

}