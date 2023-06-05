package com.ice.hxy.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.hxy.mode.entity.UserFriend;
import com.ice.hxy.mode.entity.UserFriendReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
@Mapper
public interface UserFriendMapper extends BaseMapper<UserFriend> {

    List<UserFriendReq> selectCheckFriend(@Param("toUserId") Long toUserId);

    int removeFromToUserId(@Param("fromUserId")Long fromUserId,@Param("toUserId") Long toUserId);

    int countFromToUserId(@Param("fromUserId")Long fromUserId,@Param("toUserId") Long toUserId);

    Integer removeChatRecord(@Param("userId") Long userId,@Param("friendId") Long friendId);
}
