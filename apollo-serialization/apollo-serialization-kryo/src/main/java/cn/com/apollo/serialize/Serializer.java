package cn.com.apollo.serialize;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jiaming on 2019/4/20.
 */
public interface Serializer {

    OutputStream serialize(Object t, OutputStream outputStream);

    <T> T deserialize(InputStream inputStream);

}
