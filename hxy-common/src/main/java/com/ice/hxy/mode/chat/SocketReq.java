package com.ice.hxy.mode.chat;

import lombok.Data;

/**
 * @Author ice
 * @Date 2023/5/23 16:23
 * @Description: 前端发送的内容
 */
@Data
public class SocketReq {
    private Integer type;
    private Object data;
}
