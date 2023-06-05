package com.ice.hxy.mq.listener;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ice.hxy.mode.mq.AckMode;
import com.ice.hxy.mode.mq.MqClient;
import com.ice.hxy.util.GsonUtils;
import com.ice.hxy.mapper.ChatRecordMapper;
import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.entity.ChatHalRecord;
import com.ice.hxy.mode.entity.ChatRecord;
import com.ice.hxy.mode.entity.TeamChatRecord;
import com.ice.hxy.service.commService.RedisCache;

import com.ice.hxy.service.chatService.IChatHalRecordService;
import com.ice.hxy.service.chatService.IChatRecordService;
import com.ice.hxy.service.chatService.ITeamChatRecordService;
import com.ice.hxy.util.SensitiveUtils;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/3/3 18:18
 * @Description: TODO
 */
@Component
@Slf4j
public class ChatListener {
    @Resource
    private ChatRecordMapper chatRecordMapper;
    @Autowired
    private IChatHalRecordService chatHalRecordService;
    @Resource
    private SaveMessageMq messageMq;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private IChatRecordService chatRecordService;
    @Autowired
    private ITeamChatRecordService teamChatRecordService;

    @Resource
    private SaveMessageMq saveMessageMq;

    @RabbitListener(queues = MqClient.READ_CHAT_QUEUE, ackMode = AckMode.AUTO)
    public void chatRedRecord(Message message, Channel channel) {
        Gson gson = GsonUtils.getGson();
        String mes = new String(message.getBody(), StandardCharsets.UTF_8);
        List<String> ids = gson.fromJson(mes, new TypeToken<List<String>>() {
        }.getType());
        if (!ids.isEmpty()) {
            List<ChatRecord> list = chatRecordMapper.selectBatchIds(ids);
            List<Long> arrayList = new ArrayList<>();
            for (ChatRecord chatRecord : list) {
                if (chatRecord.getHasRead() == 0) {
                    arrayList.add(chatRecord.getId());
                }
            }
            if (arrayList.size() > 0) {
                chatRecordMapper.updateReadBatchById(arrayList);
            }


        } else {
            messageMq.saveMessage(message, "chatRedRecord 要处理的数据为空");
        }


    }

    @RabbitListener(queues = MqClient.CHAT_HAL_QUEUE, ackMode = AckMode.MANUAL)
    public void chatHAL(Message message, Channel channel) {
        boolean saveMessage = messageMq.saveMessage(message);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Gson gson = GsonUtils.getGson();
        String mes = new String(message.getBody(), StandardCharsets.UTF_8);
        if (saveMessage) {
            ChatHalRecord chatHalRecord = gson.fromJson(mes, ChatHalRecord.class);
            chatHalRecordService.save(chatHalRecord);
        }
        try {

            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("error chatHAL message:{}", e.getMessage());
        }

    }


    @RabbitListener(queues = MqClient.NETTY_QUEUE, ackMode = AckMode.MANUAL)
    public void SaveChatRecord(Message message, Channel channel) {
        MessageProperties messageProperties = message.getMessageProperties();
        String messageId = messageProperties.getClusterId();
        long deliveryTag = messageProperties.getDeliveryTag();
        try {
            Gson gson = GsonUtils.getGson();
            ChatRecord chatRecord = gson.fromJson(new String(message.getBody(), StandardCharsets.UTF_8), ChatRecord.class);
            if (chatRecord != null) {
                String mes = chatRecord.getMessage();
                try {
                    mes = SensitiveUtils.sensitive(mes);
                } catch (Exception e) {
                    log.error("SaveChatRecord 过滤失败");
                }

                chatRecord.setMessage(mes);
                boolean save = chatRecordService.save(chatRecord);
                if (!save) {
                    saveMessageMq.saveMessage(message, "保存聊天记录失败");
                    log.error("保存聊天记录失败");
                }else {
                    if (chatRecord.getHasRead() == 0) {
                        Long friendId = chatRecord.getAcceptId();
                        String key= CacheConstants.READ_CHAT + chatRecord.getUserId();
                        Double score = redisCache.getZSetScore(key, friendId);
                        if (score != null) {
                            redisCache.setScore(key, friendId,++score);
                        }else {
                            redisCache.addZSet(key, friendId, 1);
                        }
                    }
                }

            } else {
                saveMessageMq.saveMessage(message, "chatRecord == null ");
                log.error("保存聊天记录失败");
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            saveMessageMq.saveMessage(messageId, e.getMessage());
            log.error("保存聊天记录失败: {}", e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("消息队列拒绝失败: {}", e.getMessage());
            }
        }


    }

    @RabbitListener(queues = MqClient.TEAM_QUEUE, ackMode = AckMode.MANUAL)
    public void SaveTeamChatRecord(Message message, Channel channel) {
        MessageProperties messageProperties = message.getMessageProperties();
        String messageId = messageProperties.getClusterId();
        long deliveryTag = messageProperties.getDeliveryTag();
        TeamChatRecord teamChatRecord = GsonUtils.getGson().fromJson(new String(message.getBody(), StandardCharsets.UTF_8), TeamChatRecord.class);
        if (teamChatRecord != null) {
            boolean save = teamChatRecordService.save(teamChatRecord);
            if (!save) {
                log.error("保存队伍聊天记录失败...");
            }
        } else {
            log.error("保存队伍聊天记录失败...");
        }

        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("消息队列拒绝失败:{}", e.getMessage());
        }
    }
}
