package com.ice.hxy.util;

import java.util.regex.Pattern;

/**
 * @Author ice
 * @Date 2023/1/9 17:46
 * @Description: TODO
 */
public class REUtils {
    public static boolean isTel(String tel) {
        String pattern = "(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";
        return !Pattern.compile(pattern).matcher(tel).matches();
    }
    public static boolean isEmail(String email) {
        String pattern = "\\w[-\\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\\.)+[A-Za-z]{2,14}";
        return !Pattern.compile(pattern).matcher(email).matches();
    }
    public static boolean isName(String name) {
        String pattern = "[\\w@]+";
        return !Pattern.compile(pattern).matcher(name).find();
    }
}
