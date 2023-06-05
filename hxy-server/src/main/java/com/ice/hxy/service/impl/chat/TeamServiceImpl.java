package com.ice.hxy.service.impl.chat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ice.hxy.annotation.RedissonLock;
import com.ice.hxy.common.B;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.exception.GlobalException;
import com.ice.hxy.mapper.TeamMapper;
import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.dto.TeamQuery;
import com.ice.hxy.mode.entity.Team;
import com.ice.hxy.mode.entity.TeamChatRecord;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.entity.UserTeam;
import com.ice.hxy.mode.entity.vo.TeamUserAvatarVo;
import com.ice.hxy.mode.entity.vo.TeamUserVo;
import com.ice.hxy.mode.entity.vo.UserAvatarVo;
import com.ice.hxy.mode.enums.TeamStatusEnum;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.mode.request.*;
import com.ice.hxy.mode.resp.SafetyUserResponse;
import com.ice.hxy.service.UserService.IUserService;
import com.ice.hxy.service.UserService.UserTeamService;
import com.ice.hxy.service.chatService.ITeamChatRecordService;
import com.ice.hxy.service.chatService.TeamService;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.util.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author BING
 * @description 针对表【team(队伍表)】的数据库操作Service实现
 * @createDate 2022-08-22 15:45:11
 */
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisCache redisCache;
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private IUserService userService;
    @Autowired
    private ITeamChatRecordService chatRecordService;


    @Override
    public B<TeamUserVo> getByTeamId(Long id) {
        if (LongUtil.isEmpty(id)) {
            return B.empty();
        }
        User loginUser = UserUtils.getLoginUser();
        Team team = this.getById(id);
        if (team == null) {
            return B.empty();
        }
        List<UserTeam> userTeams = userTeamService.lambdaQuery()
                .select(UserTeam::getUserId).eq(UserTeam::getTeamId,id).list();
        List<Long> ids = new ArrayList<>();
        for (UserTeam userTeam : userTeams) {
            Long userId = userTeam.getUserId();
            ids.add(userId);
        }
        boolean contains = ids.contains(loginUser.getId());
        if (!contains) {
            return B.empty();
        }
        List<User> list = userService.listByIds(ids);
        ArrayList<SafetyUserResponse> userVos = new ArrayList<>();
        for (User user : list) {
            SafetyUserResponse safetyUserResponse = UserUtils.getSafetyUserResponse(user);
            userVos.add(safetyUserResponse);
        }
        TeamUserVo teamUserVo = new TeamUserVo();
        if (loginUser.getId().equals(team.getUserId())) {
            teamUserVo.setCaptain(true);
        }
        BeanUtils.copyProperties(team, teamUserVo);
        teamUserVo.setUserId(null);
        teamUserVo.setUserVo(userVos);
        return B.ok(teamUserVo);
    }

    @Override
    @RedissonLock
    public B<Long> addTeam(TeamAddRequest team) {
        if (team == null) {
            return B.empty();
        }
        User loginUser = UserUtils.getLoginUser();
        Team tm = new Team();
        tm.setId(tm.getId());
        tm.setName(team.getName());
        tm.setDescription(team.getDescription());
        tm.setMaxNum(team.getMaxNum());
        tm.setPassword(team.getPassword());
        tm.setStatus(team.getStatus());
        Long expireTime = team.getExpireTime();
        tm.setTags(team.getTags());
        if (!LongUtil.isEmpty(expireTime)) {
            LocalDateTime date = DateUtils.getLocalTimeByTimestamp(expireTime);
            tm.setExpireTime(date);
        }
        return B.ok(getAddTeam(tm, loginUser));
    }

    public Long getAddTeam(Team team, User loginUser) {
        //        校验信息
        //队伍人数 > 1 且 <= 20
        long maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0L);
        if (maxNum < 1 || maxNum >= 20) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //队伍标题 <= 20
        String teamName = team.getName();
        if (!StringUtils.hasText(teamName) && teamName.length() >= 20) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        String teamDescription = team.getDescription();
        //描述 <= 512
        if (teamDescription.length() >= 512) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍描述不满足要求");
        }
        //status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        String password = team.getPassword();
        TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusByValue(status);
        //如果 status 是加密状态，一定要有密码，且密码 <= 32
        if (TeamStatusEnum.ENCRYPTION.equals(statusEnum) && (!StringUtils.hasText(password) || password.length() > 32)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍密码不满足要求");
        }
        if (StringUtils.hasText(team.getTags())) {
            String tags = team.getTags();
            Gson gson = GsonUtils.getGson();
            try {
                List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
                }.getType());
                if (CollectionUtils.isEmpty(tagList)) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR, "标签错误");
                }
                team.setTags(gson.toJson(tagList));
            } catch (Exception e) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "标签错误");
            }
        }
        //超时时间 > 当前时间
        LocalDateTime expireTime = team.getExpireTime();
        if (expireTime == null || LocalDateTime.now().isAfter(expireTime)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        //校验用户最多创建 5 个队伍
        Long userId = loginUser.getId();
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        long count = this.count(wrapper);
        if (count > 5) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户最多创建5个队伍");
        }
        //        插入队伍信息到队伍表
        return transactionTemplate.execute(sta -> {
            team.setId(null);
            team.setUserId(userId);
            team.setAvatarUrl(AvatarUrlUtils.getTeam_url().get(0));
            if (redisCache.hasKey(CacheConstants.DEFAULT_AVATAR_TEAM)) {
                List<String> urlList = redisCache.getCacheList(CacheConstants.DEFAULT_AVATAR_TEAM);
                String randomUrl = AvatarUrlUtils.getRandomUrl(urlList);
                if (StringUtils.hasText(randomUrl)) {
                    team.setAvatarUrl(randomUrl);
                }
            }
            int insert = baseMapper.insert(team);
            Long teamId = team.getId();
            if (insert != 1 || LongUtil.isEmpty(teamId)) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "创建失败");
            }
            //插入用户 => 队伍关系到关系表
            UserTeam userTeam = new UserTeam();
            userTeam.setUserId(userId);
            userTeam.setTeamId(teamId);
            userTeam.setJoinTime(LocalDateTime.now());
            boolean result = userTeamService.save(userTeam);
            if (!result) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "创建失败");
            }
            return teamId;
        });

    }


    @Override
    public B<List<TeamUserVo>> getTeamList(TeamQuery teamQuery) {
        boolean isAdmin = false;
        try {
            isAdmin = UserRole.isAdmin();
        } catch (Exception ignored) {
        }
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
        wrapper.and(wr -> wr.gt(true, "expire_time", new Date()).or().isNotNull("expire_time"));
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (!LongUtil.isEmpty(id)) {
                wrapper.eq("id", id);
            }
            String searchTxt = teamQuery.getSearchTxt();
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                wrapper.eq("max_num", maxNum);
            }
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                wrapper.eq("user_id", userId);
            }
            if (StringUtils.hasText(searchTxt)) {
                wrapper.like("name", searchTxt).or().like("description", searchTxt);
            }
            Integer status = teamQuery.getStatus();
            TeamStatusEnum teamStatusByValue = TeamStatusEnum.getTeamStatusByValue(status);

            if (teamStatusByValue == null) {
                wrapper.and(wr -> wr.eq("status", TeamStatusEnum.PUBLIC.getValue())
                        .or().eq("status", TeamStatusEnum.ENCRYPTION.getValue()));
            }
            if (teamStatusByValue != null && isAdmin && teamStatusByValue.equals(TeamStatusEnum.PRIVATE)) {
                wrapper.and(wr -> wr.eq("status", TeamStatusEnum.PUBLIC.getValue()).
                        or().eq("status", TeamStatusEnum.ENCRYPTION.getValue()).
                        or().eq("status", TeamStatusEnum.PRIVATE.getValue()));
            }
        }
        List<Team> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            B.empty();
        }
        List<TeamUserVo> teamUserVos = new ArrayList<>();
        for (Team team : list) {
            Long teamId = team.getId();
            QueryWrapper<UserTeam> teamQueryWrapper = new QueryWrapper<>();
            teamQueryWrapper.eq("team_id", teamId);
            List<UserTeam> userTeams = userTeamService.list(teamQueryWrapper);
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            teamUserVo.setExpireTime(team.getExpireTime());
            if (!CollectionUtils.isEmpty(userTeams)) {
                for (UserTeam userTeam : userTeams) {
                    Long userId = userTeam.getUserId();
                    User userById = userService.getById(userId);
                    if (userById != null) {
                        teamUserVo.getUserVo().add(UserUtils.getSafetyUserResponse(userById));
                    }
                }
            }
            teamUserVos.add(teamUserVo);

        }
        return B.ok(teamUserVos);
    }

    /**
     * 查看用户加入的队伍
     *
     * @return 200
     */
    @Override
    public B<List<TeamUserAvatarVo>> getJoinTeamList() {
        User user = UserUtils.getLoginUser();
        Long userId = user.getId();
        List<TeamUserAvatarVo> userAvatarVos = baseMapper.selectJoinTeamUserList(userId);
        for (TeamUserAvatarVo userAvatarVo : userAvatarVos) {
            Long voUserId = userAvatarVo.getUserId();
            if (userId.equals(voUserId)) {
                userAvatarVo.setCaptain(true);
            }
            userAvatarVo.setUserId(null);
        }
        return B.ok(userAvatarVos);
    }

    /**
     * 加入队伍
     *
     * @param teamJoinRequest team
     * @param request         登录
     * @return v
     */
    @Override
    public B<Boolean> addUserTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            return B.parameter();
        }
        Long teamId = teamJoinRequest.getTeamId();
        if (LongUtil.isEmpty(teamId)) {
            return B.parameter();
        }
        User loginUser = UserUtils.getLoginUser();
        Team team = this.getById(teamId);
        if (team == null) {
            return B.parameter();
        }
        // 判断队伍的过期
        LocalDateTime expireTime = team.getExpireTime();
        if (expireTime == null || LocalDateTime.now().isAfter(expireTime)) {
            return B.parameter("队伍已过期");
        }
        // 判断队伍的权限
        Integer status = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(statusEnum)) {
            return B.parameter("禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.ENCRYPTION.equals(statusEnum)) {
            if (!StringUtils.hasText(password) || !team.getPassword().equals(MD5.getTeamMD5(password))) {
                return B.parameter("密码错误");
            }
        }
        StringBuilder stringBuilder = new StringBuilder("user:team:addUserTeam");
        RLock lock = redissonClient.getLock(stringBuilder.append(teamId).toString().intern());
        try {
            int i = 0;
            while (true) {
                i++;
                if (lock.tryLock(3000, 3000, TimeUnit.MILLISECONDS)) {
                    boolean userTeam = getAddUserTeam(team, teamId, loginUser);
                    if (userTeam) {
                        return B.ok();
                    }
                    return B.error("加入失败");
                }
                if (i > 30) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR, "加入失败");
                }
            }
        } catch (InterruptedException e) {
            log.error("addUserTeam 加锁失败");
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "加入失败");

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean getAddUserTeam(Team team, Long teamId, User loginUser) {
        // 用户加入队伍不超过 5 个
        long count = userTeamService.lambdaQuery().eq(UserTeam::getUserId, loginUser.getId()).count();
        if (count > 5) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "你最多加入 5 个队伍");
        }
        // 队伍已满
        count = countUserTeamByTeamId(teamId);
        if (count > team.getMaxNum()) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "队伍已满");
        }
        // 不能加入已经加入的队伍
        count = userTeamService.lambdaQuery()
                .eq(UserTeam::getTeamId,teamId).eq(UserTeam::getUserId,loginUser.getId()).count();
        if (count > 0) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "重复加入队伍");
        }
        // 保存
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUser.getId());
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(LocalDateTime.now());
        return userTeamService.save(userTeam);
    }

    @Override
    @Transactional
    public B<Boolean> updateTeam(TeamUpdateRequest teamUpdateRequest) {
        if (teamUpdateRequest == null) {
            return B.parameter();
        }
        Long maxNum = teamUpdateRequest.getMaxNum();
        String teamName = teamUpdateRequest.getName();
        String teamDescription = teamUpdateRequest.getDescription();
        if (maxNum != null) {
            if (maxNum < 1 || maxNum >= 20) {
                return B.parameter("参数不满足要求");
            }
        }
        //队伍标题 <= 20
        if (StringUtils.hasText(teamName)) {
            if (teamName.length() >= 20) {
                return B.parameter("参数不满足要求");
            }
        }
        //描述 <= 512
        if (StringUtils.hasText(teamDescription)) {
            if (teamDescription.length() >= 512) {
                return B.parameter("参数不满足要求");
            }
        }
        User loginUser = UserUtils.getLoginUser();
        Long id = teamUpdateRequest.getId();
        if (LongUtil.isEmpty(id)) {
            return B.parameter();
        }
        Team team = this.getById(id);
        if (team == null) {
            return B.parameter();
        }
        if (!loginUser.getId().equals(team.getUserId()) && !UserRole.isAdmin()) {
            return B.auth();
        }
        Integer status = teamUpdateRequest.getStatus();
        if (status != null && status >= 0) {
            TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusByValue(status);
            String password = teamUpdateRequest.getPassword();
            if (statusEnum.equals(TeamStatusEnum.ENCRYPTION)) {
                if (!StringUtils.hasText(password)) {
                    return B.parameter("请设置密码");
                }
            }
        }
        team = new Team();
        team.setId(teamUpdateRequest.getId());
        BeanUtils.copyProperties(teamUpdateRequest, team);
        if (StringUtils.hasText(team.getPassword())) {
            team.setPassword(MD5.getTeamMD5(team.getPassword()));
        }

        boolean b = this.updateById(team);
        if (!b) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "跟新失败");
        }
        return B.ok();
    }

    // 退出退伍
    @Override
    @Transactional(rollbackFor = Exception.class)
    public B<Boolean> quitTeam(Long teamId, HttpServletRequest request) {
        if (LongUtil.isEmpty(teamId)) {
            return B.parameter();
        }
        User loginUser = UserUtils.getLoginUser();

        Team team = this.getById(teamId);
        if (team == null || LongUtil.isEmpty(team.getId())) {
            return B.parameter("队伍不存在");
        }
        Long userId = loginUser.getId();
        long count = userTeamService.lambdaQuery()
                .eq(UserTeam::getUserId,userId).eq(UserTeam::getTeamId,teamId).count();
        if (count != 1) {
            return B.parameter("未加入队伍");
        }
        long teamHasJoinName = this.countUserTeamByTeamId(teamId);
        // 队伍只剩一人
        if (teamHasJoinName == 1) {
            boolean removeById = this.removeById(teamId);
            if (removeById) {
                boolean record = chatRecordService.deleteTeamChatRecordByTeamId(teamId);
                if (!record) {
                    throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除错误");
                }
            }
        } else {
            if (userId.equals(team.getUserId())) {
                // 队长 退出
                List<UserTeam> list = userTeamService.lambdaQuery().eq(UserTeam::getTeamId, teamId).last("order by join_time asc limit 2").list();
                if (CollectionUtils.isEmpty(list) || list.size() <= 1) {
                    return B.parameter();
                }
                UserTeam userTeam = list.get(1);
                Long teamUserId = userTeam.getUserId();
                // 跟新队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(teamUserId);
                boolean update = this.updateById(updateTeam);
                if (!update) {
                    throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除错误");
                }
            }
        }
        // 移除用户
        boolean remove = userTeamService.lambdaUpdate()
                .eq(UserTeam::getUserId,userId).eq(UserTeam::getTeamId,teamId).remove();
        if (!remove) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除错误");
        }
        return B.ok();
    }

    /**
     * 根据 team ID 获取加入的人数
     *
     * @param teamId 队伍id
     * @return 数量
     */
    private long countUserTeamByTeamId(Long teamId) {
        return userTeamService.lambdaQuery().eq(UserTeam::getTeamId, teamId).count();
    }

    @Override
    public B<Boolean> quitTeamByUser(UserTeamQuitRequest userTeamQuitRequest, HttpServletRequest request) {
        if (userTeamQuitRequest == null) {
            return B.parameter();
        }
        Long teamId = userTeamQuitRequest.getTeamId();
        Long userId = userTeamQuitRequest.getUserId();
        if (LongUtil.isEmpty(teamId) && LongUtil.isEmpty(userId)) {
            return B.parameter();
        }
        User loginUser = UserUtils.getLoginUser();
        Long loginUserId = loginUser.getId();
        Team team = baseMapper.selectById(teamId);
        if (team == null) {
            return B.parameter();

        }
        Long teamUserId = team.getUserId();
        if (LongUtil.isEmpty(teamUserId)) {
            return B.parameter();

        }
        if (!loginUserId.equals(teamUserId)) {
            return B.auth();
        }
        UserTeam userTeam = userTeamService.lambdaQuery()
                .eq(UserTeam::getUserId,userId)
                .eq(UserTeam::getTeamId,teamId).one();
        if (userTeam == null) {
            return B.parameter();
        }
        if (userTeamService.removeById(userTeam)) {
            return B.ok();
        }
        return B.error();
    }

    @Override
    public List<Long> getUserTeamListById(Long teamId, Long userId) {
        List<UserTeam> list = userTeamService.lambdaQuery().eq(UserTeam::getTeamId,teamId).list();
        List<Long> teamIdList = new ArrayList<>();
        if (list.size() <= 0) {
            return teamIdList;
        }
        for (UserTeam userTeam : list) {
            Long id = userTeam.getUserId();
            if (userId.equals(id)) {
                continue;
            }
            teamIdList.add(id);
        }
        return teamIdList;
    }

    @Override
    public Team getTeamByTeamUser(Long teamId, Long userId) {
        return this.lambdaQuery().eq(Team::getUserId, userId).eq(Team::getId, teamId).one();
    }

    @Override
    public boolean updateTeamByTeam(Team team) {
        if (team == null) {
            return false;
        }
        return this.updateById(team);
    }

    @Override
    public B<Map<String, Object>> getTeamIdByChat(IdRequest idRequest) {
        if (idRequest == null) {
            return B.parameter();
        }
        Long teamId = idRequest.getId();
        if (LongUtil.isEmpty(teamId)) {
            return B.parameter();
        }
        User user = UserUtils.getLoginUser();
        if (LongUtil.isEmpty(teamId)) {
            return B.parameter();
        }
        Team team = this.getById(teamId);
        Long userId = user.getId();
        if (team == null) {
            return B.parameter("队伍不存在");
        }
        List<UserTeam> userTeams = userTeamService.lambdaQuery().eq(UserTeam::getTeamId, teamId).list();
        if (userTeams == null || userTeams.size() <= 0) {
            return B.parameter();
        }
        boolean userTeamIs = false;
        List<Long> userIds = new ArrayList<>();
        for (UserTeam userTeam : userTeams) {
            Long teamUserId = userTeam.getUserId();
            if (teamUserId.equals(userId)) {
                userTeamIs = true;
                continue;
            }
            userIds.add(teamUserId);
        }
        if (!userTeamIs) {
            return B.parameter();
        }
        List<UserAvatarVo> ids = userService.getUserAvatarVoByIds(userIds);
        List<TeamChatRecord> teamChatRecords = chatRecordService.lambdaQuery()
                .eq(TeamChatRecord::getCreateTime, teamId).list();
        Map<String, Object> map = new HashMap<>();
        map.put("teamName", team.getName());
        map.put("teamUrl", team.getAvatarUrl());
        map.put("userList", ids);
        map.put("teamChat", teamChatRecords);
        return B.ok(map);
    }


    @Override
    public B<TeamUserAvatarVo> getByTeamAvatarId(Long id) {
        if (id == null || id <= 0) {
            return B.empty();
        }
        User loginUser = UserUtils.getLoginUser();
        Long loginUserId = loginUser.getId();
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", loginUserId);
        wrapper.eq("team_id", id);
        long count = userTeamService.count(wrapper);
        if (count != 1) {
            return B.parameter("请先加入该队伍");

        }
        TeamUserAvatarVo teamUserAvatarVo = baseMapper.selectAvatarByID(id);
        if (teamUserAvatarVo == null) {
            return B.parameter("请先加入该队伍");
        }
        return B.ok(teamUserAvatarVo);
    }

    @Override
    public boolean teamById(Long id) {
        long con = baseMapper.selectCountById(id);
        return con == 1;
    }

    @Override
    public Team getTeamByUser(Long teamId, Long uid) {
        Team team = this.getById(teamId);
        if (team == null) {
            return null;
        }
        if (userTeamService.lambdaQuery()
                .eq(UserTeam::getUserId, uid)
                .eq(UserTeam::getTeamId, teamId)
                .count() <= 0) {
            return null;
        }
        return team;
    }

    public boolean isUserTeam(Long teamId, Long uid) {
        if (!this.teamById(teamId)) {
            return false;
        }
        return userTeamService.lambdaQuery()
                .eq(UserTeam::getUserId, uid)
                .eq(UserTeam::getTeamId, teamId)
                .count() > 0;
    }

    @Override
    public boolean isAdminTeam(Long teamId, Long userId) {
        if (teamId == null || userId == null) {
            return false;
        }
        Team team = this.getById(teamId);
        if (team == null) {
            return false;
        }
        Long teamUserId = team.getUserId();
        return userId.equals(teamUserId);
    }


}




