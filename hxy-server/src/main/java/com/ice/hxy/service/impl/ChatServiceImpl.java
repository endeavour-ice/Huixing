package com.ice.hxy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.hxy.common.B;
import com.ice.hxy.designPatten.factory.AsyncFactory;
import com.ice.hxy.mode.chat.ChatMessagePyReq;
import com.ice.hxy.mode.chat.ChatMessageResp;
import com.ice.hxy.mode.entity.*;
import com.ice.hxy.mode.entity.vo.UserAvatarVo;
import com.ice.hxy.mode.event.MsgSendGPTEvent;
import com.ice.hxy.mode.event.MsgSendPyEvent;
import com.ice.hxy.mode.event.MsgSendTeamEvent;
import com.ice.hxy.mode.request.CursorPageReq;
import com.ice.hxy.mode.resp.CursorPageResp;
import com.ice.hxy.mq.RabbitService;
import com.ice.hxy.service.ChatService;
import com.ice.hxy.service.UserService.IUserFriendService;
import com.ice.hxy.service.UserService.IUserService;
import com.ice.hxy.service.UserService.UserTeamService;
import com.ice.hxy.service.chatService.IChatRecordService;
import com.ice.hxy.service.chatService.ITeamChatRecordService;
import com.ice.hxy.service.chatService.TeamService;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.util.*;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ice.hxy.mode.constant.UserConstant.ASSISTANT_ID;

