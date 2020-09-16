package com.oxygen.socket.netty;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

public class NettyChannelMap {

    private static ConcurrentHashMap<String, ChannelHandlerContext> map = new ConcurrentHashMap<String, ChannelHandlerContext>();

    public static void add(String clientId, ChannelHandlerContext ctx) {
        map.put(clientId, ctx);
    }

    public static ChannelHandlerContext get(String clientId) {
        return map.get(clientId);
    }

    public static void remove(ChannelHandlerContext ctx) {
        map.values().removeIf(x -> x.equals(ctx));
        ctx.close();
//        for (Map.Entry entry : map.entrySet()) {
//            if (entry.getValue() == ctx) {
//                map.remove(entry.getKey());
//            }
//        }
    }
}
