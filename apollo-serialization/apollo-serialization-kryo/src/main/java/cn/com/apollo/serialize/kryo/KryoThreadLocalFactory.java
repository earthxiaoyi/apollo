package cn.com.apollo.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;

/**
 * Created by jiaming on 2019/4/21.
 */
public class KryoThreadLocalFactory {

    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return KryoUtils.create();
        }
    };

    public static Kryo getKryo(){
        return KRYO_THREAD_LOCAL.get();
    }

}
