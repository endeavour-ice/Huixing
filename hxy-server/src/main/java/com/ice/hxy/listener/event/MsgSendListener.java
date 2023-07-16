package com.ice.hxy.listener.event;


import com.ice.hxy.mode.chat.ChatMessagePyReq;
import com.ice.hxy.mode.chat.ChatMessageResp;
import com.ice.hxy.mode.chat.ChatType;
import com.ice.hxy.mode.chat.SocketResp;
import com.ice.hxy.mode.entity.ChatRecord;
import com.ice.hxy.mode.entity.TeamChatRecord;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.event.MsgSendGPTEvent;
import com.ice.hxy.mode.event.MsgSendPyEvent;
import com.ice.hxy.mode.event.MsgSendTeamEvent;
import com.ice.hxy.service.ChatService;
import com.ice.hxy.service.UserService.IUserService;
import com.ice.hxy.service.WebSocketService;
import com.ice.hxy.util.ChatGptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.ice.hxy.mode.constant.UserConstant.ASSISTANT_ID;

/**
 * @Author ice
 * @Date 2023/5/21 16:37
 * @Description: 发送消息监听器
 */
@Component
public class MsgSendListener {
    @Resource
    private WebSocketService webSocketService;
    @Autowired
    private ChatService chatService;

    @Autowired
    private IUserService userService;
    @Async(value = "executorService")
    @EventListener(classes = MsgSendPyEvent.class)
    public void notifyPyOnline(MsgSendPyEvent msgSendPyEvent) {
        Long pyId = msgSendPyEvent.getPyId();
        ChatMessageResp res = msgSendPyEvent.getResp();
        ChatRecord chatRecord = chatService.getCRByCMR(res, pyId);
        SocketResp<ChatMessageResp> resp = new SocketResp<>();
        resp.setType(ChatType.FD.getValue());
        resp.setData(res);
        webSocketService.sendPyOnline(resp, chatRecord);
    }
    @Async(value = "executorService")
    @EventListener(classes = MsgSendTeamEvent.class)
    public void notifyTeamOnline(MsgSendTeamEvent teamEvent) {
        ChatMessageResp res = teamEvent.getResp();
        Long teamId = teamEvent.getTeamId();
        SocketResp<ChatMessageResp> resp = new SocketResp<>();
        resp.setType(ChatType.TM.getValue());
        resp.setData(res);
        TeamChatRecord teamChatRecord= chatService.geCRTByCMR(res, teamId);
        Long uid = res.getUserInfo().getUid();
        List<Long> tuids= chatService.selectTeamByUser(uid, teamId);
        webSocketService.sendTeamOnline(resp, tuids,teamChatRecord);
    }

    @Async(value = "executorService")
    @EventListener(classes = MsgSendGPTEvent.class)
    public void notifyGptOnline(MsgSendGPTEvent teamEvent) {
        Long uid = teamEvent.getUid();
        User user = userService.getById(ASSISTANT_ID);
        ChatMessagePyReq messagePyReq = new ChatMessagePyReq();
        String message = ChatGptUtils.sendChatP(teamEvent.getMessage());
        messagePyReq.setContent(message);
        ChatMessageResp msgResp = chatService.getMsgResp(user, messagePyReq);
        SocketResp<ChatMessageResp> resp = new SocketResp<>();
        resp.setType(ChatType.GP.getValue());
        resp.setData(msgResp);
        ChatRecord record = chatService.getCRByCMR(msgResp, uid);
        webSocketService.sendGPTOnline(resp, uid,record);
    }

}
