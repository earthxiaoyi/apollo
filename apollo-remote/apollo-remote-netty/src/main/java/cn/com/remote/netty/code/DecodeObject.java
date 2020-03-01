package cn.com.remote.netty.code;

/**
 * @author jiaming
 */
public class DecodeObject {

    private byte[] body;

    public DecodeObject(byte[] body) {
        this.body = body;
    }

    public byte[] getBody() {
        return body;
    }
}
