package com.ice.hxy.mode.chat;

import lombok.Data;

/**
 * @Author ice
 * @Date 2023/5/21 17:20
 * @Description: 推送的消息体
 */
@Data
public class SocketResp<T>{
    private Integer type;
    private T data;
}
