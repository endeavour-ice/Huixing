package com.ice.hxy.util;

import com.ice.hxy.service.WebSocketService;

/**
 * @Author ice
 * @Date 2023/5/25 18:41
 * @Description: TODO
 */
public class SocketUtil {
    private static final WebSocketService webSocketService = SpringUtil.getBean(WebSocketService.class);

    public static void remove(Long id) {
        webSocketService.close(id);
    }
}
