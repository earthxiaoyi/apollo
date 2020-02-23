package cn.com.handler;

import cn.com.apollo.common.Request;
import cn.com.channel.Channel;
import cn.com.code.ApolloCodeC;

public class DecoderHandler implements Handler {

    private Handler handler;

    public DecoderHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void connect(Channel channel, Object msg) {
        handler.connect(channel, msg);
    }

    @Override
    public void disconnect(Channel channel, Object msg) {
        handler.disconnect(channel, msg);
    }

    @Override
    public void recevied(Channel channel, Object msg) {
        if (msg instanceof Request) {
            Request request = (Request) msg;
            Object data = ApolloCodeC.decodeBody("kryo", request.getData());
            request.setData(data);
            handler.recevied(channel, request);
        } else {
            throw new IllegalStateException("无法识别的消息");
        }
    }

    @Override
    public void sent(Channel channel, Object result) {
        handler.sent(channel, result);
    }

    @Override
    public void cause(Channel channel, Throwable cause) {
        handler.cause(channel, cause);
    }
}
