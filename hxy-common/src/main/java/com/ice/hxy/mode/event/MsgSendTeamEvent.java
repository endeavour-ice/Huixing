package com.ice.hxy.mode.event;

import com.ice.hxy.mode.chat.ChatMessageResp;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @Author ice
 * @Date 2023/5/22 10:23
 * @Description: TODO
 */
@Getter
public class MsgSendTeamEvent extends ApplicationEvent {

    private static final long serialVersionUID = -9134715852315653304L;

    private final ChatMessageResp resp;
    private final Long teamId;

    public MsgSendTeamEvent(Object source, ChatMessageResp resp,Long teamId) {
        super(source);
        this.resp = resp;
        this.teamId = teamId;
    }
}
