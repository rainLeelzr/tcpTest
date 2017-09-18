package avatar.rain.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 发送数据给客户端的时，执行此类进行编码
 */
public class AvatarEncoder extends MessageToByteEncoder<TCPPacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, TCPPacket msg, ByteBuf out) throws Exception {
        out.writeInt(msg.getBytes().length);
        out.writeInt(msg.getCmd());
        out.writeInt(msg.getType());
        out.writeInt(msg.getUserId());
        out.writeInt(msg.getCode());
        out.writeBytes(msg.getBytes());
    }
}
