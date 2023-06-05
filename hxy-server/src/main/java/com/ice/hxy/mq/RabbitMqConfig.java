package com.ice.hxy.mq;


import com.ice.hxy.mode.enums.MQKey;
import com.ice.hxy.mode.mq.MqClient;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * mq 创建队列
 *
 * @author ice
 * @date 2022/8/20 11:23
 */
@Configuration
public class RabbitMqConfig {
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean("directExchange")
    public DirectExchange directExchange() {
        return new DirectExchange(MqClient.DIRECT_EXCHANGE);
    }

    @Bean("directCalExchange")
    public DirectExchange directCalExchange() {
        return new DirectExchange(MqClient.EXCHANGE_CAL_DIRECT);
    }

    @Bean("nettyQueue")
    public Queue NettyQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", MqClient.DIE_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", MQKey.DIE.getKey());
        // 设置过期时间 10 秒
        arguments.put("x-message-ttl", 10000);
        return QueueBuilder.durable(MQKey.NETTY.getQueue()).withArguments(arguments).build();
    }

    @Bean
    public Binding bindingNetty(@Qualifier("directExchange") DirectExchange directExchange,
                                @Qualifier("nettyQueue") Queue nettyQueue) {
        return BindingBuilder.bind(nettyQueue).to(directExchange).with(MQKey.NETTY.getKey());
    }

    @Bean("dieExchange")
    public DirectExchange dieExchange() {
        return new DirectExchange(MqClient.DIE_EXCHANGE);
    }

    @Bean("dieQueue")
    public Queue dieQueue() {
        return QueueBuilder.durable(MQKey.DIE.getQueue()).build();
    }

    @Bean
    public Binding dieBinding(@Qualifier("dieExchange") DirectExchange dieExchange,
                              @Qualifier("dieQueue") Queue dieQueue) {
        return BindingBuilder.bind(dieQueue).to(dieExchange).with(MQKey.DIE.getKey());
    }

    @Bean("redisQueue")
    public Queue redisQueue() {
        return QueueBuilder.durable(MQKey.REDIS.getQueue()).build();
    }

    @Bean
    public Binding redisBinding(@Qualifier("directExchange") DirectExchange dieExchange,
                                @Qualifier("redisQueue") Queue redisQueue) {
        return BindingBuilder.bind(redisQueue).to(dieExchange).with(MQKey.REDIS.getKey());
    }

    @Bean("teamQueue")
    public Queue teamQueue() {
        return QueueBuilder.durable(MQKey.TEAM.getQueue()).build();
    }

    @Bean
    public Binding teamBinding(@Qualifier("teamQueue") Queue teamQueue,
                               @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(teamQueue).to(directExchange).with(MQKey.TEAM.getKey());
    }

    @Bean("readTeamQueue")
    public Queue ReadTeamQueue() {
        return QueueBuilder.durable(MQKey.READ_TEAM.getQueue()).build();
    }

    @Bean
    public Binding ReadTeamQueueBinding(@Qualifier("readTeamQueue") Queue ReadTeamQueue,
                                        @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(ReadTeamQueue).to(directExchange).with(MQKey.READ_TEAM.getKey());
    }

    @Bean("readChatQueue")
    public Queue ReadChatQueue() {
        return QueueBuilder.durable(MQKey.READ_CHAT.getQueue()).build();
    }

    @Bean
    public Binding ReadChatQueueBinding(@Qualifier("readChatQueue") Queue readChatQueue,
                                        @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(readChatQueue).to(directExchange).with(MQKey.READ_CHAT.getKey());
    }

    @Bean("ossQueue")
    public Queue ossQueue() {
        return QueueBuilder.durable(MQKey.OSS.getQueue()).build();
    }

    @Bean
    public Binding ossQueueBinding(@Qualifier("ossQueue") Queue ossQueue,
                                   @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(ossQueue).to(directExchange).with(MQKey.OSS.getKey());
    }

    @Bean("removeRedisByKeyQueue")
    public Queue removeRedisByKeyQueue() {
        return QueueBuilder.durable(MQKey.REMOVE_REDIS.getQueue()).build();
    }

    @Bean
    public Binding removeRedisByKeyQueueBinding(@Qualifier("removeRedisByKeyQueue") Queue removeRedisByKeyQueue,
                                                @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(removeRedisByKeyQueue).to(directExchange).with(MQKey.REMOVE_REDIS.getKey());
    }

    @Bean("chatHalQueue")
    public Queue chatHalQueue() {
        return QueueBuilder.durable(MQKey.CHAT_HAL.getQueue()).build();
    }

    @Bean
    public Binding chatHalQueueBinding(@Qualifier("chatHalQueue") Queue removeRedisByKeyQueue,
                                       @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(removeRedisByKeyQueue).to(directExchange).with(MQKey.CHAT_HAL.getKey());
    }

    @Bean("postReadQueue")
    public Queue postReadQueue() {
        return QueueBuilder.durable(MQKey.READ_POST.getQueue()).build();
    }

    @Bean
    public Binding postReadQueueBinding(@Qualifier("postReadQueue") Queue postReadQueue,
                                        @Qualifier("directCalExchange") DirectExchange directCalExchange) {
        return BindingBuilder.bind(postReadQueue).to(directCalExchange).with(MQKey.READ_POST.getKey());
    }
    @Bean("tagReadQueue")
    public Queue tagReadQueue() {
        return QueueBuilder.durable(MQKey.TAG.getQueue()).build();
    }

    @Bean
    public Binding tagReadQueueBinding(@Qualifier("tagReadQueue") Queue tagReadQueue,
                                        @Qualifier("directExchange") DirectExchange directCalExchange) {
        return BindingBuilder.bind(tagReadQueue).to(directCalExchange).with(MQKey.TAG.getKey());
    }
}
