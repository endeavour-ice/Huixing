package com.ice.hxy.listener.event;


import com.ice.hxy.mode.chat.ChatMessageResp;
import com.ice.hxy.mode.chat.ChatType;
import com.ice.hxy.mode.chat.SocketResp;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.event.UserOnlineEvent;
import com.ice.hxy.service.ChatService;
import com.ice.hxy.service.UserService.IUserFriendService;
import com.ice.hxy.service.WebSocketService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/5/24 20:13
 * @Description: 用户上线通知
 */
@Component
public class UserOnlineListener {
    @Resource
    private IUserFriendService friendService;
    @Resource
    private WebSocketService webSocketService;
    @Resource
    private ChatService chatService;
    /**
     * 通知好友上线
     * @param userOnlineEvent
     */
    @Async(value = "executorService")
    @EventListener(classes = UserOnlineEvent.class)
    public void pyOnline(UserOnlineEvent userOnlineEvent) {
        User user = userOnlineEvent.getUser();
        List<Long> fids = friendService.selectFriendRId(user.getId());
        if (fids == null || fids.size() <= 0) {
            return;
        }
        ChatMessageResp.UserInfo userInfo= chatService.buildUserInfo(user);
        SocketResp<ChatMessageResp.UserInfo> socketResp = new SocketResp<>();
        socketResp.setType(ChatType.ADV.getValue());
        socketResp.setData(userInfo);
        webSocketService.sendPyOnlineNotice(socketResp,fids);
    }

    /**
     * 通知队伍上线
     * @param userOnlineEvent
     */
    @Async(value = "executorService")
    @EventListener(classes = UserOnlineEvent.class)
    public void teamOnline(UserOnlineEvent userOnlineEvent) {

    }
}
