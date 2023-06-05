package com.ice.hxy.util;


import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.exception.GlobalException;

import java.text.SimpleDateFormat;

/**
 * @Author ice
 * @Date 2023/2/24 12:28
 * @Description: TODO
 */
public class SDFUtils {
    private static volatile SimpleDateFormat sdf;
    private static final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

    // 防止反射破解
    private SDFUtils() {
        if (sdf != null) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
    }

    public static SimpleDateFormat getFdt() {
        if (sdf != null) {
            return sdf;
        }
        synchronized (SDFUtils.class) {
            if (sdf != null) {
                return sdf;
            }
            sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return sdf;
        }
    }

    public static SimpleDateFormat getSf() {
        return sf;
    }
}
