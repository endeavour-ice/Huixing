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
    HOT("hot", "最热"),
    RANDOM("rdm", "随机");
    private final String value;
    private final String dec;

    PostSortedEnum(String value, String dec) {
        this.value = value;
        this.dec = dec;
    }

    public String getValue() {
        return value;
    }

    public String getDec() {
        return dec;
    }

    public static PostSortedEnum isScope(String value, String d) {
        PostSortedEnum deft = DESC;
        if (StringUtils.hasText(d)) {
            PostSortedEnum postSortedEnum = hasEnum(d);
            if (postSortedEnum!=null) deft = postSortedEnum;
        }
        if (StringUtils.hasText(value)) {
            return deft;
        }
        for (PostSortedEnum postSortedEnum : PostSortedEnum.values()) {
            if (postSortedEnum.getValue().equals(value)) {
                return postSortedEnum;
            }
        }
        return deft;
    }

    public static PostSortedEnum hasEnum(String value) {
        for (PostSortedEnum postSortedEnum : PostSortedEnum.values()) {
            if (postSortedEnum.getValue().equals(value)) {
                return postSortedEnum;
            }
        }
        return null;

    }
}
