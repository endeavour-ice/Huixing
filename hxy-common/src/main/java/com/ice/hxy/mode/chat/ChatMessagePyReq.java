package com.ice.hxy.mode.chat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @Author ice
 * @Date 2023/5/21 20:37
 * @Description: TODO
 */
@Data
public class ChatMessagePyReq {
    @NotNull
    @Length( max = 10000,message = "消息内容过长")
    @ApiModelProperty("消息内容")
    private String content;

    private Long acceptId;

    @ApiModelProperty("回复的消息id,如果没有别传就好")
    private Long replyMsgId;
}
