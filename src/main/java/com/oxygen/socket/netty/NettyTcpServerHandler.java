package com.oxygen.socket.netty;

import com.oxygen.socket.constant.FrameType;
import com.oxygen.socket.model.BdmCabinetHeartbeat;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Date;
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

    private final int OK = 1;
//    @Autowired
//    private BdmCabinetHeartbeatDao bdmCabinetHeartbeatDao;

    private final ConcurrentHashMap<String, ChannelHandlerContext> server = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        parse(ctx, msg);
    }

    private void parse(ChannelHandlerContext ctx, Object msg) {
        //获取客户端的IP和端口
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIP = insocket.getAddress().getHostAddress();
        //强转Server与Client之间通信的数据传输载体(Netty的数据容器)
        ByteBuf byteBuf = (ByteBuf) msg;
        if (!FRAME_HEAD.equals(ByteBufUtil.hexDump(byteBuf, 0, 2))) {
            return;
        }
        //构建心跳实体类
        BdmCabinetHeartbeat heartbeat = new BdmCabinetHeartbeat();
        String code ;
        String id = getId(byteBuf);
        String frameType = ByteBufUtil.hexDump(byteBuf, 4, 1);
        switch (frameType) {
            case FrameType.RECEIVED_HEART:
                server.putIfAbsent(id, ctx);
                heartbeat.setIp(clientIP);//客户端IP
                heartbeat.setPort(insocket.getPort());//客户端端口
                code = ByteBufUtil.hexDump(byteBuf, 5, 8);
                //if (code.contains("b")) {
                //414b500000000001 转为大写 414B500000000001
                code = code.toUpperCase();
                heartbeat.setNumber(code);//柜子编号
                //}
                heartbeat.setStatus(OK);//状态：1-正常，2-异常
                heartbeat.setCreateTime(new Date());//发送心跳时间
                //将心跳信息保存到数据库中
                //bdmCabinetHeartbeatDao.insert(heartbeat);
                //回应客户端
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
        String length = ByteBufUtil.hexDump(byteBuf, 3, 2);
        int start = 6, end = Byte.parseByte(length) * 0xff;
        System.out.println("start -->: "+start + ", end -->:"+end);
        System.out.println("====>"+ByteBufUtil.hexDump(byteBuf, start, end));
        return ByteBufUtil.hexDump(byteBuf, start, end);
    }
}
