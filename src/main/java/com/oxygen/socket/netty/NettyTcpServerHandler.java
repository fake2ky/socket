package com.oxygen.socket.netty;

import com.oxygen.socket.constant.FrameType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 数据帧传输格式
 * 0xA5  0x5A  0x00  0x0A  0x43  0x01  0x25  CHK  0x0d  0x0a
 * CHK = 0x00 ^ 0x0A ^ 0x43 ^ 0x01 ^ 0x25 = 0x6D
 * 心跳 a55a001001414b50000000001xx0d0a
 */
public class NettyTcpServerHandler extends ChannelInboundHandlerAdapter {

    private final String FRAME_HEAD = "a55a";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        parse(ctx, msg);
    }

    private void parse(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        if(!FRAME_HEAD.equals(ByteBufUtil.hexDump(byteBuf, 0, 2))){
            return;
        }

        String frameType = ByteBufUtil.hexDump(byteBuf, 4, 1);
        switch (frameType){
            case FrameType.RECEIVED_HEART:
                byteBuf.setByte(4, 2);
                ctx.writeAndFlush(byteBuf);
                break;
        }
        // TODO: 需要封装
        // ctx.writeAndFlush(Unpooled.copiedBuffer("Netty received", Charset.defaultCharset()));
    }
}
