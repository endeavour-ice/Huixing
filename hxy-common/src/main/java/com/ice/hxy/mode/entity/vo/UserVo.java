package com.ice.hxy.mode.entity.vo;

import cn.hutool.core.util.DesensitizedUtil;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.ice.hxy.annotation.Sensitive;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户消息脱敏
 * </p>
 *
 * @author ice
 * @since 2022-06-14
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "User对象", description = "用户表")
public class UserVo extends UserAvatarVo implements Serializable{

    private static final long serialVersionUID = -6204388767292859512L;

    @ApiModelProperty("登陆账号")
    private String userAccount;

    @ApiModelProperty("性别")
    private String gender;

    /**
     * 标签
     */
    private String tags;

    /**
     * 个人描述
     */
    private String profile;

    @ApiModelProperty("手机号")
    @Sensitive(DesensitizedUtil.DesensitizedType.MOBILE_PHONE)
    private String tel;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("用户状态")
    private Integer userStatus;

    @ApiModelProperty("用户角色 ,判断是否是管理员")
    private String role;

    @ApiModelProperty("成员编号")
    private String planetCode;

    @ApiModelProperty("创建时间")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createTime;



}
