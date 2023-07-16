package com.ice.hxy.mode.request.admin;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author ice
 * @Date 2023/7/9 20:59
 * @Description: TODO
 */
@Data
public class UserStatusReq implements Serializable {
    private static final long serialVersionUID = 1001410978174347696L;
    private int id;
    private String status;
}
