package avatar.rain.tcp;

import java.util.Arrays;

/**
 * 网络数据包的基础结构
 */
public class TCPPacket {

    private int len;//包长度

    private int type;//包类型. 0 protobuff ; 1 json

    private int cmd;//对应具体命令（反射到具体函数中）

    private int userId;

    private int code;//序号。可以根据序号来分配工人线程处理业务逻辑

    private byte[] bytes;// 业务数据

    public int getType() {
        return type;
    }

    public int getCmd() {
        return cmd;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getUserId() {
        return userId;
    }

    public int getCode() {
        return code;
    }

    TCPPacket(int len, int cmd, int type, int userId, int code, byte[] bytes) {
        this.len = len;
        this.cmd = cmd;
        this.type = type;
        this.userId = userId;
        this.code = code;
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("\"len\":")
                .append(len)
                .append(",\"type\":")
                .append(type)
                .append(",\"cmd\":")
                .append(cmd)
                .append(",\"userId\":")
                .append(userId)
                .append(",\"code\":")
                .append(code)
                .append(",\"bytes\":")
                .append(Arrays.toString(bytes))
                .append('}')
                .toString();
    }
}
