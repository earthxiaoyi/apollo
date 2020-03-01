package cn.com.remote.netty.code;

import cn.com.remote.netty.NettyChannel;
import cn.com.apollo.common.*;
import cn.com.apollo.serialize.Serializer;
import cn.com.apollo.serialize.kryo.KryoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * @author jiaming
 */
public class NettyCodec {

    private static final Logger logger = LoggerFactory.getLogger(NettyCodec.class);

    private final Decoder decoder = new Decoder();
    private final Encoder encoder = new Encoder();

    private final URI uri;

    private static final String SERIAL_KEY = "kryo";
    private static final int HEADER_LENGTH = 17;
    private static final byte MAGIC_LOW = 0x11;
    private static final byte MAGIC_HIGH = 0x22;
    private static final byte FLAG_REQUEST_TYPE = Constant.REQUEST;
    private static final byte FLAG_RESPONSE_TYPE = Constant.RESPONSE;
    private static final byte FLAG_STATUS = Constant.SUCCESS;
    private static final byte FLAG_SERIAL_TYPE = Constant.KRYO_SERIALIZE;

    public NettyCodec(URI uri) {
        this.uri = uri;
    }

    public Decoder getDecoder() {
        return decoder;
    }

    public Encoder getEncoder() {
        return encoder;
    }

