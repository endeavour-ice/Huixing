package com.ice.hxy.mode.event;

import com.ice.hxy.mode.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @Author ice
 * @Date 2023/5/24 20:06
 * @Description: 用户上线通知
 */
@Getter
public class UserOnlineEvent extends ApplicationEvent {
    private static final long serialVersionUID = 5161479718786091455L;
    private final User user;

    public UserOnlineEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
