package com.ice.hxy.service.chatService;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.hxy.mode.entity.TeamChatRecord;

/**
 * <p>
 * 队伍聊天记录表 服务类
 * </p>
 *
 * @author ice
 * @since 2022-09-12
 */
public interface ITeamChatRecordService extends IService<TeamChatRecord> {

    /**
     * 根据team id 删除队伍的聊天信息
     *
     * @param teamId
     * @return
     */
    boolean deleteTeamChatRecordByTeamId(Long teamId);



}
