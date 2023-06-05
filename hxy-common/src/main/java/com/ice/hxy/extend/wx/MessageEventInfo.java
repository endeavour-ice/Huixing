package com.ice.hxy.extend.wx;

import lombok.Data;

@Data
public class MessageEventInfo {

    /*
    {"Content":"急急急","CreateTime":"1679662222","ToUserName":"gh_53c62bde0354","FromUserName":"o-B0h5hX4ifDiilN-tNnBVlt8EO4","MsgType":"text","MsgId":"24046983601929067"}
     */
    /**
     * 用户openid
     */
    private String FromUserName;
    /**
     * 消息类型
     */
    private String MsgType;
    /**
     * 事件类型
     */
    private String Event;
    /**
     * 事件KEY值，获取二维码时的scene_id
     */
    private String EventKey;
    /**
     * 二维码的Ticket
     */
    private String Ticket;
}
