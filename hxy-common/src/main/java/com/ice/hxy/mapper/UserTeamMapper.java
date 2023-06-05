package com.ice.hxy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.hxy.mode.entity.UserTeam;
import org.apache.ibatis.annotations.Mapper;

/**
* @author BING
* @description 针对表【user_team(队伍表)】的数据库操作Mapper
* @createDate 2022-08-22 15:55:33
* @Entity com.user.usercenter.model.domain.UserTeam
*/
@Mapper
public interface UserTeamMapper extends BaseMapper<UserTeam> {

}




