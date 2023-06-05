package com.ice.hxy.service.impl.chat;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.hxy.mapper.ChatHalRecordMapper;
import com.ice.hxy.mode.entity.ChatHalRecord;
import com.ice.hxy.service.chatService.IChatHalRecordService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 大厅聊天记录表 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-04-24
 */
@Service
public class ChatHalRecordServiceImpl extends ServiceImpl<ChatHalRecordMapper, ChatHalRecord> implements IChatHalRecordService {

}
