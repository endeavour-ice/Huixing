package com.ice.hxy.service;

import com.ice.hxy.mode.chat.ChatMessageResp;
import com.ice.hxy.mode.chat.SocketResp;
import com.ice.hxy.mode.entity.ChatRecord;
import com.ice.hxy.mode.entity.TeamChatRecord;
import io.netty.channel.Channel;

import java.util.List;

/**
 * @Author ice
 * @Date 2023/5/21 17:17
 * @Description: TODO
 */
public interface WebSocketService {
    /**
     * 处理ws连接
     * @param channel
     */
    void handlerAdded(Channel channel);
    /**
     * 将消息发送好友
     * @param socketResp
     * @param chatRecord
     */
    void sendPyOnline(SocketResp<ChatMessageResp> socketResp, ChatRecord chatRecord);

    /**
     * 将消息发送个队伍
     * @param resp 消息体
     * @param tuids 队伍群员id
     * @param chatRecord 要要保存的消息体
     */
    void sendTeamOnline(SocketResp<ChatMessageResp> resp, List<Long> tuids, TeamChatRecord chatRecord);

    /**
     * gpt 聊天
     * @param resp
     * @param uid
     * @param record
     */
    void sendGPTOnline(SocketResp<ChatMessageResp> resp, Long uid,ChatRecord record);

    /**
     * 关闭连接
     * @param channel
     */
    void close(Channel channel);
    void close(Long id);

    /**
     * 认证
     * @param token token
     * @param channel 连接
     */
    void auth(String token, Channel channel);

    void sendPyOnlineNotice(SocketResp<ChatMessageResp.UserInfo> socketResp, List<Long> fids);
}
