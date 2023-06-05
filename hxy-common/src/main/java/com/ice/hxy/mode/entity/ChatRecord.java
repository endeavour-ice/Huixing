package com.ice.hxy.mode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 聊天记录表
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
@TableName("chat_record")
@ApiModel(value = "ChatRecord对象", description = "聊天记录表")
@Data
public class ChatRecord implements Serializable {


    private static final long serialVersionUID = -4824601743400604129L;
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("用户id ")
    private Long userId;

    @ApiModelProperty("好友id")
    private Long acceptId;

    @ApiModelProperty("是否已读 0 未读")
    private Integer hasRead;
    // 发送的时间
    private LocalDateTime sendTime;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createTime;
    private Integer status;
    @ApiModelProperty("是否删除")
    @TableLogic
    private Integer isDelete;

    @ApiModelProperty("消息")
    private String message;

}
