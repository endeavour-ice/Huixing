package com.ice.hxy.mode.enums;

/**
 * @Author ice
 * @Date 2023/5/28 10:13
 * @Description: 文章分组
 */
public enum PostGroupEnum {
    INDEX(0, "主页发布的文章"),
    TEAM(999,"队伍的文章");

    PostGroupEnum(long value,String dec) {
        this.value = value;
    }

    private final long value;

    public long getValue() {
        return value;
    }
}
