package cn.com;

import cn.com.apollo.common.URI;
import cn.com.code.ApolloCodeC;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 消息解码类
 * Created by jiaming on 2019/4/27.
 */
public class MessageDecoder extends LengthFieldBasedFrameDecoder {

    private ApolloCodeC codeC;
    private static final String SERIAL_KEY = "kryo";

    public MessageDecoder(int maxFrameLength, int lengthFieldOffset,
                          int lengthFieldLength, URI uri) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
        this.codeC = new ApolloCodeC(uri);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object object = codeC.decoder(SERIAL_KEY, in);
        return object;
    }
}
