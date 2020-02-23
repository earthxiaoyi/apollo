package cn.com.apollo.common.exception;

/**
 * @author jiaming
 */
public class RemoteException extends RuntimeException {

    public static final int NETWORK_EXCEPTION = 0;

    public RemoteException(String message) {
        super(message);
    }

    public RemoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteException(Throwable cause) {
        super(cause);
    }

    public RemoteException(int code, String message, Throwable cause) {
        super(message, cause);
    }
}
