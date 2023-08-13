package com.ice.hxy.mode.enums;

/**
 * 用户的状态
 *
 * @author ice
 * @date 2022/9/19 17:18
 */

public enum UserStatus {
    NORMAL(0,"正常用户"),
    LOCKING(1,"被锁定"),
    PRIVATE(2, "私密");
    private final int key;
    private final String name;
    UserStatus(int key,String name) {
        this.key = key;
        this.name = name;
    }

    public static String getN(Integer key) {
        for (UserStatus value : UserStatus.values()) {
            if (key.equals(value.getKey())) {
                return value.getName();
            }
        }
        return NORMAL.getName();
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }
}
