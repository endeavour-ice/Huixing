package com.ice.hxy.mode.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ice
 * @date 2022/8/23 12:46
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamUpdateRequest extends IdRequest implements Serializable{

    private static final long serialVersionUID = 7722073575347437133L;

    /**
     * 队伍的名称
     */
    private String name;
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
     * 过期时间
     */
    private LocalDateTime expireTime;
}
