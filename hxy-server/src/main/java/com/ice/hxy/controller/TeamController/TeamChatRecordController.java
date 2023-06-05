package com.ice.hxy.controller.TeamController;


import com.ice.hxy.service.chatService.ITeamChatRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 队伍聊天记录表 前端控制器
 * </p>
 *
 * @author ice
 * @since 2022-09-12
 */
@RestController
@RequestMapping("/partner/teamChatRecord")
public class TeamChatRecordController {

    @Autowired
    private ITeamChatRecordService chatRecordService;



}
