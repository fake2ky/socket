package com.oxygen.socket.constant;

public interface FrameType {

    /**
     * 收到保持心跳
     */
    String RECEIVED_HEART = "01";

    /**
     * 应答保持心跳
     */
    String SEND_HEART = "02";

    String RECEIVED_UNLOCK = "06";
}
