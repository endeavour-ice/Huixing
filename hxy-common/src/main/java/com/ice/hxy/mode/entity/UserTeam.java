package com.ice.hxy.mode.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 队伍表
 * @TableName user_team
 */
@TableName(value ="user_team")
@Data
public class UserTeam implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = -4639540624582420418L;
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 加入队伍时间
     */
    @TableField("join_time")
    private LocalDateTime joinTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @ApiModelProperty("消息未读数")
    private Integer unreadNum;
    /**
     * 创建时间
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updateTime;


}