/**
 * @Author ice
 * @Date 2023/5/21 21:33
 * @Description: 聊天服务类
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Resource
    private TeamService teamService;
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private RedisCache redisCache;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private IUserFriendService friendService;
    @Resource
    private RabbitService rabbitService;
    @Resource
    private IChatRecordService chatRecordService;
    @Resource
    private IUserService userService;
    @Resource
    private ITeamChatRecordService teamChatRecordService;

    @Override
    public B<ChatMessageResp> sendMsg(ChatMessagePyReq request) {
        if (ChatMessagePyReqEmpty(request)) {
            return B.parameter("消息内容错误");
        }
        Long pyId = request.getAcceptId();
        User loginUser = UserUtils.getLoginUser();
        if (!friendService.isUserFriend(loginUser.getId(), pyId)) {
            return B.parameter();
        }
        ChatMessageResp resp = getMsgResp(loginUser, request);
        eventPublisher.publishEvent(new MsgSendPyEvent(this, resp, pyId));
        return B.ok(resp);
    }

    @Override
    public B<ChatMessageResp> sendGPT(ChatMessagePyReq request) {
        if (ChatMessagePyReqEmpty(request)) {
            return B.parameter("消息内容错误");
        }
        String content = request.getContent();
        User loginUser = UserUtils.getLoginUser();
        Long loginUserId = loginUser.getId();
        ChatMessageResp msgResp = getMsgResp(loginUser, request);
        ChatRecord cmr = getCRByCMR(msgResp, ASSISTANT_ID);
        rabbitService.sendNettyChat(cmr);
        eventPublisher.publishEvent(new MsgSendGPTEvent(this, loginUserId, content));
        return B.ok(msgResp);
    }


    @Override
    public B<ChatMessageResp> sendTeam(ChatMessagePyReq request) {
        if (ChatMessagePyReqEmpty(request)) {
            return B.parameter("消息内容错误");
        }
        User loginUser = UserUtils.getLoginUser();
        Long teamId = request.getAcceptId();
        Long loginUserId = loginUser.getId();
        Team team = teamService.getTeamByTeamUser(teamId, loginUserId);
        if (team == null) {
            return B.parameter("请先加入该队伍");
        }
        ChatMessageResp msgResp = getMsgResp(loginUser, request);
        eventPublisher.publishEvent(new MsgSendTeamEvent(this, msgResp, teamId));
        return B.ok(msgResp);
    }

    @Override
    public B<CursorPageResp<List<ChatMessageResp>>> selectChatTeamList(CursorPageReq request) {
        User loginUser = UserUtils.getLoginUser();
        if (CursorPageReq.isEmpty(request)) {
            return B.parameter();
        }
        Long id = request.getId();
        Long uid = loginUser.getId();
        Integer count = request.getCount();
        Long cursor = request.getCursor();
        LocalDateTime time;
        if (LongUtil.isEmpty(cursor)) {
            time = LocalDateTime.now();
        } else {
            try {
                time = DateUtils.getLocalTimeByTimestamp(count);
            } catch (Exception e) {
                log.error("selectChatTeamList getLocalTimeByTimestamp param:{} error:{}", count, e.getMessage());
                return B.parameter();
            }
        }
        if (LongUtil.isEmpty(id)) {
            return B.parameter();
        }
        boolean userTeam = teamService.isUserTeam(id, uid);
        if (!userTeam) {
            return B.parameter();
        }
        List<Long> userIds = userTeamService.lambdaQuery()
                .eq(UserTeam::getTeamId, id)
                .select(UserTeam::getUserId)
                .list().stream().map(UserTeam::getUserId).collect(Collectors.toList());
        List<UserAvatarVo> userAvatarVoByIds = userService.getUserAvatarVoByIds(userIds);
        if (CollectionUtils.isEmpty(userAvatarVoByIds)) {
            return B.parameter();
        }
        Map<Long, UserAvatarVo> map = userAvatarVoByIds.stream().collect(Collectors.toMap(UserAvatarVo::getId, i -> i));
        Page<TeamChatRecord> teamChatRecordPage = new Page<>(1, count);
        Page<TeamChatRecord> page = teamChatRecordService.lambdaQuery().eq(TeamChatRecord::getTeamId, id)
                .lt(TeamChatRecord::getCreateTime, time)
                .orderByDesc(TeamChatRecord::getCreateTime)
                .page(teamChatRecordPage);
        List<TeamChatRecord> chatRecords = page.getRecords();
        CursorPageResp<List<ChatMessageResp>> resp = new CursorPageResp<>();
        if (CollectionUtils.isEmpty(chatRecords)) {
            resp.setData(new ArrayList<>());
            resp.setCursor(cursor);
            resp.setIsLast(page.hasNext());
            return B.ok(resp);
        }
        long localTimestamp = DateUtils.getLocalTimestamp(chatRecords.get(chatRecords.size() - 1).getCreateTime());
        List<ChatMessageResp> chatMessageResps = buildTeamMessage(chatRecords, map);
        resp.setData(chatMessageResps);
        resp.setCursor(localTimestamp);
        resp.setIsLast(page.hasNext());
        return B.ok(resp);
    }

    @Override
    public B<CursorPageResp<List<ChatMessageResp>>> selectChatUserList(CursorPageReq request) {
        User loginUser = UserUtils.getLoginUser();
        Long userId = loginUser.getId();
        if (CursorPageReq.isEmpty(request)) {
            return B.parameter();
        }
        Long fid = request.getId();
        if (LongUtil.isEmpty(fid)) {
            return B.parameter();
        }
        Long cursor = request.getCursor();
        Integer count = request.getCount();
        User user = userService.getById(fid);
        if (user == null) {
            return B.parameter();
        }
        CursorPageResp<List<ChatMessageResp>> resp = new CursorPageResp<>();
        List<ChatMessageResp> resps = new ArrayList<>();
        List<ChatRecord> records = chatRecordService.selectChatListByUserIdAndFriendIdCursor(userId, fid, cursor, count);
        if (CollectionUtils.isEmpty(records)) {
            resp.setCursor(request.getCursor());
            resp.setIsLast(true);
            resp.setData(resps);
            return B.ok(resp);
        }
        int size = records.size();
        for (int i = size - 1; i >= 0; i--) {
            ChatRecord chatRecord = records.get(i);
            Long uid = chatRecord.getUserId();
            if (userId.equals(uid)) {
                resps.add(getMsgRespByChatRecord(chatRecord, loginUser));
            } else {
                resps.add(getMsgRespByChatRecord(chatRecord, user));
            }
        }
        resp.setCursor(resps.get(size - 1).getMessage().getSendTime());
        resp.setData(resps);
        resp.setIsLast(size != request.getCount());
        return B.ok(resp);
    }

    private ChatMessageResp getMsgRespByChatRecord(ChatRecord record, User user) {
        ChatMessageResp resp = new ChatMessageResp();
        resp.setUserInfo(buildUserInfo(user));
        resp.setMessage(buildMessage(record));
        return resp;
    }


    public ChatMessageResp getMsgResp(User user, ChatMessagePyReq request, Long mid) {
        ChatMessageResp resp = new ChatMessageResp();
        resp.setUserInfo(buildUserInfo(user));
        resp.setMessage(buildMessage(request, mid));
        return resp;
    }

    @Override
    public ChatMessageResp getMsgResp(User user, ChatMessagePyReq request) {
        long mid = SnowFlake.getSnowLong();
        return getMsgResp(user, request, mid);
    }

    @Override
    public ChatRecord getCRByCMR(ChatMessageResp resp, Long pyId) {
        return buildChatRecord(resp, pyId);
    }

    @Override
    public ChatMessageResp getMsgResp(User user, TeamChatRecord teamChatRecord) {
        ChatMessageResp resp = new ChatMessageResp();
        resp.setUserInfo(buildUserInfo(user));
        resp.setMessage(buildMessage(teamChatRecord));
        return null;
    }


    public ChatMessageResp.UserInfo buildUserInfo(User user) {
        ChatMessageResp.UserInfo userInfo = new ChatMessageResp.UserInfo();
        userInfo.setUsername(user.getUserAccount());
        userInfo.setAvatar(user.getAvatarUrl());
        userInfo.setUid(user.getId());
        return userInfo;
    }

    public ChatMessageResp.UserInfo buildUserInfo(UserAvatarVo user) {
        ChatMessageResp.UserInfo userInfo = new ChatMessageResp.UserInfo();
        userInfo.setUsername(user.getUsername());
        userInfo.setAvatar(user.getAvatarUrl());
        userInfo.setUid(user.getId());
        return userInfo;
    }

    private List<ChatMessageResp> buildTeamMessage(List<TeamChatRecord> chatRecordList, Map<Long, UserAvatarVo> userAvatarVoMap) {
        ArrayList<ChatMessageResp> chatMessageResps = new ArrayList<>();
        for (TeamChatRecord teamChatRecord : chatRecordList) {
            ChatMessageResp chatMessageResp = new ChatMessageResp();
            ChatMessageResp.Message message = buildMessage(teamChatRecord);
            Long userId = teamChatRecord.getUserId();
            UserAvatarVo userAvatarVo = userAvatarVoMap.get(userId);
            ChatMessageResp.UserInfo userInfo = buildUserInfo(userAvatarVo);
            chatMessageResp.setMessage(message);
            chatMessageResp.setUserInfo(userInfo);
            chatMessageResps.add(chatMessageResp);
        }
        return chatMessageResps;
    }

    private ChatMessageResp.Message buildMessage(ChatMessagePyReq request, Long mid) {
        ChatMessageResp.Message message = new ChatMessageResp.Message();
        message.setId(mid);
        message.setSendTime(DateUtils.getLocalTimestamp(LocalDateTime.now()));
        message.setContent(request.getContent());
        return message;
    }

    private ChatMessageResp.Message buildMessage(ChatRecord record) {
        ChatMessageResp.Message message = new ChatMessageResp.Message();
        message.setId(record.getId());
        message.setSendTime(DateUtils.getLocalTimestamp(record.getSendTime()));
        message.setContent(record.getMessage());
        message.setStatus(record.getStatus());
        return message;
    }

    private ChatRecord buildChatRecord(ChatMessageResp resp, Long pyId) {
        ChatMessageResp.Message message = resp.getMessage();
        ChatMessageResp.UserInfo userInfo = resp.getUserInfo();
        ChatRecord chatRecord = new ChatRecord();
        chatRecord.setId(message.getId());
        chatRecord.setUserId(userInfo.getUid());
        chatRecord.setAcceptId(pyId);
        chatRecord.setSendTime(DateUtils.getLocalTimeByTimestamp(message.getSendTime()));
        chatRecord.setMessage(message.getContent());
        return chatRecord;
    }

    private TeamChatRecord buildTeamChatRecord(ChatMessageResp resp, Long teamId) {
        ChatMessageResp.Message message = resp.getMessage();
        ChatMessageResp.UserInfo userInfo = resp.getUserInfo();
        TeamChatRecord teamChatRecord = new TeamChatRecord();
        teamChatRecord.setId(message.getId());
        teamChatRecord.setUserId(userInfo.getUid());
        teamChatRecord.setTeamId(teamId);
        teamChatRecord.setMessage(message.getContent());
        teamChatRecord.setCreateTime(DateUtils.getLocalTimeByTimestamp(message.getSendTime()));
        return teamChatRecord;
    }

    private ChatMessageResp.Message buildMessage(TeamChatRecord chatRecord) {
        ChatMessageResp.Message message = new ChatMessageResp.Message();
        message.setId(chatRecord.getId());
        message.setSendTime(DateUtils.getLocalTimestamp(chatRecord.getCreateTime()));
        message.setContent(chatRecord.getMessage());
        message.setStatus(chatRecord.getStatus());
        return message;
    }

    @Override
    public boolean ChatMessagePyReqEmpty(ChatMessagePyReq chatMessagePyReq) {
        if (chatMessagePyReq == null) {
            return true;
        }
        String content = chatMessagePyReq.getContent();
        Long acceptId = chatMessagePyReq.getAcceptId();
        Long replyMsgId = chatMessagePyReq.getReplyMsgId();
        if (!StringUtils.hasText(content)) {
            return true;
        }
        if (content.length() > 10000) {
            return true;
        }
        if (acceptId != null) {
            return acceptId <= 0;
        }
        if (replyMsgId != null) {
            return replyMsgId <= 0;
        }
        return false;
    }


    @Override
    public List<Long> selectTeamByUser(Long id, Long teamId) {
        return teamService.getUserTeamListById(teamId,id);
    }

    @Override
    public TeamChatRecord geCRTByCMR(ChatMessageResp res, Long teamId) {
        return buildTeamChatRecord(res, teamId);
    }

    private static final Map<String, ExpiringMap<String, Integer>> map = new ConcurrentHashMap<>();

    @Override
    public void unReadTeam(Long tuid, Long teamId) {
        String key = unReadTeamKey(teamId, tuid);
        int num = 1;
        if (redisCache.hasKey(key)) {
            num = redisCache.getCacheObject(key);
            num++;
        }
        redisCache.setCacheObject(key, num, 4, TimeUnit.SECONDS);
        Threads.time().execute(AsyncFactory.upTimeUNReadNum(tuid, teamId, key), 3);
    }

    private String unReadTeamKey(Long teamID, Long tuid) {
        return "un_read_team:" + teamID + ":" + tuid;
    }

}
