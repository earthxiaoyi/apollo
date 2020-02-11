package cn.com.apollo.common;

/**
 * @author jiaming
 */
public class Constant {

    public static final int MAX_PAYLOAD = 8 * 1024 * 1024;

    public static final short MAGIC = (short) 0xca1100;

    public static final byte REQUEST = 0;

    public static final byte RESPONSE = 1;

    public static final int DEFAULT_HEARTBEAT_TIMEOUT = 60 * 1000;

    public static final String HEARTBEAT_TIMEOUT = "heartbeat.timeout";

    public static final String VERSION = "version";

    public static final String VERSION_DEFUALT_VALUE = "0.0.0";

    public static final int PORT = 20020;

    public static final byte KRYO_SERIALIZE = 0;

    public static final int HEADER_LENGTH = 17;

    public static final int MSG_LENGTH_OFFSET = 13;

    public static final String TIME_OUT = "timeout";

    public static final int TIMEOUT = 5000;

    public static final int CONNECT_TIMEOUT = 5000;

    public static final String WEIGHT = "weight";

    public static final int DEFAULT_WEIGHT = 100;

    public static final byte SUCCESS = 0;

    public static final byte REQUEST_TIMEOUT = 1;

    public static final byte RESPONSE_TIMEOUT = 2;

    public static final byte BAD_REQUEST = 3;

    public static final byte BAD_RESPONSE = 4;

    public static final int RETRY_NUMS = 2;

    public static final String RETRY_KEY = "retry";

    public static final String NAME_SERVICE_PATH = "apollo";

    public static final String CONSUMER = "consumer";

    public static final String PROVIDER = "provider";

    public static final int NAME_SERVICE_TIMEOUT = 5000;

    public static final String GROUP = "group";

    public static final String GROUP_DEFAULT_VALUE = "default";

    public static final String CONNECT = "connect";

    public static final int DEFUAL_CONNECT = 1;

    public static final String IO_DECODER = "io_decoder";

    public static final boolean IO_DECODER_DEFAULT_VALUE = false;

    public static final String IO_THREAD = "io_thread";

    public static final int IO_THREAD_DEFAULT_NUM = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);

    public static final String DEFAULT_REMOVE_KEY = "-";

    public static final String PROVIDER_FILTER = "provider.cn.com.apollo.rpc.filter";

    public static final String CONSUMER_FILTER = "consumer.cn.com.apollo.rpc.filter";

    public static final String TPS_FILTER_KEY = "tps";

    public static final String TPS_INTERVAL_KEY = "tps.interval";
    public static final long TPS_INTERVAL_NUM = 60 * 1000;


}
