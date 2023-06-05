package com.ice.hxy.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.hxy.mode.entity.ChatRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 * 聊天记录表 Mapper 接口
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
@Mapper
public interface ChatRecordMapper extends BaseMapper<ChatRecord> {

    int updateReadBatchById(@Param("ids") List<Long> ids);

    List<ChatRecord> selectAllByUserIdAndFriendId(@Param("uid") Long uid, @Param("fid") Long fid,@Param("time") LocalDateTime time,@Param("size") int size);

    int selectUserAddFriend(@Param("userId") Long userId, @Param("friendId") Long friendId);
}
