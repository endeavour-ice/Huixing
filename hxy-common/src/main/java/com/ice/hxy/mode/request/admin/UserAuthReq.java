package com.ice.hxy.mode.request.admin;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author ice
 * @Date 2023/7/9 20:34
 * @Description: 用户权限设置
 */
@Data
public class UserAuthReq implements Serializable {
    private static final long serialVersionUID = 8503292499955594243L;
    private int id;
    private String role;
}
