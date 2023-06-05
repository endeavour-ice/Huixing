package com.ice.hxy.service;

import com.ice.hxy.common.B;
import com.ice.hxy.mode.chat.ChatMessagePyReq;
import com.ice.hxy.mode.chat.ChatMessageResp;
import com.ice.hxy.mode.entity.ChatRecord;
import com.ice.hxy.mode.entity.TeamChatRecord;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.request.CursorPageReq;
import com.ice.hxy.mode.resp.CursorPageResp;

import java.util.List;


public interface ChatService {
    /**
     * 将request 数据转换为Resp
     *
     * @param user    用户
     * @param request 前端传过来的消息体
     * @return resp
     */
    ChatMessageResp getMsgResp(User user, ChatMessagePyReq request);

    /**
     * 将消息体解构成保存聊天消息的实体类
     * @param resp
     * @param pyId
     * @return
     */
    ChatRecord getCRByCMR(ChatMessageResp resp, Long pyId);

    ChatMessageResp getMsgResp(User user, TeamChatRecord teamChatRecord);

    boolean ChatMessagePyReqEmpty(ChatMessagePyReq chatMessagePyReq);

    List<Long> selectTeamByUser(Long id, Long teamId);


    TeamChatRecord geCRTByCMR(ChatMessageResp res, Long teamId);

    /**
     * 处理队伍未读消息
     *
     * @param tuid 队伍的用户id
     * @param teamId
     */
    void unReadTeam(Long tuid, Long teamId);

    /**
     * 将用户消息构建成返回给前端的聊天用户的消息体
     * @param user
     * @return
     */
    ChatMessageResp.UserInfo buildUserInfo(User user);

    /**
     * 好友发送消息
     *
     * @param request
     * @return
     */
    B<ChatMessageResp> sendMsg(ChatMessagePyReq request);

    /**
     * gpt聊天
     *
     * @param request
     * @return
     */
    B<ChatMessageResp> sendGPT(ChatMessagePyReq request);

    /**
     * 队伍发送消息
     * @param request
     * @return
     */
    B<ChatMessageResp> sendTeam(ChatMessagePyReq request);

    /**
     * 获取好友的聊天消息
     * @param request
     * @return
     */
    B<CursorPageResp<List<ChatMessageResp>>> selectChatUserList(CursorPageReq request);

    /**
     * 获取队伍的消息
     * @param request
     * @return
     */
    B<CursorPageResp<List<ChatMessageResp>>> selectChatTeamList(CursorPageReq request);
}
