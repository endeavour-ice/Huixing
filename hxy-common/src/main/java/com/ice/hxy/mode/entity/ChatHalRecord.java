package com.ice.hxy.mode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 大厅聊天记录表
 * </p>
 *
 * @author ice
 * @since 2023-04-24
 */
@TableName("chat_hal_record")
@ApiModel(value = "ChatHalRecord对象", description = "大厅聊天记录表")
@Data
public class ChatHalRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("用户id	")
    private Long userId;

    @ApiModelProperty("消息")
    private String message;

    @ApiModelProperty("是否已读 0 未读")
    private Integer hasRead;

    @ApiModelProperty("发送的时间")
    private String sendTime;


}
