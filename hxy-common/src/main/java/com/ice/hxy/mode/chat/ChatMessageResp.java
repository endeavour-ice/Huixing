package com.ice.hxy.mode.chat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author ice
 * @Date 2023/5/21 20:31
 * @Description: 前端返回的消息体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResp {
    @ApiModelProperty("发送者信息")
    private UserInfo userInfo;
    @ApiModelProperty("消息详情")
    private Message message;
    @Data
    public static class UserInfo{
        @ApiModelProperty("用户名称")
        private String username;
        @ApiModelProperty("用户id")
        private Long uid;
        @ApiModelProperty("头像")
        private String avatar;
    }

    @Data
    public static class Message{
        @ApiModelProperty("消息id")
        private Long id;
        @ApiModelProperty("消息发送时间")
        private long sendTime;
        @ApiModelProperty("消息内容")
        private String content;
        @ApiModelProperty("消息的状态")
        private Integer status;

        @ApiModelProperty("父消息，如果没有父消息，返回的是null")
        private ReplyMsg reply;
    }
    @Data
    public static class ReplyMsg {
        @ApiModelProperty("消息id")
        private Long id;
        @ApiModelProperty("用户名称")
        private String username;
        @ApiModelProperty("消息内容")
        private String content;
        @ApiModelProperty("是否可消息跳转 0否 1是")
        private Integer canCallback;
        @ApiModelProperty("跳转间隔的消息条数")
        private Integer gapCount;
    }
}
