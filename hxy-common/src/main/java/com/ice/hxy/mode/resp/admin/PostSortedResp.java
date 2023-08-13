package com.ice.hxy.mode.resp.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author ice
 * @Date 2023/8/6 11:43
 * @Description: TODO
 */
@Data
@AllArgsConstructor
public class PostSortedResp {
    private  String value;
    private  String dec;
    private boolean other = false;
}
