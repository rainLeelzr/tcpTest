package avatar.rain.tcp;

import avatar.rain.LogUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.UnsupportedEncodingException;

/**
 * 网络数据包的基础结构
 */
public class TcpPacket {

    /**
     * 一个完整的tcp包至少的长度（字节）
     */
    public static final int AT_LEAST_LENGTH = 10;

    /**
     * 默认的编码解码字符串的字符集
     */
    public static final String DEFAULT_CHARSET = "utf-8";

    /**
     * body的长度（字节）
     */
    private int bodyLength;

    /**
     * url的长度（字节）
     */
    private int urlLength;

    /**
     * 请求的url
     */
    private byte[] url;

    /**
     * 用户id的长度（字节）
     */
    private byte userIdLength;

    /**
     * 用户id
     */
    private byte[] userId;

    /**
     * body数据的格式
     */
    private byte bodyType;

    /**
     * body数据
     */
    private byte[] body;

    public enum PackageMetadata {
        // body的长度（字节）
        BODY_LENGTH(int.class),

        // url的长度（字节）
        URL_LENGTH(int.class),
        // 请求的url
        URL(byte[].class),

        // 用户id的长度（字节）
        USER_ID_LENGTH(byte.class),
        // 用户id
        USER_ID(byte[].class),

        // body数据的格式
        BODY_TYPE(byte.class),
        // body数据
        BODY(byte[].class);

        private Class type;

        PackageMetadata(Class type) {
            this.type = type;
        }

        public Class getType() {
            return type;
        }
    }

    /**
     * tcp包的body部分的数据类型
     */
    public enum BodyType {

        PROTOBUF((byte) 0),
        JSON((byte) 1);

        private byte id;

        BodyType(byte id) {
            this.id = id;
        }

        public byte geId() {
            return id;
        }
    }

    public TcpPacket(int bodyLength, int urlLength, byte[] url, byte userIdLength, byte[] userId, byte bodyType, byte[] body) {
        this.bodyLength = bodyLength;
        this.urlLength = urlLength;
        this.url = url;
        this.userIdLength = userIdLength;
        this.userId = userId;
        this.bodyType = bodyType;
        this.body = body;
    }

    /**
     * 构建body类型为proto格式的tcp包
     *
     * @param url  url
     * @param body body的二进制数组数据
     */
    public static TcpPacket buildProtoPackage(String url, byte[] body) {
        return buildPackage(url, BodyType.PROTOBUF.id, body);
    }

    /**
     * 构建body类型为Json格式的tcp包
     *
     * @param url  url
     * @param body body的二进制数组数据
     */
    public static TcpPacket buildJsonPackage(String url, byte[] body) {
        return buildPackage(url, BodyType.JSON.id, body);
    }

    /**
     * 构建body类型为Json格式的tcp包
     *
     * @param url  url
     * @param body body的字符串数据
     */
    public static TcpPacket buildJsonPackage(String url, String body) {
        byte[] bodyBytes;
        try {
            bodyBytes = body.getBytes(DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            LogUtil.getLogger().error(e.getMessage(), e);
            bodyBytes = new byte[0];
        }
        return buildJsonPackage(url, bodyBytes);
    }

    /**
     * 构建tcp包
     *
     * @param url      url
     * @param bodyType body数据的格式
     * @param body     body数据
     */
    public static TcpPacket buildPackage(String url, byte bodyType, byte[] body) {
        byte[] urlBytes;
        try {
            urlBytes = url.getBytes(DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            LogUtil.getLogger().error(e.getMessage(), e);
            urlBytes = new byte[0];
        }
        return new TcpPacket(body.length, urlBytes.length, urlBytes, (byte) 0, new byte[0], bodyType, body);
    }

    /**
     * 服务器发送给客户端的数据包的格式
     */
    public ByteBuf getByteBuf() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeInt(this.bodyLength);
        byteBuf.writeInt(this.urlLength);
        byteBuf.writeBytes(this.url);
        byteBuf.writeByte(this.userIdLength);
        byteBuf.writeBytes(this.userId);
        byteBuf.writeByte(this.bodyType);
        byteBuf.writeBytes(this.body);
        return byteBuf;
    }

    public byte[] getUserId() {
        return userId;
    }

    public void setUserId(String userIdStr) {
        if (userIdStr == null) {
            this.userId = null;
            this.userIdStr = null;
            this.userIdLength = 0;
            return;
        }

        try {
            this.userId = userIdStr.getBytes(DEFAULT_CHARSET);
            this.userIdStr = userIdStr;
            this.userIdLength = (byte) this.userId.length;
        } catch (UnsupportedEncodingException e) {
            LogUtil.getLogger().error(e.getMessage(), e);
        }
    }

    public byte getBodyType() {
        return bodyType;
    }

    public byte[] getBody() {
        return body;
    }

    /**
     * url的字符串形式
     */
    private String urlStr;

    public String getUrlStr() {
        if (this.url == null) {
            return null;
        }

        if (this.urlStr == null) {
            try {
                urlStr = new String(this.url, DEFAULT_CHARSET);
            } catch (UnsupportedEncodingException e) {
                LogUtil.getLogger().error(e.getMessage(), e);
                urlStr = "";
            }
        }

        return this.urlStr;
    }

    /**
     * userId的字符串形式
     */
    private String userIdStr;

    public String getUserIdStr() {
        if (this.userId == null) {
            return null;
        }

        if (this.userIdStr == null) {
            try {
                userIdStr = new String(this.userId, DEFAULT_CHARSET);
            } catch (UnsupportedEncodingException e) {
                LogUtil.getLogger().error(e.getMessage(), e);
                userIdStr = "";
            }
        }

        return this.userIdStr;
    }

    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("\"bodyLength\":")
                .append(bodyLength)
                .append(",\"urlLength\":")
                .append(urlLength)
                .append(",\"userIdLength\":")
                .append(userIdLength)
                .append(",\"bodyType\":")
                .append(bodyType)
                .append(",\"urlStr\":\"")
                .append(getUrlStr()).append('\"')
                .append(",\"userIdStr\":\"")
                .append(getUserIdStr()).append('\"')
                .append('}')
                .toString();
    }
}
