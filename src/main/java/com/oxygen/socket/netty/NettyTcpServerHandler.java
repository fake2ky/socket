package com.oxygen.socket.netty;

import com.oxygen.socket.constant.FrameType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;

import java.util.Optional;
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

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        // ctx.channel().remoteAddress()
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        server.values().removeIf(x -> x.equals(ctx));
        ctx.close();
    }

    private void parse(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        if (!FRAME_HEAD.equals(ByteBufUtil.hexDump(byteBuf, 0, 2))) {
            return;
        }
        String deviceId = getDeviceId(byteBuf);
        String frameType = ByteBufUtil.hexDump(byteBuf, 4, 1);
        switch (frameType) {
            case FrameType.RECEIVED_HEART:
                server.put(deviceId, ctx);
                byteBuf.setByte(4, 2);
                ctx.writeAndFlush(byteBuf);
                break;
        }
        // TODO: 需要封装
        // ctx.writeAndFlush(Unpooled.copiedBuffer("Netty received", Charset.defaultCharset()));
    }

    /**
     * 文档规则固定的获取唯一标识
     * @param byteBuf
     * @return
     */
    private String getDeviceId(ByteBuf byteBuf) {
        int start = 5, end = byteBuf.getByte(3) - 8;
        return ByteBufUtil.hexDump(byteBuf, start, end);
    }

    /**
     * 计算校验码
     * @param bytes
     * @return
     */
    private byte getXor(byte[] bytes){
        byte temp = 0;
        for (int i = 1; i < bytes.length; i++) {
            temp ^= bytes[i];
        }
        return temp;
    }

    /**
     * 向服务器发送开锁
     * @param deviceId hexString
     * @param userId hexString
     */
    public void unlock(String deviceId, String userId) {
        ByteBuf buffer = Unpooled.buffer();
        byte[] deviceIdBytes = ByteBufUtil.decodeHexDump(deviceId);
        byte[] userIdBytes = ByteBufUtil.decodeHexDump(userId);
        int length = 8 + deviceIdBytes.length + userIdBytes.length;
        byte[] bytes = {(byte) 0xa5, 0x5a, 0x00, (byte) length, 0x06};
        buffer.writeBytes(bytes);
        buffer.writeBytes(deviceIdBytes);
        buffer.writeBytes(userIdBytes);
        byte xor = getXor(buffer.copy(2, buffer.capacity()).array());
        buffer.writeBytes(new byte[]{xor, 0x0d, 0x0a});
        Optional.of(server.get(deviceId)).ifPresent(ctx -> {
            ctx.writeAndFlush(buffer);
        });
    }

}
