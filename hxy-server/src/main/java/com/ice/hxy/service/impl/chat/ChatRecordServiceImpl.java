package com.ice.hxy.service.impl.chat;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.exception.GlobalException;
import com.ice.hxy.mapper.ChatRecordMapper;
import com.ice.hxy.mode.entity.ChatRecord;
import com.ice.hxy.service.chatService.IChatRecordService;
import com.ice.hxy.util.DateUtils;
import com.ice.hxy.util.LongUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 聊天记录表 服务实现类
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
@Service
@Slf4j
public class ChatRecordServiceImpl extends ServiceImpl<ChatRecordMapper, ChatRecord> implements IChatRecordService {


    @Override
    public List<ChatRecord> selectChatListByUserIdAndFriendIdCursor(Long userId, Long fid, Long cursor, Integer count) {
        LocalDateTime time;
        if (!LongUtil.isEmpty(cursor)) {
            try {
                time = DateUtils.getLocalTimeByTimestamp(cursor);
            } catch (Exception e) {
                log.error("selectChatListByUserIdAndFriendIdCursor 解析参数错误 error:{}", e.getMessage());
                throw new GlobalException(ErrorCode.PARAMS_ERROR);
            }
        } else {
            time = LocalDateTime.now();
        }
        return baseMapper.selectAllByUserIdAndFriendId(userId, fid, time, count);
    }
}
