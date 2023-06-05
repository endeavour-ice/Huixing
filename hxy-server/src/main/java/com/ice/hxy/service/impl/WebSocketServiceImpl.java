package com.ice.hxy.service.impl;


import com.ice.hxy.mode.chat.ChatType;
import com.ice.hxy.service.commService.TokenService;
import com.ice.hxy.util.GsonUtils;
import com.ice.hxy.mode.chat.ChatMessageResp;
import com.ice.hxy.mode.chat.SocketResp;
import com.ice.hxy.mode.entity.ChatRecord;
import com.ice.hxy.mode.entity.TeamChatRecord;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.event.UserOnlineEvent;
import com.ice.hxy.mq.RabbitService;
import com.ice.hxy.service.ChatService;
import com.ice.hxy.service.WebSocketService;
import com.ice.hxy.util.LongUtil;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author ice
 * @Date 2023/5/21 17:30
 * @Description: TODO
 */
@Service
public class WebSocketServiceImpl implements WebSocketService {
    // 所有在线的的连接
    private static final Map<Long, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();
    // 所有连接
    private static final ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    @Autowired
    @Qualifier("websocketExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private TokenService tokenService;
    @Resource
    private RabbitService rabbitService;
    @Resource
    private ChatService chatService;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void sendPyOnline(SocketResp<ChatMessageResp> socketResp, ChatRecord chatRecord) {
        Long acceptId = chatRecord.getAcceptId();
        Channel channel = USER_CHANNEL_MAP.get(acceptId);
        chatRecord.setHasRead(1);
        if (channel != null) {
            chatRecord.setHasRead(0);
            sendMsg(channel, socketResp);
        }
        rabbitService.sendNettyChat(chatRecord);
    }

    private <T> void sendMsg(Channel channel, SocketResp<T> socketResp) {
        channel.writeAndFlush(new TextWebSocketFrame(GsonUtils.getGson().toJson(socketResp)));
    }

    @Override
    public void sendTeamOnline(SocketResp<ChatMessageResp> resp, List<Long> tuids, TeamChatRecord chatRecord) {

        for (Long tuid : tuids) {
            Channel channel = USER_CHANNEL_MAP.get(tuid);
            if (channel != null) {
                // 用户在线推送
                threadPoolTaskExecutor.execute(() -> sendMsg(channel, resp));
            } else {
                // 用户不在线
                chatService.unReadTeam(tuid, chatRecord.getTeamId());
            }
        }
        rabbitService.sendNettyChatTeam(chatRecord);
    }

    @Override
    public void sendGPTOnline(SocketResp<ChatMessageResp> resp, Long uid, ChatRecord record) {
        Channel channel = USER_CHANNEL_MAP.get(uid);
        sendMsg(channel, resp);
        rabbitService.sendNettyChat(record);
    }

    @Override
    public void handlerAdded(Channel channel) {
        CHANNELS.add(channel);
    }

    @Override
    public void auth(String token, Channel channel) {
        User user = tokenService.getTokenUser(token);
        if (user == null) {
            // token失效通知
            SocketResp<String> socketResp = new SocketResp<>();
            socketResp.setType(ChatType.XX.getValue());
            sendMsg(channel, socketResp);
        } else {
            USER_CHANNEL_MAP.put(user.getId(), channel);
            // 上线通知
            applicationEventPublisher.publishEvent(new UserOnlineEvent(this,user));
        }

    }

    public Long getChannelById(Channel channel) {
        if (channel == null) {
            return null;
        }
        for (Long id : USER_CHANNEL_MAP.keySet()) {
            Channel chl = USER_CHANNEL_MAP.get(id);
            if (chl.equals(channel) && chl.id().asLongText().equals(channel.id().asLongText())) {
                return id;
            }
        }
        return null;
    }

    @Override
    public void close(Channel channel) {
        Long id = getChannelById(channel);
        CHANNELS.remove(channel);
        if (!LongUtil.isEmpty(id)) {
            USER_CHANNEL_MAP.remove(id);
        }

    }



    @Override
    public void sendPyOnlineNotice(SocketResp<ChatMessageResp.UserInfo> socketResp, List<Long> fids) {
        for (Long fid : fids) {
            Channel channel = USER_CHANNEL_MAP.get(fid);
            if (channel == null) {
                continue;
            }
            sendMsg(channel, socketResp);
        }
    }

    public  void close(Long id) {
        Channel channel = USER_CHANNEL_MAP.get(id);
        if (channel != null) {
            channel.close();
        }

        USER_CHANNEL_MAP.remove(id);
    }
}
