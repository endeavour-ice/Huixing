package com.ice.hxy.mode.enums;

public enum MQKey {
    NETTY("Netty队列","netty","netty_queue"),
    REDIS("redis队列","redis_key","redis_queue"),
    TEAM("队伍的队列","team_key","team_queue"),
    READ_TEAM("队伍聊天信息已读的队列", "read_team_key","read_team_queue"),
    READ_CHAT("好友聊天信息已读的队列", "read_chat_key","read_chat_queue"),
    READ_POST("Post已读的队列", "read_post_key","read_post_queue"),
    OSS("oos的队列", "oss_key","oss_queue"),
    TAG("tag的队列", "tag_key","tag_queue"),
    REMOVE_REDIS("删除Redis key的队列", "removeRedisByKey","removeRedisByQueue"),
    CHAT_HAL("大厅信息的队列", "chat_hal","chat_hal_queue"),
    DIE("死信队列", "die_key","die_queue");

    // 绑定的key

    private final String key;
    private final String queue;

    MQKey(String dec, String key, String queue) {
        this.key = key;
        this.queue = queue;
    }

    public String getKey() {
        return key;
    }

    public String getQueue() {
        return queue;
    }
}
