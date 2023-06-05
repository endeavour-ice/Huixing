package com.ice.hxy.util;

/**
 * @Author ice
 * @Date 2023/5/30 15:33
 * @Description: TODO
 */
public class PageUtil {
    public static boolean hasNext(long current, long size, long total) {
        if (size == 0) {
            return false;
        }
        long pages = total / size;
        if (total % size != 0) {
            pages++;
        }
        return current < pages;
    }
}
