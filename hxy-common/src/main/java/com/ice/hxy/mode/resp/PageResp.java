package com.ice.hxy.mode.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author ice
 * @Date 2023/5/30 14:43
 * @Description: TODO
 */
@Data
@AllArgsConstructor
public class PageResp<T> {
    private Boolean isLast = Boolean.FALSE;
    private T data;
}
