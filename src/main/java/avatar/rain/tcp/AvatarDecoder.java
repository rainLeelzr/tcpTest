package avatar.rain.tcp;

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
        // 约定一个完整的tcp包的数据长度至少为20字节
        if (in.readableBytes() < 20) {
            return;
        }

        in.markReaderIndex();

        // 除了头信息的数据body长度
        int length = in.readInt();

        if (length <= 0 || length > MAX_LENGTH) {

        }
        int cmd = in.readInt();
        int type = in.readInt();
        int userId = in.readInt();
        int code = in.readInt();
        if (code == 0) {//如果对序号有要求，那么默认将序号等于用户id
            code = userId;
        }

        // 如果body的长度少于头信息中的length，则是半包情况，重置此次tcp包的数据读取工作
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        // 读取一个包含完整body数据的包，如果有粘包，则不继续读取，让其在下一个循环事件中处理
        byte[] bytes = new byte[length];
        in.readBytes(bytes);

        // 将数据封装成一个完整的数据包
        out.add(new TCPPacket(length, cmd, type, userId, code, bytes));
    }
}
