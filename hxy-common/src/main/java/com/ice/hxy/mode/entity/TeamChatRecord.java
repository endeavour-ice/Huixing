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
 * 队伍聊天记录表
 * </p>
 *
 * @author ice
 * @since 2022-09-12
 */
@TableName("team_chat_record")
@ApiModel(value = "TeamChatRecord对象", description = "队伍聊天记录表")
@Data
public class TeamChatRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("队伍id")
    private Long teamId;

    @ApiModelProperty("消息")
    private String message;

    private Integer status;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createTime;

    @ApiModelProperty("是否删除")
    @TableLogic
    private Integer isDelete;


}
