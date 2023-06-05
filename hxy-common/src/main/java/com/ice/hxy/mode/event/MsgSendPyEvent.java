package com.ice.hxy.mode.event;

import com.ice.hxy.mode.chat.ChatMessageResp;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @Author ice
 * @Date 2023/5/21 16:41
 * @Description: TODO
 */
@Getter
public class MsgSendPyEvent extends ApplicationEvent {
    private static final long serialVersionUID = -6208140705526212198L;
    private final ChatMessageResp resp;
    private final Long pyId;
    public MsgSendPyEvent(Object source, ChatMessageResp resp, Long pyId) {
        super(source);
        this.resp = resp;
        this.pyId = pyId;
    }
}
