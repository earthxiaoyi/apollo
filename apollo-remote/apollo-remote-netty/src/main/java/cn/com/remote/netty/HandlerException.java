package cn.com.remote.netty;

/**
 * @author jiaming
 */
public class HandlerException extends RuntimeException {

    private int code;

    public HandlerException() {
    }

    public HandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public HandlerException(String message) {
        super(message);
    }

    public HandlerException(Throwable cause) {
        super(cause);
    }

    public HandlerException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
