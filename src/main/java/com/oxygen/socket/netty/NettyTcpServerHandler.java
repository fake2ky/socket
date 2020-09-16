package com.oxygen.socket.netty;

import com.oxygen.socket.constant.FrameType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * 数据帧传输格式
 * 0xA5  0x5A  0x00  0x0A  0x43  0x01  0x25  CHK  0x0d  0x0a
 * CHK = 0x00 ^ 0x0A ^ 0x43 ^ 0x01 ^ 0x25 = 0x6D
 * 心跳 a55a001001414b50000000001xx0d0a
 */
@Component
@ChannelHandler.Sharable
public class NettyTcpServerHandler extends ChannelInboundHandlerAdapter {

    private final String FRAME_HEAD = "a55a";

//    private final int OK = 1;
//    @Autowired
//    private BdmCabinetHeartbeatDao bdmCabinetHeartbeatDao;
//    private final ConcurrentHashMap<String, ChannelHandlerContext> server = new ConcurrentHashMap<>();

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //channel失效，从Map中移除
        NettyChannelMap.remove(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //强转Server与Client之间通信的数据传输载体(Netty的数据容器)
        ByteBuf byteBuf = (ByteBuf) msg;
        if (!FRAME_HEAD.equals(ByteBufUtil.hexDump(byteBuf, 0, 2))) {
            return;
        }
        String frameType = ByteBufUtil.hexDump(byteBuf, 4, 1);
        switch (frameType) {
            case FrameType.RECEIVED_HEART:
                receivedHeart(ctx, byteBuf);
                break;
            case FrameType.SEND_CLOSE_LOCK_ORDER:
                sendLockStatusOrder(ctx, byteBuf);
                break;
        }
    }


    //收到心跳
    private void receivedHeart(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        //获取客户端的IP和端口
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIP = insocket.getAddress().getHostAddress();
        String code = ByteBufUtil.hexDump(byteBuf, 5, 8).toUpperCase();
        NettyChannelMap.add(code, ctx);
        //回应客户端
        byteBuf.setByte(4, FrameType.SEND_HEART.hashCode());
        ctx.writeAndFlush(byteBuf);
    }


    //服务器向柜子发送的锁状态指令
    private void sendLockStatusOrder(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        //回应客户端
        byteBuf.setByte(4, FrameType.SELECT_LOCK_STATUS.hashCode());
        ctx.writeAndFlush(byteBuf);

    }
}
