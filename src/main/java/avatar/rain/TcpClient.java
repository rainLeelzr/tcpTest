package avatar.rain;

import avatar.rain.im.protobuf.IM;
import avatar.rain.tcp.AvatarDecoder;
import avatar.rain.tcp.AvatarEncoder;
import avatar.rain.tcp.PacketBodyType;
import avatar.rain.tcp.TCPPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.TimeUnit;

public class TcpClient {

    public void start(String addr, int port) throws Exception {
        final EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .handler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decoder", new AvatarDecoder());
                            pipeline.addLast(new ChannelInboundHandlerAdapter() {

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    // 新的channel激活时，绑定channel与session的关系
                                    Channel channel = ctx.channel();

                                    LogUtil.getLogger().debug("客户端接收到服务器的连接，服务器ip：{}", channel.remoteAddress());

                                    super.channelRegistered(ctx);

                                    new Thread(() -> {
                                        try {
                                            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
                                        } catch (InterruptedException e) {
                                            LogUtil.getLogger().error(e.getMessage(), e);
                                        }
                                        int cmd = 1;
                                        int type = PacketBodyType.Proto.getType();
                                        int userId = 3;
                                        int code = 4;

                                        try {
                                            IM.SendTextToUserC2S.Builder sendTextToUserC2S = IM.SendTextToUserC2S.newBuilder().setToUserId(10).setMessage("hello你好！");
                                            byte[] bytes = sendTextToUserC2S.build().toByteArray();
                                            // byte[] bytes = {2, 3};
                                            // String json = "{\"toUserId\": 10,\"message\": \"hello你好！\",\"user\": {\"id\": \"ididid\",\"account\": \"acc\",\"pwd\": \"p\",\"createTime\": 22222,\"status\": 2}}";
                                            // byte[] bytes = json.getBytes("utf-8");

                                            ByteBuf byteBuf = Unpooled.buffer();
                                            byteBuf.writeInt(bytes.length);
                                            byteBuf.writeInt(cmd);// cmd
                                            byteBuf.writeInt(type);// type
                                            byteBuf.writeInt(userId);// userId
                                            byteBuf.writeInt(code);// code
                                            byteBuf.writeBytes(bytes);
                                            ChannelFuture channelFuture = channel.writeAndFlush(byteBuf);
                                            channelFuture.addListener((ChannelFutureListener) future -> {
                                                LogUtil.getLogger().debug(
                                                        "[发送给服务器成功]bodyLen={}, cmd={}, type={}, userId={}, code={}, data={}",
                                                        bytes.length,
                                                        cmd,
                                                        type,
                                                        userId,
                                                        code,
                                                        bytes);
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }).start();

                                }

                                @Override
                                public void channelRead(ChannelHandlerContext cx, Object object) {
                                    TCPPacket packet = (TCPPacket) object;

                                    LogUtil.getLogger().debug("收到服务端消息：{}", packet.toString());
                                }


                                @Override
                                public void channelReadComplete(ChannelHandlerContext ctx) {
                                    ctx.flush();
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    if (!"远程主机强迫关闭了一个现有的连接。".equals(cause.getMessage())) {
                                        LogUtil.getLogger().error(cause.getMessage(), cause);
                                    }
                                    if (cause instanceof java.io.IOException)
                                        return;
                                    ctx.close();
                                }

                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    LogUtil.getLogger().info("userEventTriggered");
                                    super.userEventTriggered(ctx, evt);
                                    if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
                                        IdleStateEvent event = (IdleStateEvent) evt;
                                        // todo 分发用户下线事件
                                        if (event.state() == IdleState.ALL_IDLE) {
                                            LogUtil.getLogger().debug("tcp超时没有读写操作");
                                            ctx.channel().close();
                                        }
                                    }
                                }

                                @Override
                                public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                                    ctx.fireChannelUnregistered();
                                }

                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    super.channelInactive(ctx);
                                    LogUtil.getLogger().debug("成功关闭了一个tcp连接：{}", ctx.channel().remoteAddress());
                                }

                            });
                            pipeline.addLast("encoder", new AvatarEncoder());
                        }
                    });
            ChannelFuture f = b.connect(addr, port).sync();
            LogUtil.getLogger().info("连接服务器成功:{},本地地址:{}", f.channel().remoteAddress(), f.channel().localAddress());
            f.channel().closeFuture().sync();//等待客户端关闭连接
        } catch (Exception e) {
            LogUtil.getLogger().error(e.getMessage(), e);
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        TcpClient tcpClient = new TcpClient();
        tcpClient.start("127.0.0.1", 8135);
    }

}
