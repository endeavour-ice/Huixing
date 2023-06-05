package com.ice.hxy.mode.chat;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 聊天的类型
 * @author ice
 * @date 2022/9/12 19:08
 */

public enum ChatType {
    SM("系统",999),
    CT("连接",0),
    FD("好友",1),
    TM("队伍",2),
    XT("心跳",3),
    ADV("上线通知",4),
    XX("前端token失效",5),
    HAL("大厅",6),
    GP("智能助手", 7);
    private final int value;
    private static final Map<Integer, ChatType> cache;

    static {
        cache = Arrays.stream(ChatType.values()).collect(Collectors.toMap(ChatType::getValue, Function.identity()));
    }
    public static ChatType of(Integer value) {
        return cache.get(value);
    }
    ChatType(String type, int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
