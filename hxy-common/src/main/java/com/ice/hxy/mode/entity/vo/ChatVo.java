package com.ice.hxy.mode.entity.vo;

import com.ice.hxy.mode.request.IdRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author ice
 * @Date 2023/3/21 10:26
 * @Description: 返回聊天Vo
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class ChatVo  extends IdRequest {
    private static final long serialVersionUID = 6113484189374723453L;
    private Long friendId;
    private Long userId;
    private String message;
    private String sendTime;
}
