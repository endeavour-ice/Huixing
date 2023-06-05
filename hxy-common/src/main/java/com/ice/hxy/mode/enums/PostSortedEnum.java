package com.ice.hxy.mode.enums;

import org.springframework.util.StringUtils;

/**
 * @Author ice
 * @Date 2023/5/28 17:58
 * @Description: 排序
 */
public enum PostSortedEnum {
    // 1 - 升序， 2 - 降序， 3 - 点赞最多，4 - 评论最多, 5-推荐
    DESC("lat", "最新"),
    THUMB("like", "点赞最多"),
    COMMENT("com", "评论最多"),
    RECOMMEND("rec", "推荐"),
    HOT("hot", "最热");
    private final String value;

    PostSortedEnum(String value, String dec) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PostSortedEnum isScope(String value) {
        if (!StringUtils.hasText(value)) {
            return RECOMMEND;
        }
        for (PostSortedEnum postSortedEnum : PostSortedEnum.values()) {
            if (postSortedEnum.getValue().equals(value)) {
                return postSortedEnum;
            }
        }
        return RECOMMEND;
    }
}
