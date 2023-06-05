package com.ice.hxy.service.chatService;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.dto.TeamQuery;
import com.ice.hxy.mode.entity.Team;
import com.ice.hxy.mode.entity.vo.TeamUserAvatarVo;
import com.ice.hxy.mode.entity.vo.TeamUserVo;
import com.ice.hxy.mode.request.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author BING
 * @description 针对表【team(队伍表)】的数据库操作Service
 * @createDate 2022-08-22 15:45:11
 */
public interface TeamService extends IService<Team> {


    /**
     * 查看队伍是否存在
     * @param id
     */
    boolean teamById(Long id);

    /**
     * 查看用户是否在该队伍中
     * @param teamId
     * @param uid
     * @return
     */
    boolean isUserTeam(Long teamId, Long uid);

    Team getTeamByUser(Long teamId, Long uid);
    /**
     * 创建队伍
     *
     * @param team
     * @return
     */
    B<Long> addTeam(TeamAddRequest team);


    /**
     * 查询队伍列表
     *
     * @param teamQuery
     * @return
     */
    B<List<TeamUserVo>> getTeamList(TeamQuery teamQuery);


    /**
     * 添加队伍
     *
     * @param teamJoinRequest
     * @param request
     * @return
     */
    B<Boolean> addUserTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request);

    /**
     * 修改队伍
     *
     * @param teamUpdateRequest
     * @return
     */
    B<Boolean> updateTeam(TeamUpdateRequest teamUpdateRequest);

    /**
     * 退出队伍
     *
     * @param teamId  队伍的id
     * @param request 登录用户
     * @return
     */
    B<Boolean> quitTeam(Long teamId, HttpServletRequest request);

    /**
     * 查看用户加入的队伍
     *
     * @return 200
     */
    B<List<TeamUserAvatarVo>> getJoinTeamList();

    /**
     * 根据id获取信息
     *
     * @param id
     * @return
     */
    B<TeamUserVo> getByTeamId(Long id);

    B<Boolean> quitTeamByUser(UserTeamQuitRequest userTeamQuitRequest, HttpServletRequest request);


    List<Long> getUserTeamListById(Long teamId, Long userId);

    Team getTeamByTeamUser(Long teamId, Long userId);

    boolean updateTeamByTeam(Team team);


    B<Map<String, Object>> getTeamIdByChat(IdRequest idRequest);

    B<TeamUserAvatarVo> getByTeamAvatarId(Long id);



    /**
     * 是否是队伍管理员
     * @param teamId
     * @param userId
     * @return
     */
    boolean isAdminTeam(Long teamId, Long userId);
}
