package cn.com.apollo.serialize.kryo;

import cn.com.apollo.serialize.Serializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 消息编码器
 */
public class KryoSerializer implements Serializer {

    @Override
    public void serialize(Object t, OutputStream outputStream) {
        Kryo kryo = KryoThreadLocalFactory.getKryo();
        Output output = new Output(outputStream);
        kryo.writeClassAndObject(output, t);
        output.flush();
    }

    @Override
    public <T> T deserialize(InputStream inputStream) {
        Kryo kryo = KryoThreadLocalFactory.getKryo();
        Input input = new Input(inputStream);
        return (T) kryo.readClassAndObject(input);
    }

}
