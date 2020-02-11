package cn.com;

import cn.com.apollo.common.URI;
import cn.com.code.ApolloCodeC;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by jiaming on 2019/4/27.
 */
public class MessageEncoder extends MessageToByteEncoder {

    private ApolloCodeC codeC;
    private static final String SERIAL_KEY = "kryo";

    public MessageEncoder(URI uri) {
        this.codeC = new ApolloCodeC(uri);
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object obj, ByteBuf byteBuf) throws Exception {
        if (obj == null) {
            throw new RuntimeException("the ecode CatMessage is null");
        }
        codeC.encoder(SERIAL_KEY, byteBuf, obj);
    }


}
