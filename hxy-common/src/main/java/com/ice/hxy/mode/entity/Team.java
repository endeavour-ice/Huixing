package com.ice.hxy.mode.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 队伍表
 *
 * @TableName team
 */
@TableName(value = "team")
@Data
public class Team implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 2232493452646148672L;
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 队伍的名称
     */
    private String name;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Long maxNum;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态
     */
    private Integer status;
    /**
     * 队伍头像
     */
    private String avatarUrl;
    /**
     * 队伍标签
     */
    private String tags;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

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