package cn.com.apollo.common.exception;

/**
 * rpc exception
 *
 * @author jiaming
 */
public class RpcException extends RuntimeException {

    public static final int UNKNOWN_EXCEPTION = 0;
    public static final int BIZ_EXCEPTION = 1;
    public static final int TIMEOUT_EXCEPTION = 2;

    private int code;

    public RpcException() {
        super();
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }


}
