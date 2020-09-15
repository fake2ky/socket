package com.oxygen.socket.netty;

import com.oxygen.socket.constant.FrameType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

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

    private final ConcurrentHashMap<String, ChannelHandlerContext> server = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        parse(ctx, msg);
    }

    private void parse(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        if (!FRAME_HEAD.equals(ByteBufUtil.hexDump(byteBuf, 0, 2))) {
            return;
        }
        String id = getId(byteBuf);
        String frameType = ByteBufUtil.hexDump(byteBuf, 4, 1);
        switch (frameType) {
            case FrameType.RECEIVED_HEART:
                server.putIfAbsent(id, ctx);
                byteBuf.setByte(4, 2);
                ctx.writeAndFlush(byteBuf);
                break;
        }
        // TODO: 需要封装
        // ctx.writeAndFlush(Unpooled.copiedBuffer("Netty received", Charset.defaultCharset()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        server.values().removeIf(x -> x.equals(ctx));
        ctx.close();
    }

    /**
     * 文档规则固定的获取唯一标识
     * @param byteBuf
     * @return
     */
    private String getId(ByteBuf byteBuf) {
        String length = ByteBufUtil.hexDump(byteBuf, 2, 1);
        int start = 6, end = Byte.parseByte(length) * 0xff;;
        return ByteBufUtil.hexDump(byteBuf, start, end);
    }
}
