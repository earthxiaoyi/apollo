package cn.com.remote.netty;

import cn.com.apollo.common.Constant;
import cn.com.apollo.common.URI;
import cn.com.apollo.common.exception.RemoteException;
import cn.com.remote.netty.code.NettyCodec;
import cn.com.remote.netty.dispatcher.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by jiaming on 2019/4/20.
 */
public class NettyClient extends AbstractChannel {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(Constant.IO_THREAD_DEFAULT_NUM, new DefaultThreadFactory("NettyClientHandler", true));

    private Bootstrap bootstrap;
    private volatile Channel channel;
    private volatile boolean connected = false;
    private volatile boolean destory = false;

    public NettyClient(URI uri) {
        super(uri);
    }

    @Override
    public void connect() {
        doConnect();
    }

    protected void doConnect() {
        try {
            initClient();
            //建立连接
            ChannelFuture future = bootstrap.connect(getUri().getHost(), getUri().getPort());
            boolean isConnected = future.awaitUninterruptibly(Constant.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
            //判断连接是否建立成功
            if (isConnected && future.isSuccess()) {
                Channel newChannel = future.channel();
                Channel oldChannel = this.channel;
                this.channel = newChannel;
                if (oldChannel != null) {
                    oldChannel.close();
                }
                connected = true;
            }
        } catch (Exception e) {
            throw new RemoteException("client:" + getUri() + "to connnect server:" + getUri().getHost() + ",error message is:" + e.getMessage(), e);
        }
    }

    private void initClient() {
        bootstrap = new Bootstrap();
        bootstrap.group(bossGroup)
                .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        Integer heartbeatTimeOut = getUri().getParameter(Constant.HEARTBEAT_TIMEOUT, Constant.DEFAULT_HEARTBEAT_TIMEOUT);
                        NettyCodec codec = new NettyCodec(getUri());
                        ch.pipeline().addLast(codec.getDecoder());
                        ch.pipeline().addLast(codec.getEncoder());
                        ch.pipeline().addLast(new IdleStateHandler(heartbeatTimeOut, 0, 0, TimeUnit.MILLISECONDS));
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public void close() {
        try {
            if (channel != null) {
                channel.close();
                destory = true;
            }
        } catch (Exception e) {
            log.warn("netty close exception:" + e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }


}