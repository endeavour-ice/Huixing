package com.ice.hxy.mq.listener;

import com.ice.hxy.mode.entity.OpLog;
import com.ice.hxy.mode.mq.MqClient;
import com.ice.hxy.service.IOpLogService;
import com.ice.hxy.util.GsonUtils;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * @Author ice
 * @Date 2023/7/3 20:28
 * @Description: TODO
 */
@Component
@Slf4j
public class LogListener {
    @Resource
    private IOpLogService opLogService;

    @RabbitListener(queues = MqClient.LOG_QUEUE)
    public void saveLog(Message message, Channel channel) {
        OpLog opLog = GsonUtils.getGson().fromJson(new String(message.getBody(), StandardCharsets.UTF_8), OpLog.class);
        opLogService.save(opLog);
    }
}
