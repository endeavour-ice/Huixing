package com.ice.hxy.mode.enums;

/**
 * @Author ice
 * @Date 2023/5/27 23:03
 * @Description: 标签类别
 */
public enum TagCategoryEnum {
    // 0-通用 1-用户 2-队伍 3-文章
    COMMON(0, "通用"),
    USER(1, "用户"),
    TEAM(2, "队伍"),
    POST(3, "文章"),
    INDEX(4, "主页"),
    TEAM_POST(999, "队伍文章");
    private final long value;

    TagCategoryEnum(int value,String dec) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public static TagCategoryEnum getTagEnumByValue(Long value) {
        if (value == null) {
            return COMMON;
        }
        for (TagCategoryEnum tagCategoryEnum : TagCategoryEnum.values()) {
            long val = tagCategoryEnum.getValue();
            if (value.equals(val)) {
                return tagCategoryEnum;
            }
        }
        return COMMON;
    }
}
