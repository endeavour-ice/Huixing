package com.ice.hxy.mode.enums;

import com.ice.hxy.mode.entity.User;
import com.ice.hxy.util.UserUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author ice
 * @Date 2023/3/18 11:42
 * @Description: TODO
 */
public enum UserRole {
    ROOT("root","超级管理员"),
    /**
     * 普通用户
     */
    USER("user","普通用户"),
    /**
     * 管理员
     */
    ADMIN("admin","管理员"),

    /**
     * 测试
     */
    TEST("test","测试");
    private final String key;
    private final String name;

    UserRole(String key,String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public static String getN(String key) {
        for (UserRole value : UserRole.values()) {
            if (key.equals(value.getKey())) {
                return value.getName();
            }
        }
        return USER.getName();
    }
    public static boolean isAdmin(User user) {
        return user!=null && (user.getRole().equals(ADMIN.getKey())||user.getRole().equals(ROOT.getKey()));
    }
    public static boolean isAdmin() {
        User user = UserUtils.getLoginUser();
        return isAdmin(user);
    }
    public static boolean isRoot(User user) {
        return user!=null && user.getRole().equals(ROOT.getKey());
    }
    public static boolean isRoot(HttpServletRequest request) {
        User user = UserUtils.getLoginUser();
        return isRoot(user);
    }
}
