package cn.com;

import cn.com.apollo.common.Constant;
import cn.com.apollo.common.URI;
import cn.com.dispatcher.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by jiaming on 2019/4/20.
 */
public class NettyClient extends AbstractChannel {

    private Logger log = LoggerFactory.getLogger(this.getClass());

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
            throw new RuntimeException(e);
        }
    }

    private void initClient() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(bossGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        Integer heartbeatTimeOut = getUri().getParameter(Constant.HEARTBEAT_TIMEOUT, Constant.DEFAULT_HEARTBEAT_TIMEOUT);
                        ch.pipeline().addLast(new MessageDecoder(Constant.MAX_PAYLOAD,
                                Constant.MSG_LENGTH_OFFSET, 4, getUri()));
                        ch.pipeline().addLast(new MessageEncoder(getUri()));
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
            channel.close();
            destory = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }


}