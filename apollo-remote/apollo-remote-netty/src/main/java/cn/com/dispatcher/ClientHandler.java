package cn.com.dispatcher;

import cn.com.NettyChannel;
import cn.com.apollo.common.Constant;
import cn.com.apollo.common.Header;
import cn.com.apollo.common.Request;
import cn.com.apollo.common.Response;
import cn.com.exchange.DefaultFuture;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jiaming
 */
public class ClientHandler extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof Request) {
                Request request = (Request) msg;
            } else if (msg instanceof Response) {
                DefaultFuture.receive((Response) msg);
            }
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
        boolean isRequest = msg instanceof Request;
        promise.addListener(future -> {
            try {
                if (isRequest) {
                    boolean success = future.isSuccess();
                    Request request = (Request) msg;
                    if (success) {
                        DefaultFuture.setSend(request);
                    }
                    if (!success) {
                        Response response = new Response();
                        Header header = new Header();
                        header.setId(request.getHeader().getId());
                        header.setEventType((byte) 0);
                        header.setStatus(Constant.BAD_REQUEST);
                        response.setHeader(header);
                        DefaultFuture.receive(response);
                    }
                }
            } finally {
                NettyChannel.removeChannelIfDisconnected(ctx.channel());
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage(), cause);
        NettyChannel.removeChannelIfDisconnected(ctx.channel());
    }
}
