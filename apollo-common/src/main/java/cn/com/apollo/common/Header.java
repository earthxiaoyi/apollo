package cn.com.apollo.common;

/**
 * rpc head 协议定义如下：
 * <p>
 * 0-15     魔数字节MAGIC，0xca1100
 * 16-23    事件类型：0请求 1响应 2心跳
 * 24-31    状态：0success 1request_timeout 2response_timeout 3bad_request 4bad_response
 * 32-39    序列化协议：0 KryoSerialization
 * 40-103   请求id
 * 104-135  消息长度
 * <p>
 * Created by jiaming on 2019/4/21.
 */
public class Header {

    private short magic = Constant.MAGIC;
    private byte eventType;
    private byte status;
    private byte serialType;
    private long id;
    private int length;

    public short getMagic() {
        return magic;
    }

    public void setMagic(short magic) {
        this.magic = magic;
    }

    public byte getEventType() {
        return eventType;
    }

    public void setEventType(byte eventType) {
        this.eventType = eventType;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public byte getSerialType() {
        return serialType;
    }

    public void setSerialType(byte serialType) {
        this.serialType = serialType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
