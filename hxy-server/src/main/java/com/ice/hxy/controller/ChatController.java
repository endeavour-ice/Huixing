package com.ice.hxy.controller;

import com.ice.hxy.annotation.CurrentLimiting;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.chat.ChatMessagePyReq;
import com.ice.hxy.mode.chat.ChatMessageResp;
import com.ice.hxy.mode.request.CursorPageReq;
import com.ice.hxy.mode.resp.CursorPageResp;
import com.ice.hxy.service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/6/1 9:54
 * @Description: TODO
 */
@RestController
@RequestMapping("/chat")
public class ChatController {
    @Resource
    private ChatService chatService;

    @PostMapping("/list")
    @CurrentLimiting
    public B<CursorPageResp<List<ChatMessageResp>>> selectChatUserList(@RequestBody CursorPageReq request) {
        return chatService.selectChatUserList(request);
    }
    @PostMapping("/team/list")
    @CurrentLimiting
    public B<CursorPageResp<List<ChatMessageResp>>> selectChatTeamList(@RequestBody CursorPageReq request) {
        return chatService.selectChatTeamList(request);
    }
    @PostMapping("/send")
    @CurrentLimiting
    public B<ChatMessageResp> sendMsg( @RequestBody ChatMessagePyReq request) {
        return chatService.sendMsg(request);
    }

    @PostMapping("/gpt/send")
    @CurrentLimiting
    public B<ChatMessageResp> sendGPTMsg( @RequestBody ChatMessagePyReq request) {
        return chatService.sendGPT(request);
    }

    @PostMapping("/team/send")
    @CurrentLimiting
    public B<ChatMessageResp> send( @RequestBody ChatMessagePyReq request) {
        return chatService.sendTeam(request);
    }
}
