package com.oxygen.socket.controller;

import com.oxygen.socket.netty.NettyTcpServerHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final NettyTcpServerHandler nettyTcpServerHandler;

    @GetMapping("unlock")
    public Object unlock(String deviceId){
        nettyTcpServerHandler.unlock(deviceId, "01");
        return Boolean.TRUE;
    }

}
