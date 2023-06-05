package com.ice.hxy.service.impl.chat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.hxy.mapper.TeamChatRecordMapper;
import com.ice.hxy.mode.entity.TeamChatRecord;
import com.ice.hxy.service.chatService.ITeamChatRecordService;
import com.ice.hxy.util.LongUtil;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 队伍聊天记录表 服务实现类
 * </p>
 *
 * @author ice
 * @since 2022-09-12
 */
@Service
public class TeamChatRecordServiceImpl extends ServiceImpl<TeamChatRecordMapper, TeamChatRecord>
        implements ITeamChatRecordService {


    @Override
    public boolean deleteTeamChatRecordByTeamId(Long teamId) {
        if (LongUtil.isEmpty(teamId)) {
            return false;
        }
        QueryWrapper<TeamChatRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("team_id", teamId);
        long count = this.count(wrapper);
        if (count == 0) {
            return true;
        }
        return this.remove(wrapper);

    }

}
