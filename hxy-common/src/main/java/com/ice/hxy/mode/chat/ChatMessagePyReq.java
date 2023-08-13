package com.ice.hxy.mode.chat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author ice
 * @Date 2023/5/21 20:37
 * @Description: TODO
 */
@Data
public class ChatMessagePyReq {

    @ApiModelProperty("消息内容")
    private String content;

    private Long acceptId;

    @ApiModelProperty("回复的消息id,如果没有别传就好")
    private Long replyMsgId;
}
