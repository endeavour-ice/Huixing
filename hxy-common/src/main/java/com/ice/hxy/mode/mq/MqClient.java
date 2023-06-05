package com.ice.hxy.mode.mq;

/**
 * @author ice
 * @date 2022/8/20 15:49
 */

public interface MqClient {
    // 普通
    String NETTY_QUEUE = "netty_queue";
    String REDIS_QUEUE = "redis_queue";
    String DIRECT_EXCHANGE = "exchange_direct";
    String EXCHANGE_CAL_DIRECT = "exchange_cal_direct";
    String TEAM_QUEUE = "team_queue";
    String READ_TEAM_QUEUE = "read_team_queue";
    String READ_CHAT_QUEUE = "read_chat_queue";
    String OSS_QUEUE = "oss_queue";
    // 删除 redis key
    String REMOVE_REDIS_QUEUE = "removeRedisByQueue";
    //
    String CHAT_HAL_QUEUE = "chat_hal_queue";
    String READ_POST_QUEUE = "read_post_queue";
    // 死信
    String DIE_EXCHANGE = "exchange_die";
    String DIE_QUEUE = "die_queue";

}
