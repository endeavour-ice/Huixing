package com.ice.hxy.mq;

import com.ice.hxy.mode.entity.ChatHalRecord;
import com.ice.hxy.mode.entity.ChatRecord;
import com.ice.hxy.mode.entity.OpLog;
import com.ice.hxy.mode.entity.TeamChatRecord;
import com.ice.hxy.mode.entity.vo.ChatListVo;
import com.ice.hxy.mode.enums.MQKey;
import com.ice.hxy.mode.mq.MqClient;
import com.ice.hxy.util.GsonUtils;
import com.ice.hxy.util.IpUtils;
import com.ice.hxy.util.SnowFlake;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ice.hxy.mode.mq.MqClient.DIRECT_EXCHANGE;

/**
 * @author ice
 * @date 2022/8/20 16:03
 */
@Service
public class RabbitService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String exchange, String routingKey, Object message) {
        MessageProperties properties = new MessageProperties();
        String snowString = SnowFlake.getSnowString();
        properties.setClusterId(snowString);
        String json = GsonUtils.getGson().toJson(message);
        Message mess = new Message(json.getBytes(StandardCharsets.UTF_8), properties);
        rabbitTemplate.convertAndSend(exchange, routingKey, mess);
    }

    // 好友聊天
    public void sendNettyChat(ChatRecord chatRecord) {
        sendMessage(DIRECT_EXCHANGE, MQKey.NETTY.getKey(), chatRecord);
    }

    // 大厅保存聊天记录
    public void sendNettyChatHAL(ChatHalRecord chatRecord) {
        sendMessage(DIRECT_EXCHANGE, MQKey.CHAT_HAL.getKey(), chatRecord);

    }

    // 队伍保存聊天记录
    public void sendNettyChatTeam(TeamChatRecord teamChatRecord) {

        sendMessage(DIRECT_EXCHANGE, MQKey.TEAM.getKey(), teamChatRecord);
    }

    // 删除redis
    public void sendDelRedisKey(String key) {
        sendMessage(DIRECT_EXCHANGE, MQKey.REMOVE_REDIS.getKey(), key);
    }

    // 将好友聊天记录标记为已读
    public void sendHasChat(List<ChatListVo> chatListVos) {
        List<String> ids = chatListVos.stream().map(ChatListVo::getId).collect(Collectors.toList());
        sendMessage(DIRECT_EXCHANGE, MQKey.READ_CHAT.getKey(), ids);
    }

    // 将文章的浏览记录加+1 并计算该用户的推荐列表
    public void postRead(Long userId, Long postId) {
        Map<String, Long> map = new HashMap<>(2);
        map.put("userId", userId);
        map.put("postId", postId);
        String json = GsonUtils.getGson().toJson(map);
        sendMessage(MqClient.EXCHANGE_CAL_DIRECT, MQKey.READ_POST.getKey(), json);
    }

    public void saveTag(Long userId,Long groupId,String tag) {
        Map<String, Object> map = new HashMap<>(3);
        map.put("userId", userId);
        map.put("groupId", groupId);
        map.put("tag", tag);
        String json = GsonUtils.getGson().toJson(map);
        sendMessage(DIRECT_EXCHANGE, MQKey.TAG.getKey(),json);
    }
    // 保存日志
    public void saveLog(OpLog opLog) {
        opLog.setOpLocation(IpUtils.getRealAddressByIP(opLog.getOpIp()));
        sendMessage(MqClient.LOG_EXCHANGE,MqClient.LOG_Key,GsonUtils.getGson().toJson(opLog));
    }
}
