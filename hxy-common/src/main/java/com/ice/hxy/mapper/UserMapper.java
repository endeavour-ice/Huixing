package com.ice.hxy.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.entity.vo.UserAvatarVo;
import com.ice.hxy.mode.resp.SafetyUserResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author ice
 * @since 2022-06-14
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<UserAvatarVo> getUserAvatarVoByIds(@Param("ids") List<Long> ids);
    UserAvatarVo getUserAvatarVoById(@Param("id") String id);

    long getUserCount();

    List<SafetyUserResponse> selectFindByUserAccountLikePage(@Param("pageNum") long pageNum, @Param("pageSize")long pageSize, @Param("userName")String userName);


    SafetyUserResponse selectByNameLike(@Param("name") String name);

    List<SafetyUserResponse> selectUserVoList(@Param("current") long current, @Param("size") long size, @Param("status") int status);

    int saveFriend(@Param("id")String id,@Param("userId") Long userId,@Param("friendId") Long friendId);

    User selectByLogin(@Param("userAccount") String userAccount,@Param("passwordMD5") String passwordMD5);
}
