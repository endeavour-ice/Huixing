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
    ROOT(999,"超级管理员"),
    /**
     * 普通用户
     */
    NORMAL(0,"普通用户"),
    /**
     * 管理员
     */
    ADMIN(1,"管理员"),

    /**
     * 测试
     */
    TEST(2,"测试");
    private final int key;
    private final String name;

    UserRole(int key,String name) {
        this.key = key;
        this.name = name;
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
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
