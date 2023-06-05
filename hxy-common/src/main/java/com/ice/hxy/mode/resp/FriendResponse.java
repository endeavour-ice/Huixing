package com.ice.hxy.mode.resp;

import com.ice.hxy.mode.entity.vo.UserAvatarVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author ice
 * @Date 2023/5/13 23:33
 * @Description: 返回朋友信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FriendResponse extends UserAvatarVo {
    private boolean online = false;
}