    class Decoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf buffer, List<Object> list) throws Exception {
            //decodeHeader
            Channel channel = channelHandlerContext.channel();
            try {
                do {
                    int saveReaderIndex = buffer.readerIndex();
                    Object msg = decode(buffer);
                    if (msg == DecodeEnum.NEED_MORE) {
                        buffer.readerIndex(saveReaderIndex);
                        break;
                    } else {
                        if (msg != null) {
                            list.add(msg);
                        }
                    }
                } while (buffer.isReadable());
            } finally {
                NettyChannel.removeChannelIfDisconnected(channel);
            }
        }

        private Object decode(ByteBuf buffer) {
            int readable = buffer.readableBytes();
            //need more msg
            if (readable < HEADER_LENGTH) {
                return DecodeEnum.NEED_MORE;
            }
            byte[] header = new byte[Math.min(readable, HEADER_LENGTH)];
            //read header
            buffer.readBytes(header);
            if (header[0] != MAGIC_LOW && header[1] != MAGIC_HIGH) {
                return null;
            }
            //data length
            int length = Bytes.bytes2int(header, 13);
            int ll = length + HEADER_LENGTH;
            if (readable < ll) {
                return DecodeEnum.NEED_MORE;
            }
            Object msg;
            try {
                msg = decodeBody(buffer, header, length);
            } finally {
                //skip
                int available = ll - buffer.readerIndex();
                if (ll - buffer.readerIndex() > 0) {
                    buffer.skipBytes(available);
                }
            }
            return msg;
        }

        private Object decodeBody(ByteBuf buffer, byte[] header, int length) {
            byte type = header[2];
            byte status = header[3];
            byte serialType = header[4];
            long id = Bytes.bytes2long(header, 5);
            Serializer serializer = getSerializer(SERIAL_KEY);
            Header h = new Header();
            h.setId(id);
            h.setEventType(type);
            h.setStatus(status);
            h.setLength(length + HEADER_LENGTH);
            h.setSerialType(serialType);
            if (type == FLAG_REQUEST_TYPE) {
                //request
                Request request = new Request();
                request.setHeader(h);
                byte[] body = new byte[length];
                buffer.readBytes(body, 0, length);
                Object data;
                if (uri.getParameter(Constant.IO_DECODER, Constant.IO_DECODER_DEFAULT_VALUE)) {
                    InputStream in = new ByteArrayInputStream(body);
                    data = serializer.deserialize(in);
                } else {
                    data = new DecodeObject(body);
                }
                request.setData(data);
                return request;
            } else {
                //response
                Response response = new Response();
                response.setHeader(h);
                byte[] body = new byte[length];
                buffer.readBytes(body, 0, length);
                Object data;
                InputStream in = new ByteArrayInputStream(body);
                data = serializer.deserialize(in);
                response.setData(data);
                return response;
            }
        }

    }

    class Encoder extends MessageToByteEncoder {

        private void encodeRequest(Request request, ByteBuf byteBuf) throws IOException {
            Header h = request.getHeader();
            byte[] header = new byte[HEADER_LENGTH];
            //MAGIC
            header[0] = MAGIC_LOW;
            header[1] = MAGIC_HIGH;
            //message type
            header[2] = FLAG_REQUEST_TYPE;
            header[3] = FLAG_STATUS;
            header[4] = FLAG_SERIAL_TYPE;
            //id
            long requestId = h.getId();
            Bytes.long2bytes(requestId, header, 5);
            //set write index
            int saveWriteIndex = byteBuf.writerIndex();
            byteBuf.writerIndex(saveWriteIndex + HEADER_LENGTH);
            int startIndex = byteBuf.writerIndex();
            //encode request data
            Serializer serializer = getSerializer(SERIAL_KEY);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            OutputStream out = serializer.serialize(request.getData(), outputStream);
            encodeRequestData(byteBuf, outputStream);
            //clear
            out.close();
            outputStream.flush();
            outputStream.close();
            //length
            int len = byteBuf.writerIndex() - startIndex;
            Bytes.int2bytes(len, header, 13);
            //write
            byteBuf.writerIndex(saveWriteIndex);
            byteBuf.writeBytes(header);
            byteBuf.writerIndex(saveWriteIndex + len + HEADER_LENGTH);
        }

        private void encodeRequestData(ByteBuf buffer, ByteArrayOutputStream outputStream) {
            byte[] bytes = outputStream.toByteArray();
            buffer.writeBytes(bytes);
        }

        private void encodeResponseData(ByteBuf buffer, ByteArrayOutputStream outputStream) {
            byte[] bytes = outputStream.toByteArray();
            buffer.writeBytes(bytes);
        }

        private void encodeResponse(Response response, ByteBuf byteBuf) throws IOException {
            Header h = response.getHeader();
            byte[] header = new byte[HEADER_LENGTH];
            //MAGIC

            header[0] = MAGIC_LOW;
            header[1] = MAGIC_HIGH;
            //message type
            header[2] = FLAG_RESPONSE_TYPE;
            header[3] = h.getStatus();
            header[4] = FLAG_SERIAL_TYPE;
            //id
            long responseId = h.getId();
            Bytes.long2bytes(responseId, header, 5);
            //set write index
            int saveWriteIndex = byteBuf.writerIndex();
            byteBuf.writerIndex(saveWriteIndex + HEADER_LENGTH);
            int startIndex = byteBuf.writerIndex();
            //encode request data
            Serializer serializer = getSerializer(SERIAL_KEY);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            OutputStream out = serializer.serialize(response.getData(), outputStream);
            encodeResponseData(byteBuf, outputStream);
            //clear
            out.close();
            out.flush();
            outputStream.flush();
            outputStream.close();
            //length
            int len = byteBuf.writerIndex() - startIndex;
            Bytes.int2bytes(len, header, 13);
            //write
            byteBuf.writerIndex(saveWriteIndex);
            byteBuf.writeBytes(header);
            byteBuf.writerIndex(saveWriteIndex + len + HEADER_LENGTH);
        }

        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf byteBuf) throws Exception {
            if (msg instanceof Request) {
                Request request = (Request) msg;
                encodeRequest(request, byteBuf);
            } else if (msg instanceof Response) {
                Response response = (Response) msg;
                encodeResponse(response, byteBuf);
            } else {
                //event
            }
        }
    }

    public static Serializer getSerializer(String serialKey) {
        Serializer serializer = null;
        if ("kryo".equals(serialKey)) {
            serializer = new KryoSerializer();
        }
        return serializer;
    }
}
