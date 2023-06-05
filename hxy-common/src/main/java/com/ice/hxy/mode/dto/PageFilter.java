package com.ice.hxy.mode.dto;

import lombok.Data;

/**
 * @Author ice
 * @Date 2023/3/14 21:21
 * @Description: 手写sql page过滤
 */
@Data
public class PageFilter {
    private long current = 0;
    private long size = 20;

    public PageFilter(Long current, Long size) {
        if (current == null || size == null) {
            return;
        }
        if (--current >= 0 && size <= 30) {
            this.current = current * size;
            this.size = size;
        }
    }

    public PageFilter() {
    }
}
