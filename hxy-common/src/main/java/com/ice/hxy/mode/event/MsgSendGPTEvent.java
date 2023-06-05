package com.ice.hxy.mode.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @Author ice
 * @Date 2023/5/22 17:36
 * @Description: TODO
 */
@Getter
public class MsgSendGPTEvent extends ApplicationEvent {
    private static final long serialVersionUID = -2026420190307144596L;
    private final Long uid;
    private final String message;
    public MsgSendGPTEvent(Object source, Long uid, String message) {
        super(source);
        this.uid = uid;
        this.message = message;
    }
}
