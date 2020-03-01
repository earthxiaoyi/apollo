package cn.com.remote.netty.event;

import cn.com.remote.netty.channel.Channel;
import cn.com.remote.netty.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelEventHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ChannelEventHandler.class);

    private Channel channel;
    private Handler handler;
    private Object msg;
    private ChannelEventState state;
    private Throwable cause;

    public ChannelEventHandler(Channel channel, Handler handler, Object msg, ChannelEventState state) {
        this.channel = channel;
        this.handler = handler;
        this.msg = msg;
        this.state = state;
    }

    public ChannelEventHandler(Channel channel, Handler handler, Object msg, ChannelEventState state, Throwable cause) {
        this.channel = channel;
        this.handler = handler;
        this.msg = msg;
        this.state = state;
        this.cause = cause;
    }

    @Override
    public void run() {
        if (state == ChannelEventState.RECEIVED) {
            //解码request body
            handler.recevied(channel, msg);
        } else {
            switch (state) {
                case CONNECTED: {
                    handler.connect(channel, msg);
                    break;
                }
                case DISCONNECTED: {
                    handler.disconnect(channel, msg);
                    break;
                }
                case CAUGHT: {
                    handler.cause(channel, cause);
                    break;
                }
                case SENT: {
                    handler.sent(channel, msg);
                    break;
                }
                default: {
                    log.warn("识别不了的网络连接状态");
                    break;
                }
            }
        }
    }

    public enum ChannelEventState {
        CONNECTED,
        DISCONNECTED,
        SENT,
        RECEIVED,
        CAUGHT
    }
}
