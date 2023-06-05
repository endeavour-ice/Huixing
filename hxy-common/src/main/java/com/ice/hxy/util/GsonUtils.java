package com.ice.hxy.util;

import com.google.gson.Gson;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.exception.GlobalException;

/**
 * @Author ice
 * @Date 2023/2/24 19:33
 * @Description: TODO
 */
public class GsonUtils {
    private static volatile Gson gson;

    // 防止反射破解
    private GsonUtils() {
        if (gson != null) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
    }
    public static Gson getGson() {
        if (gson != null) {
            return gson;
        }
        synchronized (GsonUtils.class) {
            if (gson != null) {
                return gson;
            }
            gson = new Gson();
            return gson;
        }
    }
}
