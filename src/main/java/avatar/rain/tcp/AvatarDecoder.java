package avatar.rain.tcp;

import avatar.rain.LogUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 收到客户端的数据后，执行此类进行数据解码
 */
public class AvatarDecoder extends ByteToMessageDecoder {
    private static final short MAX_LENGTH = 2048;

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        LogUtil.getLogger().debug("收到网络包，长度：{}", in.readableBytes());
        // 约定一个完整的tcp包的数据长度至少为10字节
        if (in.readableBytes() < TcpPacket.AT_LEAST_LENGTH) {
            return;
        }

        in.markReaderIndex();

        // body的长度（字节）
        int bodyLength = in.readInt();
        // todo 阻止大tcp包的发送

        if (bodyLength != 0 && in.readableBytes() < TcpPacket.AT_LEAST_LENGTH - 4 + bodyLength) {
            in.resetReaderIndex();
            return;
        }

        // url的长度（字节）
        int urlLength = in.readInt();
        if (urlLength != 0 && in.readableBytes() < TcpPacket.AT_LEAST_LENGTH - 8 + bodyLength + urlLength) {
            in.resetReaderIndex();
            return;
        }

        // 请求的url
        byte[] url = new byte[urlLength];
        if (urlLength > 0) {
            in.readBytes(url);
        }

        // 用户id的长度（字节）
        byte userIdLength = in.readByte();
        if (userIdLength != 0 && in.readableBytes() < userIdLength + 1 + bodyLength) {
            in.resetReaderIndex();
            return;
        }

        // 用户id
        byte[] userId = new byte[userIdLength];
        if (userIdLength > 0) {
            in.readBytes(userId);
        }

        // body数据的格式
        byte bodyType = in.readByte();

        // body数据
        byte[] body = new byte[bodyLength];
        if (bodyLength > 0) {
            in.readBytes(body);
        }

        // 将数据封装成一个完整的数据包
        out.add(new TcpPacket(bodyLength, urlLength, url, userIdLength, userId, bodyType, body));
    }
}
