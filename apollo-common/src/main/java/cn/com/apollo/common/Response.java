package cn.com.apollo.common;

import java.io.Serializable;

/**
 * Created by jiaming on 2019/7/7.
 */
public class Response implements Serializable {

    private static final long serialVersionUID = -1L;

    private Header header;
    private Object data;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}