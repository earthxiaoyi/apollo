package cn.com.apollo.common;

import java.io.Serializable;

public class Result implements Serializable {

    private static final long serialVersionUID = -1L;

    private Throwable exception;
    private Object data;

    public Result() {
    }

    public Result(Throwable exception) {
        this.exception = exception;
    }

    public Result(Object data) {
        this.data = data;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public boolean hasException() {
        return exception != null;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
