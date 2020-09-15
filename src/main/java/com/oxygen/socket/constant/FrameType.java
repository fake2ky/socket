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

    /**
     * 服务器向柜子发开锁指令
     */
    String SEND_OPEN_LOCK_ORDER = "05";

    /**
     * 柜子向服务器开锁应答
     */
    String SEND_OPEN_LOCK_RESPOND = "06";

    /**
     * 柜体上传用户取出文具信息
     */
    String GET_PRODUCT = "08";

    /**
     * 服务器收到柜体上传用户取出文具信息应答
     */
    String SEND_PRODUCT_RESPOND = "09";
    /**
     * 柜体向服务器发送用户关锁指令
     */
    String SEND_CLOSE_LOCK_ORDER = "0c";

    /**
     * 服务器收到用户已关锁信息应答
     */
    String CLOSE_LOCK_RESPOND = "0D";
    /**
     * 服务器向柜子查询锁状态
     */
    String SELECT_LOCK_STATUS = "10";

    /**
     * 柜体收到服务器向柜子查询锁状态应答
     */
    String LOCK_STATUS_RESPOND = "11";
    /**
     * 服务器向柜体发送盘点指令
     */
    String SEND_TAKE_STOCK_ORDER = "12";

    /**
     * 柜体收到服务器要求后，柜子进行盘点并发送盘点数据应答
     */
    String TAKE_STOCK_RESPOND = "13";

    /**
     * 服务器应答柜子发送盘点数据
     */
    String SEND_TAKE_STOCK_DATA = "14";

}
