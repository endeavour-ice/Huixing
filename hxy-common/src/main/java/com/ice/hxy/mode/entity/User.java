package com.ice.hxy.mode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author ice
 * @since 2022-06-14
 */
@Data
@ApiModel(value = "User对象", description = "用户表")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("登陆账号")
    private String userAccount;
    @ApiModelProperty("登录的类型 	0 - 密码	1 - 微信	2 -QQ")
    private Integer loginType;
    @ApiModelProperty("微信id")
    private String openId;
    @ApiModelProperty("用户头像")
    private String avatarUrl;

    @ApiModelProperty("性别")
    private String gender;

    @ApiModelProperty("密码")
    private String password;
    /**
     * 标签
     */
    private String tags;

    /**
     * 个人描述
     */
    private String profile;

    @TableField(exist = false)
    private long expireTime;
    @TableField(exist = false)
    private long loginTime;

    @ApiModelProperty("手机号")
    private String tel;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("用户状态")
    private Integer userStatus;

    @ApiModelProperty("用户角色 ,判断是否是管理员")
    private String role;

    @ApiModelProperty("成员编号")
    private String planetCode;

    @ApiModelProperty("是否删除")
    @TableLogic
    private Integer isDelete;

    @ApiModelProperty("创建时间")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createTime;

    @ApiModelProperty("修改时间")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updateTime;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return user.getId().equals(this.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
