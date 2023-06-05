package com.ice.hxy.mode.entity.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author ice
 * @Date 2023/3/15 20:48
 * @Description: TODO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostUserVo extends UserAvatarVo implements Serializable {
    private static final long serialVersionUID = -1109349728470096817L;
    private int thumbTotal;
    private int postTotal;
    // 创建时长
    private String joinTime;
}
