package com.ice.hxy.mode.entity.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.ice.hxy.mode.resp.SafetyUserResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 返回队伍的用户
 *
 * @author ice
 * @since 2022-06-14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamUserVo extends TeamUserAvatarVo {


    private static final long serialVersionUID = 4751948160720884461L;

    private String url;

    /**
     * 最大人数
     */
    private Long maxNum;
    /**
     * 队伍的标签
     */
    private String tags;
    /**
     * 创建时间
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createTime;
    /**
     * 状态
     */
    private Integer status;

    /**
     * 过期的时间
     */
    private LocalDateTime expireTime;


    private List<SafetyUserResponse> userVo = new ArrayList<>();
}
