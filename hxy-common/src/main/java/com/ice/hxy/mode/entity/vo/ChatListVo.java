package com.ice.hxy.mode.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author ice
 * @Date 2023/4/24 12:50
 * @Description: 返回聊天列表
 */
@Data
public class ChatListVo {
    private String id;
    private Long userId;
    private LocalDateTime time;
    private String message;
}
