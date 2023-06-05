package com.ice.hxy.mode.entity.vo;

import com.ice.hxy.mode.chat.ChatMessageResp;
import lombok.Data;

import java.util.List;

/**
 * @Author ice
 * @Date 2023/3/21 10:59
 * @Description: TODO
 */
@Data
public class ChatResp {
    private UserAvatarVo userVo;
    private List<ChatMessageResp> chat;
    private Long cursor;
}
