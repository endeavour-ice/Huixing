package com.ice.hxy.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author ice
 * @date 2022/6/14 15:41
 */

public class MD5 {
    private static final  String SALT = "ice";
    private static final  String TEAMS = "jb";


    public static String getMD5(String password) {
        // 加密密码
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }
    public static String getTeamMD5(String teamPassword) {
        // 加密密码
        return DigestUtils.md5DigestAsHex((TEAMS + teamPassword).getBytes());
    }
    /**
     * MD5加密之方法一
     * @explain 借助apache工具类DigestUtils实现
     * @param str
     *            待加密字符串
     * @return 16进制加密字符串
     */
    public static String encryptToMD5(String str) {
        return DigestUtils.md5DigestAsHex(str.getBytes(StandardCharsets.UTF_8));
    };

}
