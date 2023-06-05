package com.ice.hxy.mode.request;

import lombok.Data;

/**
 * @Author ice
 * @Date 2023/4/26 17:25
 * @Description: QQ登录
 */
@Data
public class QQLoginRequest {
    private String type;
    private String code;
}
