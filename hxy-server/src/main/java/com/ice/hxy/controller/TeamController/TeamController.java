package com.ice.hxy.controller.TeamController;


import com.ice.hxy.common.B;
import com.ice.hxy.mode.dto.TeamQuery;
import com.ice.hxy.mode.entity.vo.TeamUserAvatarVo;
import com.ice.hxy.mode.entity.vo.TeamUserVo;
import com.ice.hxy.mode.request.*;
import com.ice.hxy.service.chatService.TeamService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author ice
 * @date 2022/8/22 16:02
 */
@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    private TeamService teamService;

    @PostMapping("/add")
    public B<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest) {
        return teamService.addTeam(teamAddRequest);
    }


    @PostMapping("/update")
    public B<Boolean> update(@RequestBody TeamUpdateRequest teamUpdateRequest) {
        return teamService.updateTeam(teamUpdateRequest);
    }

    @GetMapping("/name")
    public B<TeamUserAvatarVo> userAvatarVoB(@RequestParam("id") Long id) {
        return teamService.getByTeamAvatarId(id);
    }

    @GetMapping("/get")
    public B<TeamUserVo> getTeamById(@RequestParam("id") Long id) {
        return teamService.getByTeamId(id);
    }


    @PostMapping ("/search")
    public B<List<TeamUserVo>> getTeamList(TeamQuery teamQuery) {
        return teamService.getTeamList(teamQuery);
    }


    @GetMapping("/check")
    public B<List<TeamUserAvatarVo>> getJoinTeamList() {
        return teamService.getJoinTeamList();
    }

    @PostMapping("/join")
    public B<Boolean> addUserTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        return teamService.addUserTeam(teamJoinRequest, request);
    }

    /**
     * 退出队伍
     *
     * @param teamId  队伍id
     * @param request 响应
     * @return 返回
     */
    @GetMapping("/quit")
    public B<Boolean> quitTeam(@RequestParam Long teamId, HttpServletRequest request) {
        return teamService.quitTeam(teamId, request);
    }

    /**
     * 队伍队长 退队员
     *
     * @param request 队伍id
     * @return b
     */
    @PostMapping("/quitUserTeam")
    public B<Boolean> quitTeamByUser(@RequestBody UserTeamQuitRequest userTeamQuitRequest, HttpServletRequest request) {
        return teamService.quitTeamByUser(userTeamQuitRequest, request);
    }

    @PostMapping("/chat/team")
    public B<Map<String, Object>> getTeamIdByChat(@RequestBody IdRequest idRequest) {
        return teamService.getTeamIdByChat(idRequest);
    }



}
