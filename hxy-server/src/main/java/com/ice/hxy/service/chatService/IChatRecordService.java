package com.ice.hxy.service.chatService;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.hxy.mode.entity.ChatRecord;

import java.util.List;

/**
 * <p>
 * 聊天记录表 服务类
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
public interface IChatRecordService extends IService<ChatRecord> {




    List<ChatRecord> selectChatListByUserIdAndFriendIdCursor(Long userId, Long fid, Long cursor, Integer count);
}
