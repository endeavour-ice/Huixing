package com.ice.hxy.mode.entity.admin;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author ice
 * @Date 2023/7/21 10:39
 * @Description:
 */
@Data
public class AdminUserResp {
    private Long id;

    private String username;

    private String userAccount;
    @ApiModelProperty("登录的类型 	0 - 密码	1 - 微信	2 -QQ")
    private String loginType;
    private String avatarUrl;

    private String gender;
    /**
     * 标签
     */
    private List<String> tags;

    private String tel;

    private String email;

    private String userStatus;

    private String role;

    private String createTime;

}
