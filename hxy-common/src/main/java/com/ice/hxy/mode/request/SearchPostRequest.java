package com.ice.hxy.mode.request;

import lombok.Data;

/**
 * @Author ice
 * @Date 2023/3/19 21:29
 * @Description:
 */
@Data
public class SearchPostRequest {
    private Long userId;
    private String content;
    private String tag;
}
