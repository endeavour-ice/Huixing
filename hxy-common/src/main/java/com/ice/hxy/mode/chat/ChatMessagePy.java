package com.ice.hxy.mode.chat;

import lombok.Data;

import java.util.Date;

/**
 * @Author ice
 * @Date 2023/5/21 17:10
 * @Description: 朋友消息体
 */
@Data
public class ChatMessagePy {
    private String uid;
    private String content;
    private String avatar;
    private Integer status;
    private Date time;
}
