package com.ice.hxy.extend.wx;

import lombok.Data;

/**
 * @Author ice
 * @Date 2023/3/24 15:05
 * @Description: TODO
 */
@Data
public class WxToken {
    private String access_token;
    private Long expires_in;
}
